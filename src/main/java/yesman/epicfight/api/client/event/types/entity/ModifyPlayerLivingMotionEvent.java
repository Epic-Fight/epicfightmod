package yesman.epicfight.api.client.event.types.entity;

import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

public abstract class ModifyPlayerLivingMotionEvent extends LivingEntityPatchEvent {
	private LivingMotion motion;
	
	public ModifyPlayerLivingMotionEvent(AbstractClientPlayerPatch<?> playerpatch, LivingMotion motion) {
		super(playerpatch);
		this.motion = motion;
	}
	
	public void setMotion(LivingMotion livingmotion) {
		this.motion = livingmotion;
	}
	
	public LivingMotion getMotion() {
		return this.motion;
	}

    public AbstractClientPlayerPatch<?> getPlayerPatch() {
        return (AbstractClientPlayerPatch<?>)this.getEntityPatch();
    }

	public static class BaseLayer extends ModifyPlayerLivingMotionEvent {
		private final boolean inaction;
		
		public BaseLayer(AbstractClientPlayerPatch<?> playerpatch, LivingMotion motion, boolean inaction) {
			super(playerpatch, motion);
			
			this.inaction = inaction;
		}
		
		public boolean inaction() {
			return this.inaction;
		}
	}
	
	public static class CompositeLayer extends ModifyPlayerLivingMotionEvent {
		public CompositeLayer(AbstractClientPlayerPatch<?> playerpatch, LivingMotion motion) {
			super(playerpatch, motion);
		}
	}
}