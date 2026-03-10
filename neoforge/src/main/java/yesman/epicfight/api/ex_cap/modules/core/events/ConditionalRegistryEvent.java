package yesman.epicfight.api.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import yesman.epicfight.api.ex_cap.modules.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;

import java.util.Map;

public class ConditionalRegistryEvent extends Event
{
    private final Map<ResourceLocation, ProviderConditional.ProviderConditionalBuilder> conditionals;

    public ConditionalRegistryEvent()
    {
        conditionals = Maps.newHashMap();
    }

    public Map<ResourceLocation, ProviderConditional.ProviderConditionalBuilder> getConditionals() {
        return ImmutableMap.copyOf(conditionals);
    }

    public void addConditional(ResourceLocation id, ProviderConditional.ProviderConditionalBuilder builder) {
        conditionals.put(id, builder);
    }

    public void addConditional(ConditionalEntry... entry) {
        for (ConditionalEntry ent : entry)
        {
            conditionals.put(ent.id(), ent.builder());
        }
    }
}
