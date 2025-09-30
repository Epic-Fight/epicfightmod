package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public interface DetachablePlayerEvent<T extends PlayerPatch<?>> {
	T getPlayerPatch();
	
	void setCanceled(boolean canceled);
	
	boolean isCanceled();
}
