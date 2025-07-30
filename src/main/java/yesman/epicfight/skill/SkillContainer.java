package yesman.epicfight.skill;

import java.util.Set;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.skill.Skill.ActivateType;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillConsumeEvent;
import yesman.epicfight.world.entity.eventlistener.SkillExecuteEvent;

public class SkillContainer {
	protected Skill containingSkill;
	private PlayerPatch<?> executor;
	protected int prevDuration;
	protected int duration;
	protected int maxDuration;
	protected float resource;
	protected float prevResource;
	protected float maxResource;
	protected boolean isActivated;
	protected int stack;
	protected SkillSlot slot;
	protected SkillDataManager skillDataManager;
	protected boolean disabled;
	
	protected Skill.Resource lastResource;
	
	public SkillContainer(PlayerPatch<?> executor, SkillSlot skillSlot) {
		this.executor = executor;
		this.slot = skillSlot;
		this.skillDataManager = new SkillDataManager(skillSlot.universalOrdinal(), this);
	}
	
	/* Fix the mistype in the future */
	public void setExecutor(PlayerPatch<?> executor) {
		this.executor = executor;
	}
	
	/* Fix the mistype in the future */
	public PlayerPatch<?> getExecutor() {
		return this.executor;
	}
	
	public LocalPlayerPatch getClientExecutor() {
		return (LocalPlayerPatch)this.executor;
	}
	
	public ServerPlayerPatch getServerExecutor() {
		return (ServerPlayerPatch)this.executor;
	}
	
	public boolean setSkill(Skill skill) {
		return this.setSkill(skill, false);
	}
	
	public boolean setSkill(Skill skill, boolean initialize) {
		if (this.containingSkill == skill && !initialize) {
			return false;
		}
		
		if (skill != null && skill.category != this.slot.category()) {
			return false;
		}
		
		if (this.containingSkill != null) {
			this.containingSkill.onRemoved(this);
		}
		
		this.containingSkill = skill;
		this.resetValues();
		
		// Remove all data keys
		this.skillDataManager.clearData();
		
		if (skill != null) {
			Set<SkillDataKey<?>> datakeys = SkillDataKey.getSkillDataKeyMap().get(skill.getClass());
			
			if (datakeys != null) {
				datakeys.forEach(this.skillDataManager::registerData);
			}
			
			skill.onInitiate(this);
			
			this.setMaxResource(skill.consumption);
			this.setMaxDuration(skill.maxDuration);
		}
		
		this.stack = 0;
		
		if (initialize) {
			this.setDisabled(false);
		}
		
		return true;
	}
	
	public boolean isDisabled() {
		return this.disabled;
	}
	
	public void setDisabled(boolean disable) {
		this.disabled = disable;
	}
	
	public void resetValues() {
		this.isActivated = false;
		this.prevDuration = 0;
		this.duration = 0;
		this.prevResource = 0.0F;
		this.resource = 0.0F;
	}
	
	public boolean isEmpty() {
		return this.containingSkill == null;
	}
	
	public void setResource(float value) {
		if (this.containingSkill != null) {
			this.containingSkill.setConsumption(this, value);
		} else {
			this.prevResource = 0;
			this.resource = 0;
		}
	}
	
	public void setMaxDuration(int value) {
		this.maxDuration = Math.max(value, 0);
	}
	
	public void setDuration(int value) {
		if (this.containingSkill != null) {
			if (!this.isActivated() && value > 0) {
				this.isActivated = true;
			}
			
			this.duration = Math.min(this.maxDuration, Math.max(value, 0));
		} else {
			this.duration = 0;
		}
	}
	
	public void setStack(int stack) {
		if (this.containingSkill != null) {
			this.stack = Math.min(this.containingSkill.maxStackSize, Math.max(stack, 0));
			
			if (this.stack <= 0 && this.containingSkill.shouldDeactivateAutomatically(this.executor)) {
				this.deactivate();
				this.containingSkill.onReset(this);
			}
		} else {
			this.stack = 0;
		}
	}
	
