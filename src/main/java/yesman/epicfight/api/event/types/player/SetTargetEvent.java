package yesman.epicfight.api.event.types.player;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SetTargetEvent extends LivingEntityPatchEvent implements CancelableEvent {
	private final LivingEntity target;
	
	public SetTargetEvent(ServerPlayerPatch playerPatch, LivingEntity target) {
		super(playerPatch);

		this.target = target;
	}

    public final ServerPlayerPatch getPlayerPatch() {
        return (ServerPlayerPatch)this.getEntityPatch();
    }

	public final LivingEntity getTarget() {
		return this.target;
	}
}