package yesman.epicfight.network.common;

import javax.annotation.Nullable;

import yesman.epicfight.api.animation.AnimationVariables.IndependentAnimationVariableKey;
import yesman.epicfight.api.animation.AnimationVariables.SharedAnimationVariableKey;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class AnimationVariablePacket<T> {
	protected AssetAccessor<? extends StaticAnimation> animation;
	protected SynchedAnimationVariableKey<T> animationVariableKey;
	protected T value;
	protected Action action;
	
	public AnimationVariablePacket(SynchedAnimationVariableKey<T> animationVariableKey, @Nullable AssetAccessor<? extends StaticAnimation> animation, T value, Action action) {
		this.animationVariableKey = animationVariableKey;
		this.value = value;
		this.action = action;
		this.animation = animation;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void process(LivingEntityPatch<?> entitypatch) {
		switch (this.action) {
		case PUT -> {
			if (this.animationVariableKey.isSharedKey()) {
				entitypatch.getAnimator().getVariables().putSharedVariable((SharedAnimationVariableKey<T>)this.animationVariableKey, this.value, false);
			} else {
				entitypatch.getAnimator().getVariables().put((IndependentAnimationVariableKey<T>)this.animationVariableKey, this.animation, this.value, false);
			}
		}
		case REMOVE -> {
			if (this.animationVariableKey.isSharedKey()) {
				entitypatch.getAnimator().getVariables().removeSharedVariable((SharedAnimationVariableKey<T>)this.animationVariableKey, false);
			} else {
				entitypatch.getAnimator().getVariables().remove((IndependentAnimationVariableKey<T>)this.animationVariableKey, this.animation, false);
			}
		}
		}
	}
	
	public enum Action {
		PUT, REMOVE
	}
}
