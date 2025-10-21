package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class WitchPatch extends HumanoidMobPatch<Witch> {
	public WitchPatch(Witch witch) {
		super(witch, Factions.NEUTRAL);
	}
	
	@Override
	public void setAIAsInfantry(boolean holdingRanedWeapon) {
		this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, MobCombatBehaviors.WITCH.build(this)));
		this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.getOriginal(), 1.0F, true, 10.0D));
	}
	
	@Override
	public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
	}
	
	@Override
	public void initAnimator(Animator animator) {
		super.initAnimator(animator);
		animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.ILLAGER_IDLE);
		animator.addLivingAnimation(LivingMotions.WALK, Animations.ILLAGER_WALK);
		animator.addLivingAnimation(LivingMotions.DRINK, Animations.WITCH_DRINKING);
	}
	
	@Override
	public void updateMotion(boolean considerInaction) {
		super.commonMobUpdateMotion(considerInaction);
		
		if (this.original.isDrinkingPotion()) {
			this.currentCompositeMotion = LivingMotions.DRINK;
		}
	}
}