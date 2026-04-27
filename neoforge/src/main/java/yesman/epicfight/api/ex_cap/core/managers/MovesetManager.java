package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yesman.epicfight.api.ex_cap.core.data.Moveset;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.Map;

public class MovesetManager
{
    private static final Map<ResourceLocation, Moveset.Builder> MOVESETS = Maps.newHashMap();
    private static final Map<ResourceLocation, Moveset.Builder> BUILDER_DECLARED_MOVESETS = Maps.newHashMap();

    public static void acceptEvent()
    {
        MOVESETS.clear();
        EpicFightRegistries.MOVESETS.entrySet().forEach(entry -> MOVESETS.put(entry.getKey().location(), entry.getValue()));
        MOVESETS.putAll(BUILDER_DECLARED_MOVESETS);
    }

    public static void addMoveset(ResourceLocation rl, Moveset.Builder builder)
    {
        BUILDER_DECLARED_MOVESETS.put(rl, builder);
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
