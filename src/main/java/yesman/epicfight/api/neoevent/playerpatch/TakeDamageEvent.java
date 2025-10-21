package yesman.epicfight.api.neoevent.playerpatch;

import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public abstract class TakeDamageEvent extends PlayerPatchEvent<ServerPlayerPatch> {
	protected final DamageSource damageSource;
	protected final float damage;
	
	private TakeDamageEvent(ServerPlayerPatch playerpatch, DamageSource damageSource, float damage) {
		super(playerpatch);
		this.damageSource = damageSource;
		this.damage = damage;
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
		return this.damage;
	}
	
	public static class Income extends TakeDamageEvent implements ICancellableEvent {
		protected boolean parried;
		protected AttackResult.ResultType result;
		
		public Income(ServerPlayerPatch playerpatch, DamageSource damageSource, float baseDamage) {
			super(playerpatch, damageSource, baseDamage);
			
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
	
	public static class Pre extends TakeDamageEvent {
		private final ValueModifier.ResultCalculator calculator;
		
		public Pre(ServerPlayerPatch playerpatch, DamageSource damageSource, ValueModifier.ResultCalculator calculator, float baseDamage) {
			super(playerpatch, damageSource, baseDamage);
			
			this.calculator = calculator;
		}
		
		public void attachValueModifier(ValueModifier valueModifier) {
			this.calculator.attach(valueModifier);
		}
	}
	
	public static class Post extends TakeDamageEvent {
		public Post(ServerPlayerPatch playerpatch, DamageSource damageSource, float totalDamage) {
			super(playerpatch, damageSource, totalDamage);
		}
	}
}