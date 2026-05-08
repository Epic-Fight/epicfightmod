package yesman.epicfight.api.ex_cap.modules.core.data;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.registry.deferred.holders.DeferredPreset;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

/**
 * Represents a single entry in the builder data file.
 * @param id The ID of the entry.
 * @param template The template associated with the entry.
 * @deprecated For Removal. DeferredHolder does exactly the same thing. So this has been removed and transferred to {@link DeferredPreset} which extends {@link DeferredHolder}
 */
@Deprecated(forRemoval = true)
public record BuilderEntry(ResourceLocation id, WeaponCapability.Builder template) { }