package net.forixaim.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.MoveSet;
import net.forixaim.ex_cap.modules.core.MoveSetEntry;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;

import java.util.HashMap;
import java.util.Map;

public class ExCapMovesetRegistryEvent extends Event
{
    private final Map<ResourceLocation, MoveSet.MoveSetBuilder> MOVESETS;

    public ExCapMovesetRegistryEvent()
    {
        MOVESETS = Maps.newHashMap();
    }

    public Map<ResourceLocation, MoveSet.MoveSetBuilder> getMovesets() {
        return ImmutableMap.copyOf(MOVESETS);
    }

    public void addMoveset(ResourceLocation id, MoveSet.MoveSetBuilder moveSet) {
        MOVESETS.put(id, moveSet);
    }

    public void addMoveSet(MoveSetEntry... entries)
    {
        for (MoveSetEntry entry : entries)
        {
            addMoveset(entry.id(), entry.builder());
        }
    }
}
