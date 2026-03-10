package yesman.epicfight.api.ex_cap.modules.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSet;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapMovesetRegistryEvent;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;

import java.util.Map;

public class MovesetManager
{
    private static final Map<ResourceLocation, MoveSet.MoveSetBuilder> MOVESETS = Maps.newHashMap();

    public static void acceptEvent(ExCapMovesetRegistryEvent event)
    {
        MOVESETS.clear();
        MOVESETS.putAll(event.getMovesets());
    }

    public static void add(ResourceLocation id, JsonElement jsonElement)
    {
        try {
            MoveSet.MoveSetBuilder builder = MoveSet.MoveSetBuilder.deserialize(jsonElement);
            MOVESETS.put(id, builder);
        } catch (JsonParseException e) {
            //Skip invalid JSON
            EpicFight.LOGGER.warn(e.getMessage());
        }
    }

    public static MoveSet.MoveSetBuilder getBuilder(ResourceLocation id)
    {
        return MOVESETS.get(id);
    }
}
