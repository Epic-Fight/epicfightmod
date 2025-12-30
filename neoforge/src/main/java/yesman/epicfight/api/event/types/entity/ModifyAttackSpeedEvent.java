package yesman.epicfight.api.event.types.entity;

import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

/// Fired when compute the final attack speed of an entity.
/// This event is called on both side, to modify attack animations' playback speed on both side.
/// In addition, it modifies tooltip in the client side
public class ModifyAttackSpeedEvent extends LivingEntityPatchEvent {
	private final CapabilityItem item;
	private float attackSpeed;

	public ModifyAttackSpeedEvent(LivingEntityPatch<?> entityPatch, CapabilityItem item, float attackSpeed) {
		super(entityPatch);
		this.item = item;
		this.setAttackSpeed(attackSpeed);
	}

	public void setAttackSpeed(float attackSpeed) {
		this.attackSpeed = attackSpeed;
	}

    public float getAttackSpeed() {
        return this.attackSpeed;
    }

	public CapabilityItem getItemCapability() {
		return this.item;
	}
}