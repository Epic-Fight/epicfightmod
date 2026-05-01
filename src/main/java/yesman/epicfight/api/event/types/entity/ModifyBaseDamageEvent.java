package yesman.epicfight.api.event.types.entity;

import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Fired when compute the final attack damage of an entity.
/// This event is called on both side, to modify the delivering
/// damage in server side while it's for modifying tooltip
/// in the client side
public class ModifyBaseDamageEvent extends LivingEntityPatchEvent {
	private final ValueModifier.ResultCalculator modifiedDamageCalculator;
	private final float baseDamage;
	
	public ModifyBaseDamageEvent(LivingEntityPatch<?> entityPatch, float damage, ValueModifier.ResultCalculator modifiedDamageCalculator) {
		super(entityPatch);
		
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