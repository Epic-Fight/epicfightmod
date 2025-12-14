package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class BasicAttackEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	public BasicAttackEvent(ServerPlayerPatch playerpatch) {
		super(playerpatch, true);
	}
}