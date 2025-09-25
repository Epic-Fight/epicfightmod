package yesman.epicfight.world.capabilities.entitypatch.mob;

import java.util.Set;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class SpiderPatch<T extends PathfinderMob> extends MobPatch<T> {
	public SpiderPatch(T original) {
		super(original, Factions.NEUTRAL);
	}
	
	@Override
	protected void initAI() {
		super.initAI();
        this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, MobCombatBehaviors.SPIDER.build(this)));
        this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.original, 1.0D, false));
	}
	
	@Override
	protected void selectGoalToRemove(Set<Goal> toRemove) {
		super.selectGoalToRemove(toRemove);
		
		for (WrappedGoal wrappedGoal : this.original.goalSelector.getAvailableGoals()) {
			Goal goal = wrappedGoal.getGoal();
			
			if (goal instanceof LeapAtTargetGoal) {
				toRemove.add(goal);
			}
		}
	}
	
	@Override
	public void initAnimator(Animator animator) {
		super.initAnimator(animator);
		animator.addLivingAnimation(LivingMotions.DEATH, Animations.SPIDER_DEATH);
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.SPIDER_IDLE);
		animator.addLivingAnimation(LivingMotions.WALK, Animations.SPIDER_CRAWL);
	}

	@Override
	public void updateMotion(boolean considerInaction) {
		super.commonMobUpdateMotion(considerInaction);
	}

	@Override
	public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
		switch(stunType) {
		case SHORT:
			return Animations.SPIDER_HIT;
		case LONG:
			return Animations.SPIDER_HIT;
		case HOLD:
			return Animations.SPIDER_HIT;
		case KNOCKDOWN:
			return Animations.SPIDER_NEUTRALIZED;
		case NEUTRALIZE:
			return Animations.SPIDER_NEUTRALIZED;
		default:
			return null;
		}
	}

	@Override
	public SoundEvent getSwingSound(InteractionHand hand) {
		return SoundEvents.SPIDER_HURT;
	}
}