	public void setMaxResource(float maxResource) {
		this.maxResource = maxResource;
	}
	
	@OnlyIn(Dist.CLIENT)
	public SkillExecuteEvent sendExecuteRequest(LocalPlayerPatch executor, ControlEngine controlEngine) {
		SkillExecuteEvent event = new SkillExecuteEvent(executor, this);
		
		if (this.containingSkill == null) {
			return event;
		}
		
		Object packet = null;
		
		if (this.containingSkill instanceof ChargeableSkill chargeableSkill && this.containingSkill.getActivateType() == Skill.ActivateType.CHARGING) {
			if (executor.isChargingSkill(this.containingSkill)) {
				packet = this.containingSkill.getExecutionPacket(this, this.containingSkill.gatherArguments(this, controlEngine));
				executor.resetSkillCharging();
			} else {
				if (!this.canExecute(executor, event)) {
					this.containingSkill.validationFeedback(this);
					return event;
				}
				
				CPExecuteSkill exeSkillPacket = new CPExecuteSkill(this.getSlotId(), CPExecuteSkill.WorkType.CHARGING_START);
				chargeableSkill.gatherChargingArguments(executor, controlEngine, exeSkillPacket.getBuffer());
				packet = exeSkillPacket;
			}
		} else if (this.containingSkill instanceof HoldableSkill && this.containingSkill.getActivateType() == ActivateType.HELD) {
			if (executor.isHoldingSkill(this.containingSkill)) {
				packet = this.containingSkill.getExecutionPacket(this, this.containingSkill.gatherArguments(this, controlEngine));
				executor.resetHolding();
			} else {
				if (!this.canExecute(executor, event)) {
					this.containingSkill.validationFeedback(this);
					return event;
				}


                packet = new CPExecuteSkill(this.getSlotId(), CPExecuteSkill.WorkType.HOLD_START);
			}
		} else {
			if (!this.canExecute(executor, event)) {
				this.containingSkill.validationFeedback(this);
				return event;
			}
			
			packet = this.containingSkill.getExecutionPacket(this, this.containingSkill.gatherArguments(this, controlEngine));
		}
		
		if (packet != null) {
			controlEngine.addPacketToSend(packet);
		}
		
		return event;
	}
	
	public boolean requestExecute(ServerPlayerPatch executor, FriendlyByteBuf buf) {
		SkillExecuteEvent event = new SkillExecuteEvent(executor, this);
		
		if (this.canExecute(executor, event)) {
			this.containingSkill.executeOnServer(this, buf);
			return true;
		}
		
		return false;
	}
	
	public boolean requestCancel(ServerPlayerPatch executor, FriendlyByteBuf buf) {
		if (this.containingSkill != null) {
			this.containingSkill.cancelOnServer(this, buf);
			return true;
		}
		
		return false;
	}
	
	public boolean requestCharging(ServerPlayerPatch executor, FriendlyByteBuf buf) {
		if (this.containingSkill instanceof ChargeableSkill chargeableSkill) {
			SkillExecuteEvent event = new SkillExecuteEvent(executor, this);
			
			if (this.canExecute(executor, event)) {
				SkillConsumeEvent consumeEvent = new SkillConsumeEvent(executor, this.containingSkill, this.containingSkill.resource);
				executor.getEventListener().triggerEvents(EventType.SKILL_CONSUME_EVENT, consumeEvent);
				
				if (!consumeEvent.isCanceled()) {
					consumeEvent.getResourceType().consumer.consume(this, executor, consumeEvent.getAmount());
				}
				
				executor.startSkillCharging(chargeableSkill);
				
				return true;
			}
		}
		
		return false;
	}

