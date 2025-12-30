package yesman.epicfight.api.event.types.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;

import java.util.Map;
import java.util.function.Function;

public class EntityPatchRegistryEvent extends Event {
	private final Map<EntityType<?>, Function<Entity, EntityPatch<?>>> typeEntry;

	public EntityPatchRegistryEvent(Map<EntityType<?>, Function<Entity, EntityPatch<?>>> typeEntry) {
		this.typeEntry = typeEntry;
	}

    /// Prefer using [#registerEntityPatch] or [#registerEntityPatchUnsafe] when registering entity patches for type-safety.
    public Map<EntityType<?>, Function<Entity, EntityPatch<?>>> getTypeEntry() {
        return this.typeEntry;
    }

    /// Preferred over [#getTypeEntry()] and [#registerEntityPatchUnsafe] for type-safety:
    ///
    /// ```java
    /// registerEntityPatch(EntityType.ZOMBIE, ZombiePatch::new);
    /// ```
    ///
    /// @param entityType         the type of entity to patch
    /// @param entityPatchFactory a factory function that provides the original entity to create the entity patch.
    /// @param <T>                the entity type
    public <T extends Entity> void registerEntityPatch(
            EntityType<T> entityType,
            Function<T, EntityPatch<T>> entityPatchFactory
    ) {
        CommonEntityPatchProvider.registerEntityPatch(
                typeEntry,
                entityType,
                entityPatchFactory
        );
    }

    /// Strongly prefer [#registerEntityPatch] over this unsafe version
    /// for type-safety and strict design.
    /// Use this only as a last resort.
    ///
    /// Sometimes it is necessary to use this when dealing with vanilla types:
    ///
    /// ```java
    /// registerEntityPatchUnsafe(
    ///        registry,
    ///        EntityType.PLAYER,
    ///        entity -> new ServerPlayerPatch((ServerPlayer) entity)
    /// );
    /// ```
    public <T extends Entity> void registerEntityPatchUnsafe(
            EntityType<T> entityType,
            Function<? super T, ? extends EntityPatch<? extends T>> entityPatchFactory
    ) {
        CommonEntityPatchProvider.registerEntityPatchUnsafe(
                typeEntry,
                entityType,
                entityPatchFactory
        );
    }
}
