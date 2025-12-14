package yesman.epicfight.api.client.forgeevent;

import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.world.entity.eventlistener.DetachablePlayerEvent;

public abstract class UpdatePlayerMotionEvent extends Event implements DetachablePlayerEvent<AbstractClientPlayerPatch<?>> {
	private final AbstractClientPlayerPatch<?> playerpatch;
	private LivingMotion motion;
	
	public UpdatePlayerMotionEvent(AbstractClientPlayerPatch<?> playerpatch, LivingMotion motion) {
		this.playerpatch = playerpatch;
		this.motion = motion;
	}
	
	@Override
	public AbstractClientPlayerPatch<?> getPlayerPatch() {
		return this.playerpatch;
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