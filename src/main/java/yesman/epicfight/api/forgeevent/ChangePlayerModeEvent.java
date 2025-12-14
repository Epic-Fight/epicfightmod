package yesman.epicfight.api.forgeevent;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Cancelable
public class ChangePlayerModeEvent extends Event {
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
