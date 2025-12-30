package yesman.epicfight.api.event.types.player;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ModifyComboCounter extends LivingEntityPatchEvent implements CancelableEvent {
	private final Causal causal;
	private final AnimationAccessor<? extends StaticAnimation> animation;
	private final int prevValue;
	private int nextValue;
	
	public ModifyComboCounter(Causal causal, ServerPlayerPatch playerPatch, AnimationAccessor<? extends StaticAnimation> animation, int prevValue, int nextValue) {
        super(playerPatch);

		this.causal = causal;
		this.animation = animation;
		this.prevValue = prevValue;
		this.nextValue = nextValue;
	}
	
	public Causal getCausal() {
		return this.causal;
	}

    public ServerPlayerPatch getPlayerPatch() {
        return (ServerPlayerPatch)this.getEntityPatch();
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