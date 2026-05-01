package yesman.epicfight.api.ex_cap.core.data;

import net.minecraft.resources.ResourceLocation;

@Deprecated
public record ExCapDataEntry(ResourceLocation id, ExCapData.Builder data) { }