package net.forixaim.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.data.ExCapData;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class ExCapabilityBuilderPopulationEvent extends Event {
    private final Map<ResourceLocation, List<ExCapData>> builders;

    public ExCapabilityBuilderPopulationEvent() {
        this.builders = Maps.newHashMap();
    }

    public Map<ResourceLocation, List<ExCapData>> getBuilders() {
        return ImmutableMap.copyOf(builders);
    }

    public void registerData(ResourceLocation target, ExCapData... dataSet) {
        builders.computeIfAbsent(target, k -> Lists.newArrayList()).addAll(Arrays.asList(dataSet));
    }

    public void registerData(ResourceLocation target, List<ExCapData> dataSet)
    {
        builders.computeIfAbsent(target, k -> Lists.newArrayList()).addAll(dataSet);
    }
}
