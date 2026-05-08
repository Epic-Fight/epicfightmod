package yesman.epicfight.api.ex_cap.modules.core.events;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSetEntry;
import yesman.epicfight.api.ex_cap.data.Moveset;

import java.util.Map;

/**
 * @deprecated Use the static register via {@link yesman.epicfight.registry.deferred.MovesetRegister} this exists primarily for legacy compatibility
 */
@Deprecated(forRemoval = true)
public class ExCapMovesetRegistryEvent extends Event
{
    private final Map<ResourceLocation, Moveset.Builder> movesets;

    public ExCapMovesetRegistryEvent()
    {
        movesets = Maps.newHashMap();
    }

    public Map<ResourceLocation, Moveset.Builder> getMovesets() {
        return ImmutableMap.copyOf(movesets);
    }

    public void addMoveset(ResourceLocation id, Moveset.Builder moveSet) {
        movesets.put(id, moveSet);
    }

    @Deprecated
    public void addMoveSet(MoveSetEntry... entries)
    {
        for (MoveSetEntry entry : entries)
        {
            addMoveset(entry.id(), entry.builder());
        }
    }
}