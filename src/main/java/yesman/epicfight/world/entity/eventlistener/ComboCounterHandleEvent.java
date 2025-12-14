package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ComboCounterHandleEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	private final ComboCounterHandleEvent.Causal causal;
	private final AnimationAccessor<? extends StaticAnimation> animation;
	private final int prevValue;
	private int nextValue;
	
	public ComboCounterHandleEvent(ComboCounterHandleEvent.Causal causal, ServerPlayerPatch playerpatch, AnimationAccessor<? extends StaticAnimation> animation, int prevValue, int nextValue) {
		super(playerpatch, true);
		
		this.causal = causal;
		this.animation = animation;
		this.prevValue = prevValue;
		this.nextValue = nextValue;
	}
	
	public ComboCounterHandleEvent.Causal getCausal() {
		return this.causal;
	}
	
	public AnimationAccessor<? extends StaticAnimation> getAnimation() {
		return this.animation;
	}
	
	public int getPrevValue() {
		return this.prevValue;
	}
	
	public int getNextValue() {
		return this.nextValue;
	}
	
	public void setNextValue(int nextValue) {
		this.nextValue = nextValue;
	}
	
	public enum Causal {
		ANOTHER_ACTION_ANIMATION, TIME_EXPIRED
	}
}