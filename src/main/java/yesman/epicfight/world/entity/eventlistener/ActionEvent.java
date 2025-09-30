package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ActionEvent<T extends PlayerPatch<?>> extends AbstractPlayerEvent<T> {
	private final AnimationAccessor<? extends StaticAnimation> actionAnimation;
	private boolean resetActionTick;
	
	@SuppressWarnings("unchecked")
	public ActionEvent(PlayerPatch<?> playerdata, AnimationAccessor<? extends StaticAnimation> actionAnimation) {
		super((T)playerdata, false);
		
		this.actionAnimation = actionAnimation;
		this.resetActionTick = true;
	}
	
	public AnimationAccessor<? extends StaticAnimation> getAnimation() {
		return this.actionAnimation;
	}
	
	public void resetActionTick(boolean flag) {
		this.resetActionTick = flag;
	}
	
	public boolean shouldResetActionTick() {
		return this.resetActionTick;
	}
}