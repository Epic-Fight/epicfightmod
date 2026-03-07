package yesman.epicfight.api.event.types.registry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.ExCapData;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class ExCapabilityBuilderPopulationEvent extends Event {
    Map<WeaponCapability.Builder, List<ExCapData>> builders;

    public ExCapabilityBuilderPopulationEvent() {
        this.builders = Maps.newHashMap();
    }

    public Map<WeaponCapability.Builder, List<ExCapData>> getBuilders() {
        return ImmutableMap.copyOf(builders);
    }

    public void registerData(WeaponCapability.Builder target, ExCapData... dataSet) {
        builders.computeIfAbsent(target, k -> Lists.newArrayList()).addAll(Arrays.asList(dataSet));
    }
}
