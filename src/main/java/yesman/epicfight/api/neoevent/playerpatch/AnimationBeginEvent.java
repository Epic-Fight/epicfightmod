package yesman.epicfight.api.neoevent.playerpatch;

import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class AnimationBeginEvent extends PlayerPatchEvent<PlayerPatch<?>> {
	private StaticAnimation animation;
	
	public AnimationBeginEvent(PlayerPatch<?> playerpatch, StaticAnimation animation) {
		super(playerpatch);
		
		this.animation = animation;
	}

	public StaticAnimation getAnimation() {
		return this.animation;
	}
}
