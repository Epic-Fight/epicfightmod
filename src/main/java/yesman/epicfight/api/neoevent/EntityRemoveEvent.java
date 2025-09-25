package yesman.epicfight.api.neoevent;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public class EntityRemoveEvent extends Event {
	private final Entity.RemovalReason removalReason;
	private final Entity entity;
	
	public EntityRemoveEvent(Entity.RemovalReason removalReason, Entity entity) {
		this.entity = entity;
		this.removalReason = removalReason;
	}
	
	public Entity.RemovalReason getRemovalReason() {
		return this.removalReason;
	}
	
	public Entity getEntity() {
		return this.entity;	
	}
}
