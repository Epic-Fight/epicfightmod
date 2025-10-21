package yesman.epicfight.skill.common;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import yesman.epicfight.api.neoevent.playerpatch.ComboAttackEvent;
import yesman.epicfight.api.neoevent.playerpatch.ComboCounterHandleEvent;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.api.neoevent.playerpatch.SkillConsumeEvent;
import yesman.epicfight.api.neoevent.playerpatch.StartActionEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AbstractAnimatorControl;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class ComboAttacks extends Skill {
	/** Decides if the animation used for combo attack **/
	public static final IndependentVariableKey<Boolean> COMBO = AnimationVariables.unsyncIndependent(animator -> false, false);
	
	public static SkillBuilder<?> createComboAttackBuilder() {
		return new SkillBuilder<>(ComboAttacks::new).setCategory(SkillCategories.BASIC_ATTACK).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.NONE);
	}
	
	public static void setComboCounterWithEvent(ComboCounterHandleEvent.Causal reason, ServerPlayerPatch playerpatch, SkillContainer container, AnimationAccessor<? extends StaticAnimation> causalAnimation, int value) {
		int prevValue = container.getDataManager().getDataValue(EpicFightSkillDataKeys.COMBO_COUNTER);
		ComboCounterHandleEvent comboResetEvent = new ComboCounterHandleEvent(reason, playerpatch, causalAnimation, prevValue, value);
		PlayerPatchEvent.postAndFireSkillListeners(comboResetEvent);
		container.getDataManager().setData(EpicFightSkillDataKeys.COMBO_COUNTER, comboResetEvent.getNextValue());
	}
	
	public ComboAttacks(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void actionEvent(StartActionEvent event, SkillContainer skillContainer) {
		event.runOnServer(serverplayerpatch -> {
			if (event.getAnimation().get().getProperty(ActionAnimationProperty.RESET_PLAYER_COMBO_COUNTER).orElse(true)) {
				CapabilityItem itemCapability = serverplayerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
				List<AnimationAccessor<? extends AttackAnimation>> comboAnimations = itemCapability.getAutoAttackMotion(skillContainer.getExecutor());
				
				if (comboAnimations == null) {
					return;
				}
				
				Set<AnimationAccessor<? extends AttackAnimation>> attackMotionSet = Set.copyOf(comboAnimations.stream().collect(Collectors.toSet()));
				
				if (!attackMotionSet.contains(event.getAnimation()) && itemCapability.shouldCancelCombo(serverplayerpatch)) {
					setComboCounterWithEvent(ComboCounterHandleEvent.Causal.ANOTHER_ACTION_ANIMATION, serverplayerpatch, skillContainer, event.getAnimation(), 0);
				}
			}
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
		SkillConsumeEvent event = new SkillConsumeEvent(executor, this, this.resource, null);
		
		if (!PlayerPatchEvent.postAndFireSkillListeners(event).isCanceled()) {
			event.getResourceType().consumer.consume(skillContainer, executor, event.getAmount());
		}
		
		if (PlayerPatchEvent.postAndFireSkillListeners(new ComboAttackEvent(executor)).isCanceled()) {
			return;
		}
		
		CapabilityItem cap = executor.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		AnimationAccessor<? extends AttackAnimation> attackMotion = null;
		ServerPlayer player = executor.getOriginal();
		SkillDataManager dataManager = skillContainer.getDataManager();
		int comboCounter = dataManager.getDataValue(EpicFightSkillDataKeys.COMBO_COUNTER);
		
		if (player.isPassenger()) {
			Entity entity = player.getVehicle();
			
			if ((entity instanceof PlayerRideableJumping ridable && ridable.canJump()) && cap.availableOnHorse() && cap.getMountAttackMotion() != null) {
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
			boolean dashAttack = player.isSprinting();
			
			if (dashAttack) {
				comboCounter = comboSize - 2;
			} else {
				comboCounter %= comboSize - 2;
			}
			
			attackMotion = combo.get(comboCounter);
			comboCounter = dashAttack ? 0 : comboCounter + 1;
		}
		
		setComboCounterWithEvent(ComboCounterHandleEvent.Causal.ANOTHER_ACTION_ANIMATION, executor, skillContainer, attackMotion, comboCounter);
		
		if (attackMotion != null) {
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
				setComboCounterWithEvent(ComboCounterHandleEvent.Causal.TIME_EXPIRED, serverplayerpatch, container, null, 0);
			}
		});
	}
}
