package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class AnimationBeginEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private StaticAnimation animation;
	
	public AnimationBeginEvent(PlayerPatch<?> playerpatch, StaticAnimation animation) {
		super(playerpatch, false);
		
		this.animation = animation;
	}

	public StaticAnimation getAnimation() {
		return this.animation;
	}
}
