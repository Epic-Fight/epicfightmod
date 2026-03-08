package net.forixaim.ex_cap.modules.core.managers;

import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.data.ExCapData;
import net.forixaim.ex_cap.modules.core.events.ExCapDataRegistrationEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class DatasetManager {
    private static final Map<ResourceLocation, ExCapData> dataMap = Maps.newHashMap();

    public static void acceptEvent(ExCapDataRegistrationEvent event) {
        dataMap.clear();
        dataMap.putAll(event.getDataMap());
    }
}
