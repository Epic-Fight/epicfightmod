package yesman.epicfight.api.ex_cap.core.data;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;

@Deprecated
public record ConditionalEntry(ResourceLocation id, ProviderConditional.Builder builder) { }