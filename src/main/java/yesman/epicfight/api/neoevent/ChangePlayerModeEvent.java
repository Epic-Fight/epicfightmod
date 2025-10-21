package yesman.epicfight.api.neoevent;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class ChangePlayerModeEvent extends Event implements ICancellableEvent {
	private final PlayerPatch<?> playerpatch;
	private final PlayerPatch.PlayerMode playerMode;
	
	public ChangePlayerModeEvent(PlayerPatch<?> playerpatch, PlayerPatch.PlayerMode playerMode) {
		this.playerpatch = playerpatch;
		this.playerMode = playerMode;
	}
	
	public PlayerPatch<?> getPlayerPatch() {
		return this.playerpatch;
	}
	
	public PlayerPatch.PlayerMode getPlayerMode() {
		return this.playerMode;
	}
}
