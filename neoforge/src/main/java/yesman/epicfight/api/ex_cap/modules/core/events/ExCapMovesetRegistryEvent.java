package yesman.epicfight.api.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSet;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSetEntry;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;

import java.util.Map;

public class ExCapMovesetRegistryEvent extends Event
{
    private final Map<ResourceLocation, MoveSet.MoveSetBuilder> movesets;

    public ExCapMovesetRegistryEvent()
    {
        movesets = Maps.newHashMap();
    }

    public Map<ResourceLocation, MoveSet.MoveSetBuilder> getMovesets() {
        return ImmutableMap.copyOf(movesets);
    }

    public void addMoveset(ResourceLocation id, MoveSet.MoveSetBuilder moveSet) {
        movesets.put(id, moveSet);
    }

    public void addMoveSet(MoveSetEntry... entries)
    {
        for (MoveSetEntry entry : entries)
        {
            addMoveset(entry.id(), entry.builder());
        }
    }
}
