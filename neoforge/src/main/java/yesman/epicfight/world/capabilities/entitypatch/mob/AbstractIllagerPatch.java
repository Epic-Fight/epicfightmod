package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.entity.PathfinderMob;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

public abstract class AbstractIllagerPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> {
	public AbstractIllagerPatch(T original, Faction faction) {
		super(original, faction);	
	}
	
	@Override
	public void initAnimator(Animator animator) {
		super.initAnimator(animator);
		
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.ILLAGER_IDLE);
		animator.addLivingAnimation(LivingMotions.WALK, Animations.ILLAGER_WALK);
		animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
	}
}