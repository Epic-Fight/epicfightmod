package yesman.epicfight.api.ex_cap.core.data;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public record BuilderEntry(ResourceLocation id, WeaponCapability.Builder template) { }