package yesman.epicfight.api.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.api.ex_cap.modules.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;

import java.util.Map;

@Deprecated(forRemoval = true)
public class ConditionalRegistryEvent extends Event
{
    private final Map<ResourceLocation, ProviderConditional.Builder> conditionals;

    public ConditionalRegistryEvent()
    {
        conditionals = Maps.newHashMap();
    }

    public Map<ResourceLocation, ProviderConditional.Builder> getConditionals() {
        return ImmutableMap.copyOf(conditionals);
    }

    @Deprecated(forRemoval = true)
    public void addConditional(ResourceLocation id, ProviderConditional.Builder builder) {
        conditionals.put(id, builder);
    }

    @Deprecated(forRemoval = true)
    public void addConditional(ConditionalEntry... entry) {
        for (ConditionalEntry ent : entry)
        {
            conditionals.put(ent.id(), ent.builder());
        }
    }
}