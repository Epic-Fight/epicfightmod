package yesman.epicfight.api.event.types.player;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class TickPlayerEpicFightModeEvent extends LivingEntityPatchEvent implements CancelableEvent {
	public TickPlayerEpicFightModeEvent(PlayerPatch<?> playerPatch) {
        super(playerPatch);
	}
	
	public PlayerPatch<?> getPlayerPatch() {
		return (PlayerPatch<?>)this.getEntityPatch();
	}
}