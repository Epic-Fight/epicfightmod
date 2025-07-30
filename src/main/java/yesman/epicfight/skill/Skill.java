package yesman.epicfight.skill;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPExecuteSkill;
import yesman.epicfight.network.server.SPSetSkillValue;
import yesman.epicfight.network.server.SPSetSkillValue.Target;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillCancelEvent;

public abstract class Skill {
	public static SkillBuilder<Skill> createBuilder() {
		return new SkillBuilder<> ();
	}
	
	public static SkillBuilder<Skill> createIdentityBuilder() {
		return (new SkillBuilder<> ()).setCategory(SkillCategories.IDENTITY).setResource(Resource.NONE);
	}
	
	public static SkillBuilder<Skill> createMoverBuilder() {
		return (new SkillBuilder<> ()).setCategory(SkillCategories.MOVER).setResource(Resource.STAMINA);
	}
	
	private final Map<Attribute, AttributeModifier> attributes = Maps.newHashMap();
	protected final ResourceLocation registryName;
	protected final SkillCategory category;
	protected final CreativeModeTab creativeTab;
	protected final ActivateType activateType;
	protected final Resource resource;
	protected float consumption;
	protected int maxDuration;
	protected int maxStackSize;
	protected int requiredXp;
	
	public Skill(SkillBuilder<? extends Skill> builder) {
		if (builder.registryName == null) {
			Exception e = new IllegalArgumentException("No registry name is given for " + this.getClass().getCanonicalName());
			e.printStackTrace();
		}
		
		this.registryName = builder.registryName;
		this.category = builder.category;
		this.creativeTab = builder.tab;
		this.activateType = builder.activateType;
		this.resource = builder.resource;
	}
	
	public void setParams(CompoundTag parameters) {
		this.consumption = parameters.getFloat("consumption");
		this.maxDuration = parameters.getInt("max_duration");
		this.maxStackSize = parameters.contains("max_stacks") ? parameters.getInt("max_stacks") : 1;
		this.requiredXp = parameters.getInt("xp_requirement");
		
		this.attributes.clear();
		
		if (parameters.contains("attribute_modifiers")) {
			ListTag attributeList = parameters.getList("attribute_modifiers", 10);
			
			for (Tag tag : attributeList) {
				CompoundTag comp = (CompoundTag)tag;
				String attribute = comp.getString("attribute");
				Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(ResourceLocation.parse(attribute));
				AttributeModifier modifier = ParseUtil.toAttributeModifier(comp);
				
				this.attributes.put(attr, modifier);
			}
		}
	}
	
	/**
	 * Check the player state if he can execute the skill or not
	 */
	public boolean isExecutableState(PlayerPatch<?> executor) {
		return !executor.getOriginal().isSpectator() && !executor.isInAir() && executor.getEntityState().canUseSkill();
	}
	
	/**
	 * Check the resource & other restrictions to execute the skill
	 */
	public boolean canExecute(SkillContainer container) {
		return checkExecuteCondition(container);
	}
	
	/**
	 * This makes the skill icon white in Gui if it returns false
	 */
	public boolean checkExecuteCondition(SkillContainer container) {

		return true;
	}
	
	/**
	 * Notify the executor unmet conditions to cast the skill
	 */
	@OnlyIn(Dist.CLIENT)
	public void validationFeedback(SkillContainer container) {
		
	}
	
	/**
	 * Get a packet to send to the server
	 */
	@OnlyIn(Dist.CLIENT)
	public Object getExecutionPacket(SkillContainer container, FriendlyByteBuf args) {
		return new CPExecuteSkill(container.getSlotId(), CPExecuteSkill.WorkType.ACTIVATE, args);
	}
	
