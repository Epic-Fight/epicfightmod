package yesman.epicfight.api.client.neoevent;

import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

public abstract class UpdatePlayerMotionEvent extends PlayerPatchEvent<AbstractClientPlayerPatch<?>> {
	private LivingMotion motion;
	
	public UpdatePlayerMotionEvent(AbstractClientPlayerPatch<?> playerpatch, LivingMotion motion) {
		super(playerpatch);
		this.motion = motion;
	}
	
	public void setMotion(LivingMotion livingmotion) {
		this.motion = livingmotion;
	}
	
	public LivingMotion getMotion() {
		return this.motion;
	}
	
	public static class BaseLayer extends UpdatePlayerMotionEvent {
		private final boolean inaction;
		
		public BaseLayer(AbstractClientPlayerPatch<?> playerpatch, LivingMotion motion, boolean inaction) {
			super(playerpatch, motion);
			
			this.inaction = inaction;
		}
		
		public boolean inaction() {
			return this.inaction;
		}
	}
	
	public static class CompositeLayer extends UpdatePlayerMotionEvent {
		public CompositeLayer(AbstractClientPlayerPatch<?> playerpatch, LivingMotion motion) {
			super(playerpatch, motion);
		}
	}
}