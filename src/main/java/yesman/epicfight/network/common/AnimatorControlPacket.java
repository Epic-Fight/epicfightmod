package yesman.epicfight.network.common;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class AnimatorControlPacket {
	protected Action action;
	protected int animationId;
	protected float transitionTimeModifier;
	protected boolean pause;
	
	public AnimatorControlPacket(Action action, int animationId, float transitionTimeModifier, boolean pause) {
		this.action = action;
		this.animationId = animationId;
		this.transitionTimeModifier = transitionTimeModifier;
		this.pause = pause;
	}
	
	public <T extends SPAnimatorControl> void process(LivingEntityPatch<?> entitypatch) {
		try {
			switch (this.action) {
			case PLAY -> {
				entitypatch.getAnimator().playAnimation(this.animationId, this.transitionTimeModifier);
			}
			case PLAY_INSTANTLY -> {
				entitypatch.getAnimator().playAnimationInstantly(this.animationId);
			}
			case RESERVE -> {
				entitypatch.getAnimator().reserveAnimation(this.animationId);
			}
			case STOP -> {
				entitypatch.getAnimator().stopPlaying(this.animationId);
			}
			case SHOT -> {
				entitypatch.getAnimator().playShootingAnimation();
			}
			case SOFT_PAUSE -> {
				entitypatch.getAnimator().setSoftPause(this.pause);
			}
			case HARD_PAUSE -> {
				entitypatch.getAnimator().setHardPause(this.pause);
			}
			}
		} catch (Exception e) {
			// print out exceptions since any exceptions that occurred in the packet queue won't be printed out
			e.printStackTrace();
		}
	}
	
	public enum Action {
		PLAY, PLAY_CLIENT, PLAY_INSTANTLY, RESERVE, STOP, SHOT, SOFT_PAUSE, HARD_PAUSE
	}
	
	public enum Layer {
		ANIMATION, BASE_LAYER, COMPOSITE_LAYER;
	}
	
	public enum Priority {
		ANIMATION, LOWEST, LOW, MIDDLE, HIGH, HIGHEST;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static yesman.epicfight.api.client.animation.Layer.Priority getPriority(Priority priority) {
		switch (priority) {
		case LOWEST -> {
			return yesman.epicfight.api.client.animation.Layer.Priority.LOWEST;
		}
		case LOW -> {
			return yesman.epicfight.api.client.animation.Layer.Priority.LOW;
		}
		case MIDDLE -> {
			return yesman.epicfight.api.client.animation.Layer.Priority.MIDDLE;
		}
		case HIGH -> {
			return yesman.epicfight.api.client.animation.Layer.Priority.HIGH;
		}
		case HIGHEST-> {
			return yesman.epicfight.api.client.animation.Layer.Priority.HIGHEST;
		}
		}
		
		return null;
	}
}
