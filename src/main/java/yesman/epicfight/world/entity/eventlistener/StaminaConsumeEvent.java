package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class StaminaConsumeEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private float amount;
	
	public StaminaConsumeEvent(PlayerPatch<?> playerpatch, float amount) {
		super(playerpatch, false);
		this.amount = amount;
	}
	
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	public float getAmount() {
		return this.amount;
	}
}