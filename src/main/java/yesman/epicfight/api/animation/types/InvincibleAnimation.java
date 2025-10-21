package yesman.epicfight.api.animation.types;

import net.minecraft.tags.DamageTypeTags;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;

public class InvincibleAnimation extends ActionAnimation {
	public InvincibleAnimation(float convertTime, AnimationAccessor<? extends InvincibleAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		super(convertTime, accessor, armature);
		
		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, Float.MAX_VALUE)
			.addState(EntityState.TURNING_LOCKED, true)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.UPDATE_LIVING_MOTION, false)
			.addState(EntityState.COMBO_ATTACKS_DOABLE, false)
			.addState(EntityState.SKILL_EXECUTABLE, true)
			.addState(EntityState.INACTION, true)
			.addState(EntityState.ATTACK_RESULT, (damagesource) -> damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? AttackResult.ResultType.BLOCKED : AttackResult.ResultType.SUCCESS);
	}
}