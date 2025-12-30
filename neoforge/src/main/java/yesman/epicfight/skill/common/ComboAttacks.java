package yesman.epicfight.skill.common;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.AnimationVariables.IndependentVariableKey;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.ComboAttackEvent;
import yesman.epicfight.api.event.types.player.ModifyComboCounter;
import yesman.epicfight.api.event.types.player.SkillConsumeEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AbstractAnimatorControl;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import java.util.List;
import java.util.Set;

public class ComboAttacks extends Skill {
	/// Decides if the animation used for combo attack
	public static final IndependentVariableKey<Boolean> COMBO = AnimationVariables.unsyncIndependent(animator -> false, false);
	
	public static SkillBuilder<?> createComboAttackBuilder() {
		return new SkillBuilder<>(ComboAttacks::new).setCategory(SkillCategories.BASIC_ATTACK).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
	}
	
	public static void setComboCounterWithEvent(ModifyComboCounter.Causal reason, ServerPlayerPatch playerpatch, SkillContainer container, AnimationAccessor<? extends StaticAnimation> causalAnimation, int value) {
		int prevValue = container.getDataManager().getDataValue(EpicFightSkillDataKeys.COMBO_COUNTER);
        ModifyComboCounter comboResetEvent = new ModifyComboCounter(reason, playerpatch, causalAnimation, prevValue, value);
        EpicFightEventHooks.Player.MODIFY_COMBO_COUNTER.postWithListener(comboResetEvent, playerpatch.getEventListener());
		container.getDataManager().setData(EpicFightSkillDataKeys.COMBO_COUNTER, comboResetEvent.getNextValue());
	}

    /// Consumption amount when basic attacks set to use stamina
    private float dashAttackConsumption = 0f;
    private float airAttackConsumption = 0f;

	public ComboAttacks(SkillBuilder<?> builder) {
		super(builder);
	}

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        this.dashAttackConsumption = parameters.getFloat("dash_attack_consumption");
        this.airAttackConsumption = parameters.getFloat("air_attack_consumption");
    }

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        skillContainer.runOnServer(playerpatch -> {
            eventListener.registerEvent(
                EpicFightEventHooks.Animation.START_ACTION,
                event -> {
                    if (event.getAnimation().get().getProperty(ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER).orElse(true)) {
                        CapabilityItem itemCapability = playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
                        List<AnimationAccessor<? extends AttackAnimation>> comboAnimations = itemCapability.getAutoAttackMotion(skillContainer.getExecutor());

                        if (comboAnimations == null) {
                            return;
                        }

                        Set<AnimationAccessor<? extends AttackAnimation>> attackMotionSet = Set.copyOf(Sets.newHashSet(comboAnimations));

                        if (!attackMotionSet.contains(event.getAnimation()) && itemCapability.shouldCancelCombo(playerpatch)) {
                            setComboCounterWithEvent(ModifyComboCounter.Causal.ANOTHER_ACTION_ANIMATION, playerpatch, skillContainer, event.getAnimation(), 0);
                        }
                    }
                },
                this
            );
        });
    }

	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		EntityState playerState = executor.getEntityState();
		Player player = executor.getOriginal();
		return !(player.isSpectator() || executor.isInAir() || !playerState.canBasicAttack());
	}

	@Override
	public void executeOnServer(SkillContainer skillContainer, CompoundTag args) {
		ServerPlayerPatch executor = skillContainer.getServerExecutor();
		SkillConsumeEvent skillConsumeEvent = new SkillConsumeEvent(executor, this, this.resource, null);
        EpicFightEventHooks.Player.CONSUME_SKILL.postWithListener(skillConsumeEvent, executor.getEventListener());

		if (!skillConsumeEvent.isCanceled()) {
			skillConsumeEvent.getResourceType().consumer.consume(skillContainer, executor, skillConsumeEvent.getAmount());
		}

        ComboAttackEvent comboAttackEvent = new ComboAttackEvent(executor);
        EpicFightEventHooks.Player.COMBO_ATTACK.postWithListener(comboAttackEvent, executor.getEventListener());

		if (comboAttackEvent.isCanceled()) {
			return;
		}
		
		CapabilityItem cap = executor.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		AnimationAccessor<? extends AttackAnimation> attackMotion = null;
		ServerPlayer player = executor.getOriginal();
		SkillDataManager dataManager = skillContainer.getDataManager();
		int comboCounter = dataManager.getDataValue(EpicFightSkillDataKeys.COMBO_COUNTER);
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
            } else if (dashAttack) {
                comboCounter = comboSize - 2;
            } else {
                comboCounter %= comboSize - 2;
            }

            attackMotion = combo.get(comboCounter);
            comboCounter = (dashAttack || airAttack) ? 0 : comboCounter + 1;
		}
		
		setComboCounterWithEvent(ModifyComboCounter.Causal.ANOTHER_ACTION_ANIMATION, executor, skillContainer, attackMotion, comboCounter);

        if (attackMotion != null && this.checkConsumption(skillContainer, dashAttack, airAttack)) {
            // Remove an existing data
			executor.getAnimator().getVariables().put(COMBO, attackMotion, true);
			executor.getAnimator().playAnimation(attackMotion, 0.0F);
			
			boolean stiffAttack = EpicFightGameRules.STIFF_COMBO_ATTACKS.getRuleValue(executor.getOriginal().level());
			SPAnimatorControl animatorControlPacket;
			
			if (stiffAttack) {
				animatorControlPacket = new SPAnimatorControl(AbstractAnimatorControl.Action.PLAY, attackMotion, skillContainer.getExecutor(), 0.0F);
			} else {
				animatorControlPacket = new SPAnimatorControl(AbstractAnimatorControl.Action.PLAY_CLIENT, attackMotion, skillContainer.getExecutor(), 0.0F, AbstractAnimatorControl.Layer.COMPOSITE_LAYER, AbstractAnimatorControl.Priority.HIGHEST);
			}
			
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(animatorControlPacket, player);
		}
		
		executor.updateEntityState();
	}

	@Override
	public void updateContainer(SkillContainer container) {
		container.runOnServer(serverplayerpatch -> {
			if (container.getExecutor().getTickSinceLastAction() > 16 && container.getDataManager().getDataValue(EpicFightSkillDataKeys.COMBO_COUNTER) > 0) {
                    setComboCounterWithEvent(ModifyComboCounter.Causal.TIME_EXPIRED, serverplayerpatch, container, null, 0);
			}
		});
	}

    /// Checks the consumption of the skill based on dash, air attack states
    protected boolean checkConsumption(SkillContainer container, boolean dash, boolean air) {
        float finalConsumption = air ? this.airAttackConsumption : this.dashAttackConsumption;

        if (this.resource == Resource.STAMINA) {
            finalConsumption = container.getExecutor().getModifiedStaminaConsume(finalConsumption);
        }

        if (air || dash) {
            return container.getExecutor().consumeForSkill(this, this.resource, finalConsumption);
        } else {
            return container.getExecutor().consumeForSkill(this, this.resource);
        }
    }
}
