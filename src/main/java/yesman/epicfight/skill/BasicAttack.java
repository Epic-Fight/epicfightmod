package yesman.epicfight.skill;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.AnimationVariables.IndependentAnimationVariableKey;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.BasicAttackEvent;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillConsumeEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class BasicAttack extends Skill {
	private static final UUID EVENT_UUID = UUID.fromString("a42e0198-fdbc-11eb-9a03-0242ac130003");

    private float dashAttackConsumption = 0f;
    private float airAttackConsumption = 0f;

	/** Decides if the animation used for combo attack **/
	public static final IndependentAnimationVariableKey<Boolean> COMBO = AnimationVariables.independent(animator -> false, false);
	
	public static SkillBuilder<BasicAttack> createBasicAttackBuilder() {
		return new SkillBuilder<BasicAttack>().setCategory(SkillCategories.BASIC_ATTACK).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
	}
	
	public static void setComboCounterWithEvent(ComboCounterHandleEvent.Causal reason, ServerPlayerPatch playerpatch, SkillContainer container, AnimationAccessor<? extends StaticAnimation> causalAnimation, int value) {
		int prevValue = container.getDataManager().getDataValue(SkillDataKeys.COMBO_COUNTER.get());
		ComboCounterHandleEvent comboResetEvent = new ComboCounterHandleEvent(reason, playerpatch, causalAnimation, prevValue, value);
		container.getExecutor().getEventListener().triggerEvents(EventType.COMBO_COUNTER_HANDLE_EVENT, comboResetEvent);
		container.getDataManager().setData(SkillDataKeys.COMBO_COUNTER.get(), comboResetEvent.getNextValue());
	}
	
	public BasicAttack(SkillBuilder<? extends BasicAttack> builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		container.getExecutor().getEventListener().addEventListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID, (event) -> {
			if (event.getAnimation().get().getProperty(ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER).orElse(true)) {
				CapabilityItem itemCapability = event.getPlayerPatch().getHoldingItemCapability(InteractionHand.MAIN_HAND);
				List<AnimationAccessor<? extends AttackAnimation>> comboAnimations = itemCapability.getAutoAttackMotion(container.getExecutor());
				
				if (comboAnimations == null) {
					return;
				}
				
				Set<AnimationAccessor<? extends AttackAnimation>> attackMotionSet = Set.copyOf(Sets.newHashSet(comboAnimations));
				
				if (!attackMotionSet.contains(event.getAnimation()) && itemCapability.shouldCancelCombo(event.getPlayerPatch())) {
					setComboCounterWithEvent(ComboCounterHandleEvent.Causal.ANOTHER_ACTION_ANIMATION, event.getPlayerPatch(), container, event.getAnimation(), 0);
				}
			}
		});
	}

    protected boolean checkConsumption(SkillContainer container, boolean dash, boolean air) {
        if (air) {
            if (resource == Resource.STAMINA) {
                return container.getExecutor().consumeForSkill(this, resource, container.getExecutor().getModifiedStaminaConsume(airAttackConsumption));
            }
            return container.getExecutor().consumeForSkill(this, resource, airAttackConsumption);
        }
        if (dash) {
            if (resource == Resource.STAMINA) {
                return container.getExecutor().consumeForSkill(this, resource, container.getExecutor().getModifiedStaminaConsume(dashAttackConsumption));
            }
            return container.getExecutor().consumeForSkill(this, resource, dashAttackConsumption);

        }
        return container.getExecutor().consumeForSkill(this, resource);
    }
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecutor().getEventListener().removeListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID);
	}

    @Override
    public void setParams(CompoundTag parameters) {
        super.setParams(parameters);
        dashAttackConsumption = parameters.getFloat("dash_attack_consumption");
        airAttackConsumption = parameters.getFloat("air_attack_consumption");
    }

    @Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		EntityState playerState = executor.getEntityState();
		Player player = executor.getOriginal();
		return !(player.isSpectator() || executor.isInAir() || !playerState.canBasicAttack());
	}
	
	@Override
	public void executeOnServer(SkillContainer skillContainer, FriendlyByteBuf args) {
		ServerPlayerPatch executor = skillContainer.getServerExecutor();
		SkillConsumeEvent event = new SkillConsumeEvent(executor, this, this.resource, null);
		executor.getEventListener().triggerEvents(EventType.SKILL_CONSUME_EVENT, event);
		
		if (!event.isCanceled()) {
			event.getResourceType().consumer.consume(skillContainer, executor, event.getAmount());
		}
		
		if (executor.getEventListener().triggerEvents(EventType.BASIC_ATTACK_EVENT, new BasicAttackEvent(executor))) {
			return;
		}
		
		CapabilityItem cap = executor.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		AnimationAccessor<? extends AttackAnimation> attackMotion = null;
		ServerPlayer player = executor.getOriginal();
		SkillDataManager dataManager = skillContainer.getDataManager();
		int comboCounter = dataManager.getDataValue(SkillDataKeys.COMBO_COUNTER.get());
        boolean dashAttack = player.isSprinting();
        boolean airAttack = !skillContainer.getExecutor().getOriginal().onGround() && !skillContainer.getExecutor().getOriginal().isInWater();
		
		if (player.isPassenger()) {
			Entity entity = player.getVehicle();
			
			if ((entity instanceof PlayerRideableJumping rideable && rideable.canJump()) && cap.availableOnHorse() && cap.getMountAttackMotion() != null) {
				comboCounter %= cap.getMountAttackMotion().size();
				attackMotion = cap.getMountAttackMotion().get(comboCounter);
				comboCounter++;
			}
		} else {
			List<AnimationAccessor<? extends AttackAnimation>> combo = cap.getAutoAttackMotion(executor);
			
			if (combo == null) {
				return;
			}
			
			int comboSize = combo.size();

			if (airAttack) {
                comboCounter = comboSize - 1;
            }
            else if (dashAttack) {
				comboCounter = comboSize - 2;
			} else {
				comboCounter %= comboSize - 2;
			}
			
			attackMotion = combo.get(comboCounter);
			comboCounter = (dashAttack || airAttack) ? 0 : comboCounter + 1;
		}
		
		setComboCounterWithEvent(ComboCounterHandleEvent.Causal.ANOTHER_ACTION_ANIMATION, executor, skillContainer, attackMotion, comboCounter);
		
		if (attackMotion != null && checkConsumption(skillContainer, dashAttack, airAttack)) {
			executor.getAnimator().getVariables().put(COMBO, attackMotion, true);
			executor.getAnimator().playAnimation(attackMotion, 0.0F);
			
			boolean stiffAttack = EpicFightGameRules.STIFF_COMBO_ATTACKS.getRuleValue(executor.getOriginal().level());
			SPAnimatorControl animatorControlPacket;
			
			if (stiffAttack) {
				animatorControlPacket = new SPAnimatorControl(AnimatorControlPacket.Action.PLAY, attackMotion, 0.0F, skillContainer.getExecutor());
			} else {
				animatorControlPacket = new SPAnimatorControl(AnimatorControlPacket.Action.PLAY_CLIENT, attackMotion, 0.0F, skillContainer.getExecutor(), AnimatorControlPacket.Layer.COMPOSITE_LAYER, AnimatorControlPacket.Priority.HIGHEST);
			}
			
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(animatorControlPacket, player);
		}
		
		executor.updateEntityState();
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		if (!container.getExecutor().isLogicalClient() && container.getExecutor().getTickSinceLastAction() > 16 && container.getDataManager().getDataValue(SkillDataKeys.COMBO_COUNTER.get()) > 0) {
			setComboCounterWithEvent(ComboCounterHandleEvent.Causal.TIME_EXPIRED, container.getServerExecutor(), container, null, 0);
		}
	}
}