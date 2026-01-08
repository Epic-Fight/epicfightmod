package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ActionEvent<T extends PlayerPatch<?>> extends AbstractPlayerEvent<T> {
	private final AnimationAccessor<? extends MainFrameAnimation> actionAnimation;
	private boolean resetActionTick;
	
	@SuppressWarnings("unchecked")
	public ActionEvent(PlayerPatch<?> playerdata, AnimationAccessor<? extends MainFrameAnimation> actionAnimation) {
		super((T)playerdata, false);
		
		this.actionAnimation = actionAnimation;
		this.resetActionTick = true;
	}
	
	public AnimationAccessor<? extends MainFrameAnimation> getAnimation() {
		return this.actionAnimation;
	}
	
	public void resetActionTick(boolean flag) {
		this.resetActionTick = flag;
	}
	
	public boolean shouldResetActionTick() {
		return this.resetActionTick;
	}
}