package yesman.epicfight.api.ex_cap.modules.core.data;

import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;

/// See {@link MoveSetEntry} same concept
public record ConditionalEntry(ResourceLocation id, ProviderConditional.ProviderConditionalBuilder builder) { }
