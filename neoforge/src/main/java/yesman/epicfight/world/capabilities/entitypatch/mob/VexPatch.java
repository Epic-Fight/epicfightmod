package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Vex;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

public class VexPatch extends MobPatch<Vex> {
	public VexPatch(Vex original) {
		super(original, Factions.ILLAGER);
	}
	
	@Override
	protected void initAI() {
		super.initAI();
		
        this.original.goalSelector.addGoal(0, new ChargeAttackGoal());
	}
	
	@Override
	protected void selectGoalToRemove(Set<Goal> toRemove) {
		super.selectGoalToRemove(toRemove);
		
		Iterator<WrappedGoal> iterator = this.original.goalSelector.getAvailableGoals().iterator();
		
		int index = 0;
		while (iterator.hasNext()) {
			WrappedGoal goal = iterator.next();
			Goal inner = goal.getGoal();
			
			if (index == 1) {
				toRemove.add(inner);
				break;
			}
			
			index++;
        }
	}
	
	@Override
	public void preTickServer() {
		super.preTickServer();
		
		if (this.getEntityState().movementLocked()) {
			this.original.goalSelector.disableControlFlag(Goal.Flag.MOVE);
			this.original.goalSelector.disableControlFlag(Goal.Flag.JUMP);
		} else {
			this.original.goalSelector.enableControlFlag(Goal.Flag.MOVE);
			this.original.goalSelector.enableControlFlag(Goal.Flag.JUMP);
		}
		
		if (this.getEntityState().turningLocked()) {
			this.original.goalSelector.disableControlFlag(Goal.Flag.LOOK);
		} else {
			this.original.goalSelector.enableControlFlag(Goal.Flag.LOOK);
		}
	}
	
	@Override
	public void initAnimator(Animator animator) {
		super.initAnimator(animator);
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.VEX_IDLE);
		animator.addLivingAnimation(LivingMotions.DEATH, Animations.VEX_DEATH);
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.VEX_FLIPPING);
	}
	
	@Override
	public void updateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.INACTION;
		} else {
			currentLivingMotion = LivingMotions.IDLE;
			currentCompositeMotion = LivingMotions.IDLE;
		}
	}
	
	@Override
	public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> opponent) {
		this.original.setPos(opponent.getOriginal().getEyePosition().add(opponent.getOriginal().getLookAngle()));
		this.playAnimationSynchronized(Animations.VEX_NEUTRALIZED, 0.0F);
	}
	
	@Override
	public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
		return Animations.VEX_HIT;
	}
	
	class ChargeAttackGoal extends Goal {
		private int chargingCounter;
		
		public ChargeAttackGoal() {
			this.setFlags(EnumSet.of(Flag.MOVE));
		}
		
		@Override
		public boolean canUse() {
			if (VexPatch.this.original.getTarget() != null && !VexPatch.this.getEntityState().inaction() && VexPatch.this.original.getRandom().nextInt(10) == 0) {
				double distance = VexPatch.this.original.distanceToSqr(VexPatch.this.original.getTarget());
				return distance < 49.0D;
			} else {
				return false;
			}
		}
	    
		@Override
		public boolean canContinueToUse() {
			return this.chargingCounter > 0;
		}
		
		@Override
		public void start() {
			VexPatch.this.original.getMoveControl().setWantedPosition(VexPatch.this.original.getX(), VexPatch.this.original.getY(), VexPatch.this.original.getZ(), 0.25F);
	    	VexPatch.this.playAnimationSynchronized(Animations.VEX_CHARGE, 0.0F);
	    	VexPatch.this.original.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
	    	VexPatch.this.original.setIsCharging(true);
	    	this.chargingCounter = 20;
	    }
	    
		@Override
		public void stop() {
			VexPatch.this.original.setIsCharging(false);
		}
		
		@Override
		public void tick() {
			--this.chargingCounter;
		}
	}
}