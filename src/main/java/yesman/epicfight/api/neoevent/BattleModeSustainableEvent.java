package yesman.epicfight.api.neoevent;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class BattleModeSustainableEvent extends Event implements ICancellableEvent {
	private final PlayerPatch<?> playerpatch;
	
	public BattleModeSustainableEvent(PlayerPatch<?> playerpatch) {
		this.playerpatch = playerpatch;
	}
	
	public PlayerPatch<?> getPlayerPatch() {
		return this.playerpatch;
	}
}