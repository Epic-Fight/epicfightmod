package yesman.epicfight.api.ex_cap.core.data;

import net.minecraft.resources.ResourceLocation;

/**
 * An immutable handle representing a registered moveset configuration.
 * <p>
 * This record bridges a unique identifier with its corresponding {@link MoveSet.MoveSetBuilder}.
 * It is used to facilitate moveset inheritance and allows the {@link yesman.epicfight.api.ex_cap.core.managers.MovesetManager} to
 * provide type-safe references for weapon capabilities and style swaps.
 * </p>
 *
 * @param id      The unique {@link ResourceLocation} assigned to this moveset.
 * @param builder The {@link MoveSet.MoveSetBuilder} instance containing the
 * animations and combat logic.
 */
public record MoveSetEntry(ResourceLocation id, MoveSet.MoveSetBuilder builder) {}
