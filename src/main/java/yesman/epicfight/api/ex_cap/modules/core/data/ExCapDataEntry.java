package yesman.epicfight.api.ex_cap.modules.core.data;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a single entry in the ExCap data file.
 * @param id The ID of the entry.
 * @param data The data associated with the entry.
 * @deprecated For Removal. All this has been delegated to the WeaponModifier.
 */
@Deprecated(forRemoval = true)
public record ExCapDataEntry(ResourceLocation id, ExCapData.Builder data) { }