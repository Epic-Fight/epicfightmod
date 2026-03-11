package yesman.epicfight.api.ex_cap.modules.core.data;

import net.minecraft.resources.ResourceLocation;

public record ExCapDataEntry(ResourceLocation id, ExCapData.Builder data) { }
