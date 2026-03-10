package yesman.epicfight.api.ex_cap.modules.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import yesman.epicfight.api.ex_cap.modules.core.events.ConditionalRegistryEvent;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ConditionalManager
{
    private static final Map<ResourceLocation, ProviderConditional.ProviderConditionalBuilder> CONDITIONALS = Maps.newHashMap();

    public static ProviderConditional.ProviderConditionalBuilder get(ResourceLocation id) {
        return CONDITIONALS.get(id);
    }

    public static void add(ResourceLocation id, JsonElement json) {
        CONDITIONALS.put(id, ProviderConditional.ProviderConditionalBuilder.deserialize(json));
    }

    public static void acceptEvent(ConditionalRegistryEvent event)
    {
        CONDITIONALS.clear();
        CONDITIONALS.putAll(event.getConditionals());
    }

}
