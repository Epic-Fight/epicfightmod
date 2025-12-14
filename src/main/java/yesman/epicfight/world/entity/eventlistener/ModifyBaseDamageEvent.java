package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ModifyBaseDamageEvent<T extends PlayerPatch<?>> extends AbstractPlayerEvent<T> {
	private ValueModifier.ResultCalculator modifiedDamageCalculator;
	private final float baseDamage;
	
	public ModifyBaseDamageEvent(T playerpatch, float damage, ValueModifier.ResultCalculator modifiedDamageCalculator) {
		super(playerpatch, false);
		
		this.baseDamage = damage;
		this.modifiedDamageCalculator = modifiedDamageCalculator;
	}
	
	public float getBaseDamage() {
		return this.baseDamage;
	}
	
	public void attachValueModifier(ValueModifier modifier) {
		this.modifiedDamageCalculator.attach(modifier);
	}
	
	public float calculateModifiedDamage() {
		return this.modifiedDamageCalculator.getResult(this.baseDamage);
	}
}