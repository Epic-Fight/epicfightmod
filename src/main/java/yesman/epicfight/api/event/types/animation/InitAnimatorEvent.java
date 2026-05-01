package yesman.epicfight.api.event.types.animation;

import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class InitAnimatorEvent extends LivingEntityPatchEvent {
	private final Animator animator;
	
	public InitAnimatorEvent(LivingEntityPatch<?> entityPatch, Animator animator) {
		super(entityPatch);
		this.animator = animator;
	}
	
	public Animator getAnimator() {
		return this.animator;
	}
}