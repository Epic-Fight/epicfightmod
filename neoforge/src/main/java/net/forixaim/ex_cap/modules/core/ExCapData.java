package net.forixaim.ex_cap.modules.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.provider.ProviderConditional;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record ExCapData(List<ProviderConditional> conditionals, Map<Style, MoveSet.MoveSetBuilder> sets) {

    public void apply(WeaponCapability.Builder cap)
    {
        conditionals.forEach(cap::addConditionals);
        sets.forEach(cap::addMoveSet);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder {

        private final List<ProviderConditional> conditionals = Lists.newArrayList();
        private final Map<Style, MoveSet.MoveSetBuilder> moveSets = Maps.newHashMap();

        public Builder addConditional(ProviderConditional... conds) {
            conditionals.addAll(Arrays.asList(conds));
            return this;
        }

        public Builder addMoveset(Style style, MoveSet.MoveSetBuilder builder)
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
