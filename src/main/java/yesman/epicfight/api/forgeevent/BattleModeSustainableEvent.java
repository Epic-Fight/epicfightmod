package yesman.epicfight.api.forgeevent;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Cancelable
public class BattleModeSustainableEvent extends Event {
	private final PlayerPatch<?> playerpatch;
	
	public BattleModeSustainableEvent(PlayerPatch<?> playerpatch) {
		this.playerpatch = playerpatch;
	}
	
	public PlayerPatch<?> getPlayerPatch() {
		return this.playerpatch;
	}
}