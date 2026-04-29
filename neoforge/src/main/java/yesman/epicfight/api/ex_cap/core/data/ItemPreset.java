package yesman.epicfight.api.ex_cap.core.data;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

/**
 * An immutable handle representing a registered weapon capability.
 * <p>
 * This record serves as the bridge between a weapon's identity and its configuration.
 * It is primarily used to facilitate inheritance between builders and to provide
 * a type-safe reference for moveset and style assignments.
 * </p>
 * @param id       The unique {@link ResourceLocation} assigned to this capability.
 * @param template The {@link CapabilityItem.Builder} containing the weapon's
 * properties and behavioral definitions.
 */
public record ItemPreset(ResourceLocation id, CapabilityItem.Builder<?> template) { }
