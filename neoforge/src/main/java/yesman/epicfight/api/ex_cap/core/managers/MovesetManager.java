package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yesman.epicfight.api.ex_cap.core.data.MoveSet;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.core.data.MoveSetEntry;

import java.util.Map;

public class MovesetManager
{
    private static final Map<ResourceLocation, MoveSet.MoveSetBuilder> MOVESETS = Maps.newHashMap();
    private static final Map<ResourceLocation, MoveSet.MoveSetBuilder> REGISTERED_MOVESETS = Maps.newHashMap();


    public static void acceptEvent()
    {
        MOVESETS.clear();
        MOVESETS.putAll(REGISTERED_MOVESETS);
    }

    public static MoveSetEntry register(ResourceLocation id, MoveSet.MoveSetBuilder builder)
    {
        REGISTERED_MOVESETS.put(id, builder);
        return new MoveSetEntry(id, builder);
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
