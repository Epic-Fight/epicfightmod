package yesman.epicfight.api.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapDataEntry;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;

import java.util.Map;

public class ExCapDataRegistrationEvent extends Event
{
    private final Map<ResourceLocation, ExCapData.Builder> dataMap = Maps.newHashMap();

    public Map<ResourceLocation, ExCapData.Builder> getDataMap() {
        return ImmutableMap.copyOf(dataMap);
    }

    public void addData(ResourceLocation key, ExCapData.Builder data)
    {
        dataMap.put(key, data);
    }

    public void addData(ExCapDataEntry... entries)
    {
        for (ExCapDataEntry entry : entries)
        {
            dataMap.put(entry.id(), entry.data());
        }
    }
}
