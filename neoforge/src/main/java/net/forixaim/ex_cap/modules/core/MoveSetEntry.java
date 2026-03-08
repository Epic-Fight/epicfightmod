package net.forixaim.ex_cap.modules.core;

import net.minecraft.resources.ResourceLocation;

/// A record representing an entry in the MoveSet registry, containing the ID and the builder for the MoveSet. For modders to use when registering their MoveSets.
/// Datapack authors do not need to use this, as they will be using JSON files to define their MoveSets which are built directly from JSON data;
public record MoveSetEntry(ResourceLocation id, MoveSet.MoveSetBuilder builder) {}
