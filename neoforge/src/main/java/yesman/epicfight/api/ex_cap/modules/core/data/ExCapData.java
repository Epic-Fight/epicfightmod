package yesman.epicfight.api.ex_cap.modules.core.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import yesman.epicfight.api.ex_cap.modules.core.managers.ConditionalManager;
import yesman.epicfight.api.ex_cap.modules.core.managers.MovesetManager;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.List;
import java.util.Map;

public record ExCapData(List<ProviderConditional> conditionals, Map<Style, ResourceLocation> sets) {

    public void apply(WeaponCapability.Builder cap)
    {
        cap.addConditionals(conditionals);
        sets.forEach( (style, builder) -> cap.addMoveSet(style, MovesetManager.getBuilder(builder)));
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder {

        private final List<ProviderConditional> conditionals = Lists.newArrayList();
        private final Map<Style, ResourceLocation> moveSets = Maps.newHashMap();

        public static Builder deserialize(JsonElement jsonElement)
        {
            Builder builder = new Builder();
            JsonElement conditionals = jsonElement.getAsJsonObject().get("conditionals");
            if (conditionals != null && conditionals.isJsonArray())
            {
                conditionals.getAsJsonArray().forEach(el -> builder.addConditional(ResourceLocation.parse(el.getAsJsonObject().get("id").getAsString())));
            }
            JsonElement moveSets = jsonElement.getAsJsonObject().get("move_sets");
            if (moveSets != null && moveSets.isJsonObject())
            {
                moveSets.getAsJsonObject().entrySet().forEach(entry ->
                {
                    Style style = Style.ENUM_MANAGER.get(entry.getKey());
                    ResourceLocation moveSet = ResourceLocation.tryParse(entry.getValue().getAsString());
                    if (style != null && moveSet != null)
                    {
                        builder.addMoveset(style, moveSet);
                    }
                });
            }
            return builder;
        }

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