	@OnlyIn(Dist.CLIENT)
	public FriendlyByteBuf gatherArguments(SkillContainer container, ControlEngine controlEngine) {
		return null;
	}


	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(container.getSlotId());
		ServerPlayerPatch executor = container.getServerExecutor();

		
		if (executor.isChargingSkill()) {
			if (this instanceof ChargeableSkill chargingSkill) {
				feedbackPacket.getBuffer().writeInt(executor.getAccumulatedChargeAmount());
				chargingSkill.castSkill(executor, container, executor.getAccumulatedChargeAmount(), feedbackPacket, false);
				executor.resetSkillCharging();
				EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
			}
		} else {
			if (executor.isHoldingSkill())
			{
				if (this instanceof HoldableSkill holdableSkill) {
					holdableSkill.onStopHolding(container, args);
					executor.resetHolding();
				}
			}
			EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
		}
	}
	
	public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
		ServerPlayerPatch executor = container.getServerExecutor();
		SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, container);
		executor.getEventListener().triggerEvents(EventType.SKILL_CANCEL_EVENT, skillCancelEvent);
		EpicFightNetworkManager.sendToPlayer(SPSkillExecutionFeedback.expired(container.getSlotId()), executor.getOriginal());
	}
	
	public final float getDefaultConsumptionAmount(PlayerPatch<?> executor) {
        return switch (this.resource)
        {
            case STAMINA -> executor.getModifiedStaminaConsume(this.consumption);
            case WEAPON_CHARGE, COOLDOWN -> 1;
            default -> 0.0F;
        };
	}
	
	/**
	 * Instant feedback when the skill is executed successfully
	 * @param container
	 * @param args
	 */
	@OnlyIn(Dist.CLIENT)
	public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
	}
	
	/**
	 * Called when the duration ends.
	 * @param container
	 * @param args
	 */
	@OnlyIn(Dist.CLIENT)
	public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
		LocalPlayerPatch executor = container.getClientExecutor();
		SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, container);
		executor.getEventListener().triggerEvents(EventType.SKILL_CANCEL_EVENT, skillCancelEvent);
	}
	
	public void onInitiate(SkillContainer container) {
		container.maxDuration = this.maxDuration;
		
		for (Map.Entry<Attribute, AttributeModifier> stat : this.attributes.entrySet()) {
			AttributeInstance attr = container.getExecutor().getOriginal().getAttribute(stat.getKey());
			
			if (!attr.hasModifier(stat.getValue())) {
				attr.addTransientModifier(stat.getValue());
			}
		}
	}
	
	/**
	 * When skill removed from the container
	 * @param container
	 */
	public void onRemoved(SkillContainer container) {
		for (Map.Entry<Attribute, AttributeModifier> stat : this.attributes.entrySet()) {
			AttributeInstance attr = container.getExecutor().getOriginal().getAttribute(stat.getKey());
			
			if (attr.hasModifier(stat.getValue())) {
				attr.removeModifier(stat.getValue());
			}
		}
	}
	
	/**
	 * When stacks reach to zero
	 * @param container
	 */
	public void onReset(SkillContainer container) {
	}
	
	public void setConsumption(SkillContainer container, float value) {
		container.resource = Math.min(Math.max(value, 0), container.getMaxResource());
		
		if (value >= container.getMaxResource()) {
			if (container.stack < this.maxStackSize) {
				container.stack++;	
				container.resource = 0;
				container.prevResource = 0;
			} else {
				container.resource = container.getMaxResource();
				container.prevResource = container.getMaxResource();
			}
		} else if (value == 0 && container.stack > 0) {
			--container.stack;
		}
	}
	
	public void updateContainer(SkillContainer container) {
		container.prevResource = container.resource;
		container.prevDuration = container.duration;
		
		if (this.resource == Resource.COOLDOWN) {
			if (container.stack < this.maxStackSize) {
				container.setResource(container.resource + this.getCooldownRegenPerSecond(container.getExecutor()) * EpicFightSharedConstants.A_TICK);
			}
		}
		
		if (container.isActivated()) {
			if (this.activateType == ActivateType.DURATION) {
				container.duration--;
			}
			
			boolean isEnd = false;
			
			if (this.activateType == ActivateType.TOGGLE) {
				if (container.stack <= 0 && !container.getExecutor().getOriginal().isCreative()) {
					isEnd = true;
				}
			} else {
				if (container.duration <= 0) {
					isEnd = true;
				}
			}
			
			if (isEnd) {
				if (!container.getExecutor().isLogicalClient() && this.activateType != ActivateType.CHARGING) {
					this.cancelOnServer(container, null);
				}
				
				container.deactivate();
			}
		}

		if (this.activateType == ActivateType.HELD && container.getExecutor().getHoldableSkill() == this)
		{
			HoldableSkill holdableSkill = (HoldableSkill) this;
			holdableSkill.holdTick(container);

			if (!container.getExecutor().isLogicalClient())
			{
				container.getExecutor().resetActionTick();
			}
		}
		
		if (this.activateType == Skill.ActivateType.CHARGING && container.getExecutor().getChargingSkill() == this) {
			ChargeableSkill chargingSkill = (ChargeableSkill)this;
			chargingSkill.chargingTick(container.getExecutor());
			
			if (!container.getExecutor().isLogicalClient()) {
				container.getExecutor().resetActionTick();
				
				if (container.getExecutor().getSkillChargingTicks(1.0F) > chargingSkill.getAllowedMaxChargingTicks()) {
					SPSkillExecutionFeedback feedbackPacket = SPSkillExecutionFeedback.executed(container.getSlotId());
					feedbackPacket.getBuffer().writeInt(container.getExecutor().getAccumulatedChargeAmount());
					chargingSkill.castSkill(container.getServerExecutor(), container, container.getExecutor().getAccumulatedChargeAmount(), feedbackPacket, true);
					container.getExecutor().resetSkillCharging();
					EpicFightNetworkManager.sendToPlayer(feedbackPacket, container.getServerExecutor().getOriginal());
				}
			}
		}
	}
	
	public boolean isActivated(SkillContainer container) {
		return this.equals(container.getSkill()) ? container.isActivated() : false;
	}
	
	public boolean isDisabled(SkillContainer container) {
		return this.equals(container.getSkill()) ? container.isDisabled() : true;
	}
	
	/**
	 * Make sure this method is called in a server side.
	 */
	public void setConsumptionSynchronize(SkillContainer container, float amount) {
		if (this.equals(container.containingSkill)) {
			setSkillConsumptionSynchronize(container, amount);
		}
	}
	
	public void setMaxDurationSynchronize(SkillContainer container, int amount) {
		if (this.equals(container.containingSkill)) {
			setSkillMaxDurationSynchronize(container, amount);
		}
	}
	
	public void setDurationSynchronize(SkillContainer container, int amount) {
		if (this.equals(container.containingSkill)) {
			setSkillDurationSynchronize(container, amount);
		}
	}
	
	public void setStackSynchronize(SkillContainer container, int amount) {
		if (this.equals(container.containingSkill)) {
			setSkillStackSynchronize(container, amount);
		}
	}
	
	public void setMaxResourceSynchronize(SkillContainer container, float amount) {
		if (this.equals(container.containingSkill)) {
			setSkillMaxResourceSynchronize(container, amount);
		}
	}
	
	public static void setSkillConsumptionSynchronize(SkillContainer skillContainer, float amount) {
		skillContainer.setResource(amount);
		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.RESOURCE, skillContainer.getSlotId(), amount, false), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillDurationSynchronize(SkillContainer skillContainer, int amount) {
		skillContainer.setDuration(amount);
		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.DURATION, skillContainer.getSlotId(), amount, false), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillMaxDurationSynchronize(SkillContainer skillContainer, int amount) {
		skillContainer.setMaxDuration(amount);
		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.MAX_DURATION, skillContainer.getSlotId(), amount, false), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillStackSynchronize(SkillContainer skillContainer, int amount) {
		skillContainer.setStack(amount);
		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.STACK, skillContainer.getSlotId(), amount, false), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillMaxResourceSynchronize(SkillContainer skillContainer, float amount) {
		skillContainer.setMaxResource(amount);
		EpicFightNetworkManager.sendToPlayer(new SPSetSkillValue(Target.MAX_RESOURCE, skillContainer.getSlotId(), amount, false), skillContainer.getServerExecutor().getOriginal());
	}
	/**
	 * Make sure this method is called in a server side.
	 */
	
	public ResourceLocation getRegistryName() {
		return this.registryName;
	}
	
	public String getTranslationKey() {
		return String.format("skill.%s.%s", this.getRegistryName().getNamespace(), this.getRegistryName().getPath());
	}
	
	public float getCooldownRegenPerSecond(PlayerPatch<?> player) {
		return 1.0F;
	}
	
	public SkillCategory getCategory() {
		return this.category;
	}
	
	public CreativeModeTab getCreativeTab() {
		return this.creativeTab;
	}
	
	public int getMaxStack() {
		return this.maxStackSize;
	}
	
	public int getMaxDuration() {
		return this.maxDuration;
	}

	public float getConsumption() {
		return this.consumption;
	}
	
	public int getRequiredXp() {
		return this.requiredXp;
	}
	
	public Set<Entry<Attribute, AttributeModifier>> getModfierEntry() {
		return this.attributes.entrySet();
	}
	
	public boolean resourcePredicate(PlayerPatch<?> playerpatch) {
		return playerpatch.consumeForSkill(this, this.resource);
	}
	
	public boolean shouldDeactivateAutomatically(PlayerPatch<?> executer) {
		return !executer.getOriginal().isCreative();
	}
	
	public ActivateType getActivateType() {
		return this.activateType;
	}
	
	public Resource getResourceType() {
		return this.resource;
	}
	
	public Skill getPriorSkill() {
		return null;
	}
	
	public Skill registerPropertiesToAnimation() {
		return this;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onScreen(LocalPlayerPatch playerpatch, float resolutionX, float resolutionY) {
		
	}
	
	/**
	 * @param itemStack.getCapability() == @param cap
	 * @return
	 */
	@OnlyIn(Dist.CLIENT)
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
		return Lists.newArrayList();
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		return list;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getSkillTexture() {
		return ResourceLocation.fromNamespaceAndPath(this.getRegistryName().getNamespace(), String.format("textures/gui/skills/%s/%s.png", this.category.toString().toLowerCase(Locale.ROOT), this.getRegistryName().getPath()));
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean shouldDraw(SkillContainer container) {
		return false;
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
	public Component getDisplayName() {
		return Component.translatable(String.format("%s.%s.%s", "skill", this.getRegistryName().getNamespace(), this.getRegistryName().getPath()));
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<WeaponCategory> getAvailableWeaponCategories() {
		return null;
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumeIconList) {
		return false;
	}
	
	public enum ActivateType {
		ONE_SHOT, DURATION, DURATION_INFINITE, TOGGLE, CHARGING, HELD
	}
	
	public enum Resource {
		NONE(
			(skillContainer, playerpatch, amount) -> true,
			(skillContainer, playerpatch, amount) -> {}
		),
		
		WEAPON_CHARGE(
			(skillContainer, playerpatch, amount) -> skillContainer.getStack() >= amount,
			(skillContainer, playerpatch, amount) -> {
				Skill.setSkillStackSynchronize(skillContainer, skillContainer.getStack() - 1);
			}
		),
		
		COOLDOWN(
			(skillContainer, playerpatch, amount) -> skillContainer.getStack() >= amount,
			(skillContainer, playerpatch, amount) -> {
				Skill.setSkillStackSynchronize(skillContainer, skillContainer.getStack() - 1);
			}
		),
		
		STAMINA(
			(skillContainer, playerpatch, amount) -> playerpatch.hasStamina(amount),
			(skillContainer, playerpatch, amount) -> {
				playerpatch.resetActionTick();
				playerpatch.setStamina(playerpatch.getStamina() - amount);
			}
		),
		
		HEALTH(
			(skillContainer, playerpatch, amount) -> playerpatch.getOriginal().getHealth() > amount,
			(skillContainer, playerpatch, amount) -> {
				playerpatch.getOriginal().setHealth(playerpatch.getOriginal().getHealth() - amount);
			}
		);
		
		public final ResourcePredicate predicate;
		public final ResourceConsumer consumer;
		
		Resource(ResourcePredicate predicate, ResourceConsumer consumer) {
			this.predicate = predicate;
			this.consumer = consumer;
		}
		
		@FunctionalInterface
		public interface ResourcePredicate {
			boolean canExecute(SkillContainer skillContainer, PlayerPatch<?> playerpatch, float amount);
		}
		
		@FunctionalInterface
		public interface ResourceConsumer {
			void consume(SkillContainer skillContainer, ServerPlayerPatch playerpatch, float amount);
		}
	}
}