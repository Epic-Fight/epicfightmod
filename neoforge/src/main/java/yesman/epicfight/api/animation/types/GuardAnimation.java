package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;

public class GuardAnimation extends MainFrameAnimation {
	public GuardAnimation(float transitionTime, AnimationAccessor<? extends GuardAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		this(transitionTime, Float.MAX_VALUE, accessor, armature);
	}
	
	public GuardAnimation(float transitionTime, float lockTime, AnimationAccessor<? extends GuardAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		super(transitionTime, accessor, armature);
		
		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, lockTime)
			.addState(EntityState.TURNING_LOCKED, true)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.UPDATE_LIVING_MOTION, false)
			.addState(EntityState.COMBO_ATTACKS_DOABLE, false)
			.newTimePair(0.0F, Float.MAX_VALUE)
			.addState(EntityState.INACTION, true);
	}
}