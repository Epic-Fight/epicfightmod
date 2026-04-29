package yesman.epicfight.api.ex_cap.core.data;

import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;

/**
 * An immutable handle representing a registered style condition.
 * <p>
 * This record stores the logic required to determine the active {@code Style} of a weapon.
 * It is used by the system to evaluate environmental, skill-based, or state-based
 * requirements (e.g., dual-wielding, sheathing, or specific skill activations).
 * </p>
 * @param id      The unique {@link ResourceLocation} assigned to this condition.
 * @param builder The {@link ProviderConditional.Builder} containing
 * the logic predicate used for runtime evaluation.
 */
public record ConditionalEntry(ResourceLocation id, ProviderConditional.Builder builder) { }
