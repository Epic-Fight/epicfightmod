package yesman.epicfight.api.event.types.player;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class TogglePlayerModeEvent extends LivingEntityPatchEvent implements CancelableEvent {
	private final PlayerPatch.PlayerMode playerMode;
	
	public TogglePlayerModeEvent(PlayerPatch<?> playerPatch, PlayerPatch.PlayerMode playerMode) {
		super(playerPatch);
		this.playerMode = playerMode;
	}
	
	public PlayerPatch<?> getPlayerPatch() {
		return (PlayerPatch<?>) this.getEntityPatch();
	}
	
	public PlayerPatch.PlayerMode getPlayerMode() {
		return this.playerMode;
	}
}
