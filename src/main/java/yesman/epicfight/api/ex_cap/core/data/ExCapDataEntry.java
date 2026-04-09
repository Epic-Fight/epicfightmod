package yesman.epicfight.api.ex_cap.core.data;

import net.minecraft.resources.ResourceLocation;

public record ExCapDataEntry(ResourceLocation id, ExCapData.Builder data) { }