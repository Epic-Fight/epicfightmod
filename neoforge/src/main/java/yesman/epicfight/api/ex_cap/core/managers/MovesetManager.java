package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yesman.epicfight.api.ex_cap.core.data.Moveset;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.core.data.MovesetEntry;

import java.util.Map;

public class MovesetManager
{
    private static final Map<ResourceLocation, Moveset.Builder> MOVESETS = Maps.newHashMap();
    private static final Map<ResourceLocation, Moveset.Builder> REGISTERED_MOVESETS = Maps.newHashMap();
    private static boolean FROZEN = false;

    public static void acceptEvent()
    {
        MOVESETS.clear();
        MOVESETS.putAll(REGISTERED_MOVESETS);
    }

    public static MovesetEntry register(ResourceLocation id, Moveset.Builder builder)
    {
        if (FROZEN) {
            throw new UnsupportedOperationException("Registry is frozen, cannot register " + id.toString());
        }
        if (REGISTERED_MOVESETS.containsKey(id)) {
            throw new IllegalArgumentException("Moveset with ID " + id + " already exists!");
        }
        REGISTERED_MOVESETS.put(id, builder);
        return new MovesetEntry(id, builder);
    }

    public static void freeze()
    {
        EpicFight.LOGGER.info("Freezing moveset registry");
        FROZEN = true;
    }

    public static void add(ResourceLocation id, JsonElement jsonElement)
    {
        try {
            Moveset.Builder builder = Moveset.Builder.deserialize(jsonElement);
            MOVESETS.put(id, builder);
        } catch (JsonParseException e) {
            //Skip invalid JSON
            EpicFight.LOGGER.warn(e.getMessage());
        }
    }

    public static Moveset.Builder getBuilder(ResourceLocation id)
    {
        return MOVESETS.get(id);
    }
}
