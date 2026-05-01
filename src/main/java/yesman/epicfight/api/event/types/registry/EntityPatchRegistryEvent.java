package yesman.epicfight.api.event.types.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;

import java.util.Map;
import java.util.function.Function;

public class EntityPatchRegistryEvent extends Event {
	private final Map<EntityType<?>, Function<Entity, EntityPatch<?>>> typeEntry;

	public EntityPatchRegistryEvent(@NotNull Map<EntityType<?>, Function<Entity, @NotNull EntityPatch<?>>> typeEntry) {
		this.typeEntry = typeEntry;
	}

    /// @deprecated Prefer using [#registerEntityPatch] or [#registerEntityPatchUnsafe] when registering entity patches for type-safety.
    /// If you have a use case where the full [Map] is needed, please [file a GitHub issue](https://github.com/Epic-Fight/epicfight/issues/new?template=03_api.yml).
    @Deprecated(forRemoval = true)
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
            @NotNull EntityType<T> entityType,
            @NotNull Function<T, EntityPatch<T>> entityPatchFactory
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
            @NotNull EntityType<T> entityType,
            @NotNull Function<? super T, ? extends EntityPatch<? extends T>> entityPatchFactory
    ) {
        CommonEntityPatchProvider.registerEntityPatchUnsafe(
                typeEntry,
                entityType,
                entityPatchFactory
        );
    }
}
