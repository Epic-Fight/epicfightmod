package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ModifyAttackSpeedEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private final CapabilityItem item;
	private float attackSpeed;
	
	public ModifyAttackSpeedEvent(PlayerPatch<?> playerpatch, CapabilityItem item, float attackSpeed) {
		super(playerpatch, false);
		this.item = item;
		this.setAttackSpeed(attackSpeed);
	}
	
	public void setAttackSpeed(float attackSpeed) {
		this.attackSpeed = attackSpeed;
	}
	
	public CapabilityItem getItemCapability() {
		return this.item;
	}
	
	public float getAttackSpeed() {
		return this.attackSpeed;
	}
}