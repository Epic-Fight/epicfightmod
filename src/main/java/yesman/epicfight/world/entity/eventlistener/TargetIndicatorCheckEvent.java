package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class TargetIndicatorCheckEvent extends AbstractPlayerEvent<LocalPlayerPatch> {
	private final LivingEntityPatch<?> target;
	
	public TargetIndicatorCheckEvent(LocalPlayerPatch playerpatch, LivingEntityPatch<?> target) {
		super(playerpatch, true);
		
		this.target = target;
		this.setCanceled(true);
	}
	
	public LivingEntityPatch<?> getTarget() {
		return this.target;
	}
}