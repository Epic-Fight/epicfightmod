package yesman.epicfight.api.neoevent.playerpatch;

import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class PatchedProjectileHitEvent extends PlayerPatchEvent<PlayerPatch<?>> implements ICancellableEvent {
	private final ProjectileImpactEvent neoforgeEvent;
	
	public PatchedProjectileHitEvent(PlayerPatch<?> playerpatch, ProjectileImpactEvent neoforgeEvent) {
		super(playerpatch);
		
		this.neoforgeEvent = neoforgeEvent;
	}
	
	public ProjectileImpactEvent getNeoForgeEvent() {
		return this.neoforgeEvent;
	}
}