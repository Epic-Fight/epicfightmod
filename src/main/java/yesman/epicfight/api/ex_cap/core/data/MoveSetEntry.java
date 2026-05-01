package yesman.epicfight.api.ex_cap.core.data;

import net.minecraft.resources.ResourceLocation;

/// A record representing an entry in the {@link Moveset} registry, containing the ID and the builder for the {@link Moveset}. For modders to use when registering their {@link Moveset}.
/// Datapack authors do not need to use this, as they will be using JSON files to define their {@link Moveset} which are built directly from JSON data;
@Deprecated
public record MoveSetEntry(ResourceLocation id, Moveset.Builder builder) {}