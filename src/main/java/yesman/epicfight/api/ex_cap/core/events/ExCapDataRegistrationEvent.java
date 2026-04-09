package yesman.epicfight.api.ex_cap.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.api.ex_cap.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.core.data.ExCapDataEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ExCapDataRegistrationEvent extends Event implements IModBusEvent
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