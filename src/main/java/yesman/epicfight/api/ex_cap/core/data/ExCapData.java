package yesman.epicfight.api.ex_cap.core.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.core.managers.ConditionalManager;
import yesman.epicfight.api.ex_cap.core.managers.MovesetManager;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.List;
import java.util.Map;

@Deprecated
public record ExCapData(List<ProviderConditional.Builder> conditionals, Map<Style, ResourceLocation> sets) {

    public void apply(WeaponCapability.Builder cap)
    {
        conditionals.forEach(cap::addConditional);
        sets.forEach( (style, builder) -> cap.addMoveset(style, MovesetManager.getBuilder(builder)));
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder {

        private final List<ProviderConditional.Builder> conditionals = Lists.newArrayList();
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

        @ApiStatus.Internal
        public void addConditional(ResourceLocation... conds) {
            for (ResourceLocation cond : conds) {
                conditionals.add(ConditionalManager.get(cond));
            }
        }

        @ApiStatus.Internal
        public void addMoveset(Style style, ResourceLocation builder)
        {
            moveSets.put(style, builder);
        }
        public ExCapData build() {
            return new ExCapData(
                    List.copyOf(conditionals),
                    Map.copyOf(moveSets)
            );
        }
    }


}