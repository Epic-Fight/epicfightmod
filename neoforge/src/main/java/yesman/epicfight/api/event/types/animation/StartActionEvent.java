package yesman.epicfight.api.event.types.animation;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Fired when [ActionAnimation] starts to play
public class StartActionEvent extends LivingEntityPatchEvent {
	private final AnimationAccessor<? extends MainFrameAnimation> animation;
	private boolean resetActionTick;
	
	public StartActionEvent(LivingEntityPatch<?> entityPatch, AnimationAccessor<? extends MainFrameAnimation> actionAnimation) {
		super(entityPatch);
		
		this.animation = actionAnimation;
		this.resetActionTick = true;
	}
	
	public AnimationAccessor<? extends MainFrameAnimation> getAnimation() {
		return this.animation;
	}

	public void resetActionTick(boolean flag) {
		this.resetActionTick = flag;
	}
	
	public boolean shouldResetActionTick() {
		return this.resetActionTick;
	}
}