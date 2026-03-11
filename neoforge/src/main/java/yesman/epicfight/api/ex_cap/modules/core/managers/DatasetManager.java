package yesman.epicfight.api.ex_cap.modules.core.managers;

import com.google.common.collect.Maps;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapDataRegistrationEvent;
import net.minecraft.resources.ResourceLocation;
import com.google.gson.JsonElement;

import java.util.Map;

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
