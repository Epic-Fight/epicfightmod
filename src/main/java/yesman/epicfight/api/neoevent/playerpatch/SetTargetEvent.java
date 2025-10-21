package yesman.epicfight.api.neoevent.playerpatch;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class SetTargetEvent extends PlayerPatchEvent<ServerPlayerPatch> implements ICancellableEvent {
	private final LivingEntity target;
	
	public SetTargetEvent(ServerPlayerPatch playerpatch, LivingEntity target) {
		super(playerpatch);
		
		this.target = target;
	}
	
	public LivingEntity getTarget() {
		return this.target;
	}
}