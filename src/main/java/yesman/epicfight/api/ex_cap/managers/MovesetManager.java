package yesman.epicfight.api.ex_cap.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.data.Moveset;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.data.modifier.WeaponModifier;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapMovesetRegistryEvent;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.Map;

@ApiStatus.Experimental
public class MovesetManager
{
    private static final Map<ResourceLocation, Moveset.Builder> MOVESETS = Maps.newHashMap();
    private static final Map<ResourceLocation, Moveset.Builder> BUILDER_DECLARED_MOVESETS = Maps.newHashMap();

    public static void acceptEvent(@Deprecated ExCapMovesetRegistryEvent event)
    {
        MOVESETS.clear();
        EpicFightRegistries.MOVESETS.entrySet().forEach(entry -> MOVESETS.put(entry.getKey().location(), entry.getValue()));
        MOVESETS.putAll(BUILDER_DECLARED_MOVESETS);
        MOVESETS.putAll(event.getMovesets());
        MOVESETS.values().forEach( builder -> EpicFightRegistries.MOVESET_DATA.holders().forEach(builder::registerCustomData));
        ModifierManager.modifyMovesets();
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
            EpicFight.LOGGER.warn(e.getMessage());
        }
    }

    public static void modifyData(WeaponModifier modifier)
    {
        modifier.movesetCustomData().forEach((resourceLocation, deferredCustomDataObjectMap) ->
                MOVESETS.get(resourceLocation).addCustomData(deferredCustomDataObjectMap));
    }

    public static Moveset.Builder getBuilder(ResourceLocation id)
    {
        return MOVESETS.get(id);
    }
}
