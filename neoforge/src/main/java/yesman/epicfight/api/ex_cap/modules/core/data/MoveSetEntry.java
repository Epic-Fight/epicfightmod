package yesman.epicfight.api.ex_cap.modules.core.data;

import net.minecraft.resources.ResourceLocation;

/// A record representing an entry in the {@link MoveSet} registry, containing the ID and the builder for the {@link MoveSet}. For modders to use when registering their {@link MoveSet}.
/// Datapack authors do not need to use this, as they will be using JSON files to define their {@link MoveSet} which are built directly from JSON data;
public record MoveSetEntry(ResourceLocation id, MoveSet.MoveSetBuilder builder) {}
