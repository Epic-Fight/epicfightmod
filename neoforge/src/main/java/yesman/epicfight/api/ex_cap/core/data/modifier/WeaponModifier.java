package yesman.epicfight.api.ex_cap.core.data.modifier;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.core.data.ItemPreset;
import yesman.epicfight.api.ex_cap.core.data.Moveset;
import yesman.epicfight.api.ex_cap.core.data.MovesetEntry;
import yesman.epicfight.api.ex_cap.core.managers.MovesetManager;
import yesman.epicfight.world.capabilities.item.Style;

import java.util.Locale;
import java.util.Map;

public record WeaponModifier(ResourceLocation id, ResourceLocation target, Map<ResourceLocation, Operation> conditionals, Map<Style, ResourceLocation> moveSetModifier, @ApiStatus.Internal Type type)
{
    public enum Operation
    {
        APPEND,
        REPLACE,
        REMOVE
    }

    public enum Type
    {
        DATA_PACK,
        CODE
    }

    public static ModifierBuilder builder()
    {
        return new ModifierBuilder(Type.CODE);
    }

    @ApiStatus.Internal
    public static ModifierBuilder datapack()
    {
        return new ModifierBuilder(Type.DATA_PACK);
    }

    public static class ModifierBuilder
    {
        public ResourceLocation target = ResourceLocation.parse("");
        public final Map<ResourceLocation, Operation> conditionals;
        public final Map<Style, ResourceLocation> moveSetModifier;
        public final Type type;

        ModifierBuilder(Type type)
        {
            this.conditionals = Maps.newHashMap();
            this.moveSetModifier = Maps.newHashMap();
            this.type = type;
        }

        public ModifierBuilder setTarget(ItemPreset itemPreset)
        {
            return setTarget(itemPreset.id());
        }

        @ApiStatus.Internal
        public ModifierBuilder setTarget(ResourceLocation id)
        {
            target = id;
            return this;
        }

        @ApiStatus.Internal
        public ModifierBuilder modifyMoveset(Style style, ResourceLocation moveSet)
        {
            moveSetModifier.put(style, moveSet);
            return this;
        }

        public ModifierBuilder modifyMoveset(Style style, MovesetEntry moveSet)
        {
            this.moveSetModifier.put(style, moveSet.id());
            return this;
        }


        public ModifierBuilder modifyMoveset(Style style, Moveset.Builder builder)
        {
            if (this.target == null) {
                throw new IllegalStateException("You must call setTarget() before defining a dynamic moveset!");
            }
            ResourceLocation modifiedMoveset = ResourceLocation.fromNamespaceAndPath(target.getNamespace(), target.getPath() + "/modified/" + style.toString().toLowerCase(Locale.ROOT));
            this.modifyMoveset(style, MovesetManager.register(modifiedMoveset, builder));
            return this;
        }
    }
}
