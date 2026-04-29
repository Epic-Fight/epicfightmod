package yesman.epicfight.api.ex_cap.core.data.modifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.core.data.ItemPreset;
import yesman.epicfight.api.ex_cap.core.data.Moveset;
import yesman.epicfight.api.ex_cap.core.data.MovesetEntry;
import yesman.epicfight.api.ex_cap.core.managers.ConditionalManager;
import yesman.epicfight.api.ex_cap.core.managers.MovesetManager;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.world.capabilities.item.Style;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record WeaponModifier(ResourceLocation id, ResourceLocation target, Map<ResourceLocation, Operation> conditionals, Map<Style, ResourceLocation> moveSetModifier, @ApiStatus.Internal Type type)
{
    public enum Operation
    {
        APPEND,
        REMOVE
    }

    public enum Type
    {
        DATA_PACK,
        CODE
    }

    public static Builder builder()
    {
        return new Builder(Type.CODE);
    }

    @ApiStatus.Internal
    public static Builder datapack()
    {
        return new Builder(Type.DATA_PACK);
    }

    public static class Builder
    {
        public ResourceLocation target = ResourceLocation.parse("");
        public final Map<ResourceLocation, Operation> conditionals;
        public final List<ProviderConditional.Builder> conditionalBuilders;
        public final Map<Style, ResourceLocation> moveSetModifier;
        public final Map<Style, Moveset.Builder> moveSetBuilders;
        public final Type type;

        Builder(Type type)
        {
            this.conditionals = Maps.newHashMap();
            this.moveSetModifier = Maps.newHashMap();
            this.moveSetBuilders = Maps.newHashMap();
            this.conditionalBuilders = Lists.newArrayList();
            this.type = type;
        }

        public Builder setTarget(ItemPreset itemPreset)
        {
            return setTarget(itemPreset.id());
        }

        @ApiStatus.Internal
        public Builder setTarget(ResourceLocation id)
        {
            target = id;
            return this;
        }

        public void assemble()
        {
            moveSetBuilders.forEach( (style, builder) -> {
                ResourceLocation modifiedMoveset = ResourceLocation.fromNamespaceAndPath(target.getNamespace(), target.getPath() + "/generated/modified/" + style.toString().toLowerCase(Locale.ROOT));
                this.modifyMoveset(style, MovesetManager.register(modifiedMoveset, builder));
            });

            conditionalBuilders.forEach(builder -> {
                ResourceLocation modifiedConditional = ResourceLocation.fromNamespaceAndPath(target.getNamespace(), target.getPath() + "/generated/modified/" + builder.getWieldStyle().toString().toLowerCase(Locale.ROOT));
                this.addConditional(ConditionalManager.register(modifiedConditional, builder));
            });
        }

        @ApiStatus.Internal
        public Builder modifyMoveset(Style style, ResourceLocation moveSet)
        {
            moveSetModifier.put(style, moveSet);
            return this;
        }

        public Builder modifyMoveset(Style style, MovesetEntry moveSet)
        {
            this.moveSetModifier.put(style, moveSet.id());
            return this;
        }

        public Builder removeConditional(ResourceLocation conditional)
        {
            this.conditionals.put(conditional, Operation.REMOVE);
            return this;
        }

        public Builder removeConditional(ConditionalEntry conditionalEntry)
        {
            return this.removeConditional(conditionalEntry.id());
        }

        public Builder addConditional(ConditionalEntry conditionalEntry)
        {
            return this.addConditional(conditionalEntry.id());
        }

        public Builder addConditional(ProviderConditional.Builder builder)
        {
            this.conditionalBuilders.add(builder);
            return this;
        }

        public Builder addConditional(ResourceLocation conditional)
        {
            this.conditionals.put(conditional, Operation.APPEND);
            return this;
        }

        public Builder modifyMoveset(Style style, Moveset.Builder builder)
        {
            if (this.target == null)
            {
                throw new IllegalStateException("You must call setTarget() before defining a dynamic moveset!");
            }
            ResourceLocation modifiedMoveset = ResourceLocation.fromNamespaceAndPath(target.getNamespace(), target.getPath() + "/modified/" + style.toString().toLowerCase(Locale.ROOT));
            this.modifyMoveset(style, MovesetManager.register(modifiedMoveset, builder));
            return this;
        }
    }
}
