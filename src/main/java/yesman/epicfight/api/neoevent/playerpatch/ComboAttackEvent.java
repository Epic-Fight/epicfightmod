package yesman.epicfight.api.neoevent.playerpatch;

import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ComboAttackEvent extends PlayerPatchEvent<ServerPlayerPatch> implements ICancellableEvent {
	public ComboAttackEvent(ServerPlayerPatch playerpatch) {
		super(playerpatch);
	}
}