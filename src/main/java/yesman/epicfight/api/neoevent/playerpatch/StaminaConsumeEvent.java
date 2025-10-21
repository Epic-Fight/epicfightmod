package yesman.epicfight.api.neoevent.playerpatch;

import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class StaminaConsumeEvent extends PlayerPatchEvent<PlayerPatch<?>> {
	private float amount;
	
	public StaminaConsumeEvent(PlayerPatch<?> playerpatch, float amount) {
		super(playerpatch);
		
		this.amount = amount;
	}
	
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	public float getAmount() {
		return this.amount;
	}
}