	public boolean requestHold(ServerPlayerPatch executor, FriendlyByteBuf buf) {
		if (this.containingSkill instanceof HoldableSkill holdableSkill) {
			SkillExecuteEvent event = new SkillExecuteEvent(executor, this);

			if (this.canExecute(executor, event)) {
				SkillConsumeEvent consumeEvent = new SkillConsumeEvent(executor, this.containingSkill, this.containingSkill.resource);
				executor.getEventListener().triggerEvents(EventType.SKILL_CONSUME_EVENT, consumeEvent);

				if (!consumeEvent.isCanceled()) {
					consumeEvent.getResourceType().consumer.consume(this, executor, consumeEvent.getAmount());
				}

				executor.startSkillHolding(holdableSkill);

				return true;
			}
		}

		return false;
	}
	
	public SkillDataManager getDataManager() {
		return this.skillDataManager;
	}
	
	public float getResource() {
		return this.resource;
	}

	public int getRemainDuration() {
		return this.duration;
	}
	
	public boolean canExecute(PlayerPatch<?> executor, SkillExecuteEvent event) {
		if (this.containingSkill == null) {
			return false;
		} else {
			if (executor.isChargingSkill(this.containingSkill) && this.containingSkill instanceof ChargeableSkill chargingSkill) {
				if (executor.isLogicalClient()) {
					return true;
				} else {
					return executor.getSkillChargingTicks() >= chargingSkill.getMinChargingTicks();
				}
			}

			if (executor.isHoldingSkill(this.containingSkill) && this.containingSkill instanceof HoldableSkill) {
				return true;
			}
			
			event.setSkillExecutable(this.containingSkill.canExecute(this));
			event.setStateExecutable(this.containingSkill.isExecutableState(executor));
			executor.getEventListener().triggerEvents(EventType.SKILL_EXECUTE_EVENT, event);
			
			if (!event.isCanceled() && event.isExecutable()) {
				return (executor.getOriginal().isCreative() || this.containingSkill.resourcePredicate(executor)) || (this.isActivated() && this.containingSkill.activateType == ActivateType.DURATION);
			} else {
				return false;
			}
		}
	}
	
	public void update() {
		if (this.containingSkill != null) {
			this.containingSkill.updateContainer(this);
		}
	}
	
	public int getStack() {
		return this.stack;
	}
	
	public SkillSlot getSlot() {
		return this.slot;
	}
	
	public int getSlotId() {
		return this.slot.universalOrdinal();
	}
	
	public Skill getSkill() {
		return this.containingSkill;
	}
	
	public float getMaxResource() {
		return this.maxResource;
	}
	
	public void activate() {
		if (!this.isActivated) {
			this.prevDuration = this.maxDuration;
			this.duration = this.maxDuration;
			this.isActivated = true;
		}
	}
	
	public void deactivate() {
		if (this.isActivated) {
			this.prevDuration = 0;
			this.duration = 0;
			this.isActivated = false;
		}
	}
	
	public boolean isActivated() {
		return this.isActivated;
	}
	
	public boolean hasSkill(Skill skill) {
		return this.containingSkill != null && this.containingSkill.equals(skill);
	}
	
	public boolean isFull() {
		return this.containingSkill == null || this.stack >= this.containingSkill.maxStackSize;
	}
	
	public float getResource(float partialTicks) {
		return this.containingSkill != null && this.maxResource > 0 ? (this.prevResource + ((this.resource - this.prevResource) * partialTicks)) / this.maxResource : 0;
	}
	
	public float getNeededResource() {
		return this.containingSkill != null ? this.maxResource - this.resource : 0;
	}

	public float getDurationRatio(float partialTicks) {
		return this.containingSkill != null && this.maxDuration > 0 ? (this.prevDuration + ((this.duration - this.prevDuration) * partialTicks)) / this.maxDuration : 0;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof SkillContainer skillContainer) {
			return this.slot.equals(skillContainer.slot);
		}
		
		return false;
	}
}