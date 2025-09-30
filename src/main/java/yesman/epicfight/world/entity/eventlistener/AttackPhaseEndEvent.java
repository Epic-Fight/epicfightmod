package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class AttackPhaseEndEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	private final AnimationAccessor<? extends AttackAnimation> animation;
	private final AttackAnimation.Phase phase;
	private final int phaseOrder;
	
	public AttackPhaseEndEvent(ServerPlayerPatch playerpatch, AnimationAccessor<? extends AttackAnimation> animation, AttackAnimation.Phase phase, int phaseOrder) {
		super(playerpatch, false);
		
		this.animation = animation;
		this.phase = phase;
		this.phaseOrder = phaseOrder;
	}

	public AnimationAccessor<? extends AttackAnimation> getAnimation() {
		return this.animation;
	}
	
	public AttackAnimation.Phase getPhase() {
		return this.phase;
	}
	
	public int getPhaseOrder() {
		return this.phaseOrder;
	}
}
