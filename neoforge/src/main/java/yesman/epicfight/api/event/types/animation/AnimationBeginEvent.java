package yesman.epicfight.api.event.types.animation;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Fired when [StaticAnimation] starts to play
/// Consider using [StartActionEvent] instead to save type-checking costs called everytime when an animation is played
public class AnimationBeginEvent extends LivingEntityPatchEvent {
	private final AnimationManager.AnimationAccessor<? extends StaticAnimation> animation;
	
	public AnimationBeginEvent(LivingEntityPatch<?> playerPatch, AnimationManager.AnimationAccessor<? extends StaticAnimation> animation) {
		super(playerPatch);
		
		this.animation = animation;
	}

	public AnimationManager.AnimationAccessor<? extends StaticAnimation> getAnimation() {
		return this.animation;
	}
}
