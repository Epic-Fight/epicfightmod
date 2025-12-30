package yesman.epicfight.api.animation.types;

import javax.annotation.Nullable;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;

public class DashAttackAnimation extends AttackAnimation {
	public DashAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, AnimationAccessor<? extends DashAttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		this(transitionTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature, false);
	}
	
	public DashAttackAnimation(float transitionTime, float antic, float preDelay, float contact, float recovery, @Nullable Collider collider, Joint colliderJoint, AnimationAccessor<? extends DashAttackAnimation> accessor, AssetAccessor<? extends Armature> armature, boolean directional) {
		this(transitionTime, accessor, armature, new Phase(0.0F, antic, preDelay, contact, recovery, Float.MAX_VALUE, colliderJoint, collider));
		
		if (directional) {
			this.addProperty(StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.COMBO_ATTACK_DIRECTION_MODIFIER);
		}
	}
	
	public DashAttackAnimation(float transitionTime, AnimationAccessor<? extends DashAttackAnimation> accessor, AssetAccessor<? extends Armature> armature, Phase... phases) {
		super(transitionTime, accessor, armature, phases);
		
		this.addProperty(AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.5F);
		this.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.4F));
		this.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.1F));
	}
	
	public DashAttackAnimation(float transitionTime, String path, AssetAccessor<? extends Armature> armature, Phase... phases) {
		super(transitionTime, path, armature, phases);
		
		this.addProperty(AttackAnimationProperty.ATTACK_SPEED_FACTOR, 0.5F);
		this.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.4F));
		this.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.1F));
	}
	
	@Override
	protected void bindPhaseState(Phase phase) {
		this.stateSpectrumBlueprint
			.newTimePair(phase.start, phase.preDelay)
			.addState(EntityState.PHASE_LEVEL, 1)
			.newTimePair(phase.start, phase.contact)
			.addState(EntityState.SKILL_EXECUTABLE, false)
			.addState(EntityState.COMBO_ATTACKS_DOABLE, false)
			.newTimePair(phase.start, phase.recovery)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.UPDATE_LIVING_MOTION, false)
			.newTimePair(phase.start, phase.end)
			.addState(EntityState.INACTION, true)
			.newTimePair(phase.antic, phase.end)
			.addState(EntityState.TURNING_LOCKED, true)
			.newTimePair(phase.preDelay, phase.contact)
			.addState(EntityState.ATTACKING, true)
			.addState(EntityState.PHASE_LEVEL, 2)
			.newTimePair(phase.contact, phase.recovery)
			.addState(EntityState.COMBO_ATTACKS_DOABLE, false)
			.newTimePair(phase.contact, phase.end)
			.addState(EntityState.PHASE_LEVEL, 3)
			;
	}
	
	@Override
	public boolean isComboAttackAnimation() {
		return true;
	}
}