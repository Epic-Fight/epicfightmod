package net.forixaim.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import net.forixaim.ex_cap.modules.core.data.ExCapData;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ExCapDataRegistrationEvent
{
    private Map<ResourceLocation, ExCapData> dataMap;

    public Map<ResourceLocation, ExCapData> getDataMap() {
        return ImmutableMap.copyOf(dataMap);
    }

    public void addData(ResourceLocation key, ExCapData data)
    {
        dataMap.put(key, data);
    }
}
