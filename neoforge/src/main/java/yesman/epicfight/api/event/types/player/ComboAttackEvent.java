package yesman.epicfight.api.event.types.player;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ComboAttackEvent extends LivingEntityPatchEvent implements CancelableEvent {
	public ComboAttackEvent(ServerPlayerPatch playerPatch) {
        super(playerPatch);
	}

    public ServerPlayerPatch getPlayerPatch() {
        return (ServerPlayerPatch)this.getEntityPatch();
    }
}