package yesman.epicfight.api.ex_cap.managers;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import com.google.gson.JsonElement;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapDataRegistrationEvent;

import java.util.Map;

@Deprecated
public class DatasetManager {
    private static final Map<ResourceLocation, ExCapData.Builder> dataMap = Maps.newHashMap();

    public static void acceptEvent(ExCapDataRegistrationEvent event) {
        dataMap.clear();
        dataMap.putAll(event.getDataMap());
    }

    public static ExCapData.Builder get(ResourceLocation resourceLocation)
    {
        return dataMap.get(resourceLocation);
    }

    public static void add(ResourceLocation resourceLocation, JsonElement jsonElement)
    {
        dataMap.put(resourceLocation, ExCapData.Builder.deserialize(jsonElement));
    }
}