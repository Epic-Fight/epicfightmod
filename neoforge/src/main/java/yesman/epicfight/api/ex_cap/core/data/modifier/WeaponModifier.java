package yesman.epicfight.api.ex_cap.core.data.modifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.core.data.Moveset;
import yesman.epicfight.api.ex_cap.core.managers.ConditionalManager;
import yesman.epicfight.api.ex_cap.core.managers.MovesetManager;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.registry.deferred.holders.DeferredConditional;
import yesman.epicfight.registry.deferred.holders.DeferredMoveset;
import yesman.epicfight.registry.deferred.holders.DeferredWeapon;
import yesman.epicfight.world.capabilities.item.Style;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public record WeaponModifier(ResourceLocation target, Map<ResourceLocation, Operation> conditionalModifier, Map<Style, ResourceLocation> movesetModifier) {
    public enum Operation {
        APPEND,
        REMOVE
    }

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private ResourceLocation target;
        private final Map<ResourceLocation, Operation> conditionalModifier;
        private final Map<Style, ResourceLocation> movesetModifier;
        private final List<ProviderConditional.Builder> conditionalBuilders;
        private final Map<Style, Moveset.Builder> movesetBuilders;
        private Builder() {
            this.conditionalModifier = Maps.newHashMap();
            this.movesetModifier = Maps.newHashMap();
            this.conditionalBuilders = Lists.newArrayList();
            this.movesetBuilders = Maps.newHashMap();
        }

        public void assemble()
        {
            conditionalBuilders.forEach(builder -> {
                ResourceLocation generatedLocation = ResourceLocation.fromNamespaceAndPath(target.getNamespace(),
                        target.getPath() + "/generated/modifier/" + builder.getWieldStyle().toString().toLowerCase(Locale.ROOT));
                EpicFight.LOGGER.info("Generated conditional modifier: {}", generatedLocation);
                ConditionalManager.addConditional(generatedLocation, builder);
                addConditionalModifier(generatedLocation);
            });
            movesetBuilders.forEach((style, builder) -> {
                ResourceLocation generatedLocation = ResourceLocation.fromNamespaceAndPath(target.getNamespace(),
                        target.getPath() + "/generated/modifier/" + style.toString().toLowerCase(Locale.ROOT));
                EpicFight.LOGGER.info("Generated moveset modifier: {}", generatedLocation);
                MovesetManager.addMoveset(generatedLocation, builder);
                addMovesetModifier(style, generatedLocation);
            });
        }

        public Builder target(ResourceLocation target) {
            this.target = target;
            return this;
        }

        public Builder target(DeferredWeapon weapon)
        {
            return this.target(weapon.getId());
        }

        public Builder addConditionalModifier(ResourceLocation key) {
            this.conditionalModifier.put(key, Operation.APPEND);
            return this;
        }

        public Builder addConditionalModifier(DeferredConditional conditional) {
            return this.addConditionalModifier(conditional.getId());
        }

        public Builder addConditionalModifier(ProviderConditional.Builder builder) {
            this.conditionalBuilders.add(builder);
            return this;
        }

        public Builder removeConditionalModifier(ResourceLocation key) {
            this.conditionalModifier.put(key, Operation.REMOVE);
            return this;
        }

        public Builder removeConditionalModifier(DeferredConditional conditional) {
            return this.removeConditionalModifier(conditional.getId());
        }

        public Builder addMovesetModifier(Style style, ResourceLocation moveset) {
            this.movesetModifier.put(style, moveset);
            return this;
        }

        public Builder addMovesetModifier(Style style, DeferredMoveset preset) {
            return this.addMovesetModifier(style, preset.getId());
        }

        public Builder addMovesetModifier(Style style, Moveset.Builder builder) {
            this.movesetBuilders.put(style, builder);
            return this;
        }

        public WeaponModifier build() {
            assemble();
            return new WeaponModifier(target, conditionalModifier, movesetModifier);
        }
    }
}
