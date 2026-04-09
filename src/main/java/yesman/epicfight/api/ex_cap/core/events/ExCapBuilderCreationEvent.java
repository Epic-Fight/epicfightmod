package yesman.epicfight.api.ex_cap.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.api.ex_cap.core.data.BuilderEntry;
import yesman.epicfight.api.ex_cap.core.managers.ExCapManager;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.Map;

public class ExCapBuilderCreationEvent extends Event implements IModBusEvent
{
    private final Map<ResourceLocation, WeaponCapability.Builder> builders = Maps.newHashMap();

    public Map<ResourceLocation, WeaponCapability.Builder> getBuilders() {
        return ImmutableMap.copyOf(builders);
    }

    public void addBuilder(ResourceLocation rl, WeaponCapability.Builder builder) {
        ExCapManager.addAcceptor(rl);
        builders.put(rl, builder);
    }

    public void addBuilder(BuilderEntry... entries)
    {
        for (BuilderEntry entry : entries)
        {
            addBuilder(entry.id(), entry.template());
        }
    }


}