package net.forixaim.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.Map;

public class ExCapBuilderCreationEvent {
    private final Map<ResourceLocation, WeaponCapability.Builder> builders = Maps.newHashMap();

    public Map<ResourceLocation, WeaponCapability.Builder> getBuilders() {
        return ImmutableMap.copyOf(builders);
    }

    public void addBuilder(ResourceLocation rl, WeaponCapability.Builder builder) {
        builders.put(rl, builder);
    }
}
