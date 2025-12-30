package yesman.epicfight.api.event.types.entity;

import net.minecraft.world.damagesource.DamageSource;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class TakeDamageEvent extends LivingEntityPatchEvent {
	protected final DamageSource damageSource;
	protected final float damage;

	private TakeDamageEvent(LivingEntityPatch<?> entityPatch, DamageSource damageSource, float damage) {
		super(entityPatch);
		this.damageSource = damageSource;
		this.damage = damage;
	}

	public DamageSource getDamageSource() {
		return this.damageSource;
	}

	/// For Income and Pre, it's base damage
	/// For Post, it's modified damage
	public float getDamage() {
		return this.damage;
	}

	public static final class Income extends TakeDamageEvent implements CancelableEvent {
		private boolean parried;
        private AttackResult.ResultType result;

		public Income(LivingEntityPatch<?> entityPatch, DamageSource damageSource, float baseDamage) {
			super(entityPatch, damageSource, baseDamage);
			
			this.parried = false;
			this.result = AttackResult.ResultType.SUCCESS;
		}

		public AttackResult.ResultType getResult() {
			return this.result;
		}

		public void setResult(AttackResult.ResultType result) {
			this.result = result;
		}

		public boolean isParried() {
			return this.parried;
		}

		public void setParried(boolean parried) {
			this.parried = parried;
		}
	}

	public static final class Pre extends TakeDamageEvent {
		private final ValueModifier.ResultCalculator calculator;

		public Pre(LivingEntityPatch<?> entityPatch, DamageSource damageSource, ValueModifier.ResultCalculator calculator, float baseDamage) {
			super(entityPatch, damageSource, baseDamage);
			
			this.calculator = calculator;
		}

		public void attachValueModifier(ValueModifier valueModifier) {
			this.calculator.attach(valueModifier);
		}
	}

	public static final class Post extends TakeDamageEvent {
		public Post(LivingEntityPatch<?> entityPatch, DamageSource damageSource, float totalDamage) {
			super(entityPatch, damageSource, totalDamage);
		}
	}
}