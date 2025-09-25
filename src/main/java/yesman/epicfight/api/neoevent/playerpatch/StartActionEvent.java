package yesman.epicfight.api.neoevent.playerpatch;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class StartActionEvent extends PlayerPatchEvent<PlayerPatch<?>> {
	private final AnimationAccessor<? extends StaticAnimation> actionAnimation;
	private boolean resetActionTick;
	
	public StartActionEvent(PlayerPatch<?> playerpatch, AnimationAccessor<? extends StaticAnimation> actionAnimation) {
		super(playerpatch);
		
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