package yesman.epicfight.api.ex_cap.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.api.ex_cap.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.core.managers.DatasetManager;

import java.util.Map;
import java.util.List;

@Deprecated
public class ExCapabilityBuilderPopulationEvent extends Event {
    private final Map<ResourceLocation, List<ExCapData.Builder>> builders;

    public ExCapabilityBuilderPopulationEvent() {
        this.builders = Maps.newHashMap();
    }

    public Map<ResourceLocation, List<ExCapData.Builder>> getBuilders() {
        return ImmutableMap.copyOf(builders);
    }

    @Deprecated
    public void registerData(ResourceLocation target, ResourceLocation... dataSet) {
        List<ExCapData.Builder> list = Lists.newArrayList();
        for (ResourceLocation id : dataSet) {
            list.add(DatasetManager.get(id));
        }
        builders.computeIfAbsent(target, k -> Lists.newArrayList()).addAll(list);
    }

    @Deprecated
    public void registerData(ResourceLocation target, List<ExCapData.Builder> dataSet)
    {
        builders.computeIfAbsent(target, k -> Lists.newArrayList()).addAll(dataSet);
    }
}