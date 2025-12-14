package yesman.epicfight.world.entity.eventlistener;

import net.minecraft.world.damagesource.DamageSource;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public abstract class TakeDamageEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	protected final DamageSource damageSource;
	protected final float baseDamage;
	
	private TakeDamageEvent(ServerPlayerPatch playerpatch, DamageSource damageSource, float baseDamage, boolean cancellable) {
		super(playerpatch, cancellable);
		this.damageSource = damageSource;
		this.baseDamage = baseDamage;
	}
	
	public DamageSource getDamageSource() {
		return this.damageSource;
	}
	
	/**
	 * For Attack and Hurt, it's base damage
	 * For Damage, it's modified damage
	 * @return
	 */
	public float getDamage() {
		return this.baseDamage;
	}
	
	public static class Attack extends TakeDamageEvent {
		protected boolean parried;
		protected AttackResult.ResultType result;
		
		public Attack(ServerPlayerPatch playerpatch, DamageSource damageSource, float baseDamage) {
			super(playerpatch, damageSource, baseDamage, true);
			
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
	
	public static class Hurt extends TakeDamageEvent {
		private final ValueModifier.ResultCalculator calculator;
		
		public Hurt(ServerPlayerPatch playerpatch, DamageSource damageSource, ValueModifier.ResultCalculator calculator, float baseDamage) {
			super(playerpatch, damageSource, baseDamage, false);
			
			this.calculator = calculator;
		}
		
		public void attachValueModifier(ValueModifier valueModifier) {
			this.calculator.attach(valueModifier);
		}
	}
	
	public static class Damage extends TakeDamageEvent {
		public Damage(ServerPlayerPatch playerpatch, DamageSource damageSource, float totalDamage) {
			super(playerpatch, damageSource, totalDamage, false);
		}
	}
}
