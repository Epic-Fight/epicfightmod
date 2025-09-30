package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class AttackEndEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	private AnimationAccessor<? extends AttackAnimation> animation;
	
	public AttackEndEvent(ServerPlayerPatch playerpatch, AnimationAccessor<? extends AttackAnimation> animation) {
		super(playerpatch, false);
		this.animation = animation;
	}

	public AnimationAccessor<? extends AttackAnimation> getAnimation() {
		return this.animation;
	}
}
