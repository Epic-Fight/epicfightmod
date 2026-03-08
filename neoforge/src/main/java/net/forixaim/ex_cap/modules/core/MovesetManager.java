package net.forixaim.ex_cap.modules.core;

import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.events.ExCapMovesetRegistryEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class MovesetManager
{
    private static final Map<ResourceLocation, MoveSet.MoveSetBuilder> MOVESETS = Maps.newHashMap();

    public static void acceptEvent(ExCapMovesetRegistryEvent event)
    {
        MOVESETS.clear();
        MOVESETS.putAll(event.getMovesets());
    }

    public static MoveSet.MoveSetBuilder getBuilder(ResourceLocation id)
    {
        return MOVESETS.get(id);
    }
}
