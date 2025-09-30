package yesman.epicfight.api.animation.types;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.SimpleEvent;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.DodgeLocationIndicator;

public class DodgeAnimation extends ActionAnimation {
	public static final Function<DamageSource, AttackResult.ResultType> DODGEABLE_SOURCE_VALIDATOR = (damagesource) -> {
		if (damagesource.getEntity() != null
			&& !damagesource.is(DamageTypeTags.IS_EXPLOSION)
			&& !damagesource.is(DamageTypes.MAGIC)
			&& !damagesource.is(DamageTypeTags.BYPASSES_ARMOR)
			&& !damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
			&& !damagesource.is(EpicFightDamageTypeTags.BYPASS_DODGE)
		) {
			return AttackResult.ResultType.MISSED;
		}
		
		return AttackResult.ResultType.SUCCESS;
	};
	
	public static final Consumer<ProjectileImpactEvent> IGNORE_ALL_PROJECTILES = (event) -> {
		event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
	};
	
	public DodgeAnimation(float transitionTime, AnimationAccessor<? extends DodgeAnimation> accessor, float width, float height, AssetAccessor<? extends Armature> armature) {
		this(transitionTime, 10.0F, accessor, width, height, armature);
	}
	
	public DodgeAnimation(float transitionTime, float delayTime, AnimationAccessor<? extends DodgeAnimation> accessor, float width, float height, AssetAccessor<? extends Armature> armature) {
		super(transitionTime, delayTime, accessor, armature);
		
		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, delayTime)
			.addState(EntityState.TURNING_LOCKED, true)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.UPDATE_LIVING_MOTION, false)
			.addState(EntityState.CAN_BASIC_ATTACK, false)
			.addState(EntityState.CAN_SKILL_EXECUTION, false)
			.addState(EntityState.INACTION, true)
			.newTimePair(0.0F, Float.MAX_VALUE)
			.addState(EntityState.ATTACK_RESULT, DODGEABLE_SOURCE_VALIDATOR)
			.addState(EntityState.PROJECTILE_IMPACT_RESULT, IGNORE_ALL_PROJECTILES);
		
		
		this.addProperty(ActionAnimationProperty.AFFECT_SPEED, true);
		this.addEvents(StaticAnimationProperty.ON_END_EVENTS, SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH));
		this.addEvents(StaticAnimationProperty.TICK_EVENTS, SimpleEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH).params(EntityDimensions.scalable(width, height)));
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		
		if (!entitypatch.isLogicalClient() && entitypatch != null) {
			entitypatch.getOriginal().level().addFreshEntity(new DodgeLocationIndicator(entitypatch));
		}
	}
}