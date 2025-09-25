package yesman.epicfight.api.neoevent;

import java.util.Map;
import java.util.function.Function;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public class EntityPatchRegistryEvent extends Event implements IModBusEvent {
	private final Map<EntityType<?>, Function<Entity, EntityPatch<?>>> typeEntry;
	
	public EntityPatchRegistryEvent(Map<EntityType<?>, Function<Entity, EntityPatch<?>>> typeEntry) {
		this.typeEntry = typeEntry;
	}
	
	public Map<EntityType<?>, Function<Entity, EntityPatch<?>>> getTypeEntry() {
		return this.typeEntry;
	}
}