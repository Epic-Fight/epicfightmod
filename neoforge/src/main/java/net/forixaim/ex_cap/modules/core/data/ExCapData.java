package net.forixaim.ex_cap.modules.core.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.managers.ConditionalManager;
import net.forixaim.ex_cap.modules.core.managers.MovesetManager;
import net.forixaim.ex_cap.modules.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record ExCapData(List<ProviderConditional> conditionals, Map<Style, ResourceLocation> sets) {

    public void apply(WeaponCapability.Builder cap)
    {
        conditionals.forEach(cap::addConditionals);
        sets.forEach( (style, builder) -> cap.addMoveSet(style, MovesetManager.getBuilder(builder)));
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder {

        private final List<ProviderConditional> conditionals = Lists.newArrayList();
        private final Map<Style, ResourceLocation> moveSets = Maps.newHashMap();

        public Builder addConditional(ResourceLocation... conds) {
            for (ResourceLocation cond : conds) {
                conditionals.add(ConditionalManager.get(cond).build());
            }
            return this;
        }

        public Builder addMoveset(Style style, ResourceLocation builder)
        {
            moveSets.put(style, builder);
            return this;
        }
        public ExCapData build() {
            return new ExCapData(
                    List.copyOf(conditionals),
                    Map.copyOf(moveSets)
            );
        }
    }


}
