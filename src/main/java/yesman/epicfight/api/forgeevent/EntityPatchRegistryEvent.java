package yesman.epicfight.api.forgeevent;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public class EntityPatchRegistryEvent extends Event implements IModBusEvent {
	private final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> typeEntry;
	
	public EntityPatchRegistryEvent(Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> typeEntry) {
		this.typeEntry = typeEntry;
	}
	
	public Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> getTypeEntry() {
		return this.typeEntry;
	}
}