package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class AnimationEndEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private StaticAnimation animation;
	private boolean isEnd;
	
	public AnimationEndEvent(PlayerPatch<?> playerpatch, StaticAnimation animation, boolean isEnd) {
		super(playerpatch, false);
		
		this.animation = animation;
		this.isEnd = isEnd;
	}

	public StaticAnimation getAnimation() {
		return this.animation;
	}
	
	public boolean isEnd() {
		return this.isEnd;
	}
}
