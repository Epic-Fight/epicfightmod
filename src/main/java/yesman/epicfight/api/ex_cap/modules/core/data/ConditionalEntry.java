package yesman.epicfight.api.ex_cap.modules.core.data;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;

@Deprecated(forRemoval = true)
public record ConditionalEntry(ResourceLocation id, ProviderConditional.Builder builder) { }