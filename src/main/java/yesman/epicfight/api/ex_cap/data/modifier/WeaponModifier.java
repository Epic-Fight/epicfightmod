package yesman.epicfight.api.ex_cap.data.modifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.api.ex_cap.managers.ConditionalManager;
import yesman.epicfight.api.ex_cap.managers.MovesetManager;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;
import yesman.epicfight.registry.deferred.holders.*;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.custom.CustomData;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApiStatus.Experimental
public record WeaponModifier(List<ResourceLocation> targets, Map<ResourceLocation, Operation> conditionalModifier, Map<Style, ResourceLocation> movesetModifier, Map<ResourceLocation, Map<DeferredCustomData<? extends CustomData<?>>, Object>> movesetCustomData, Map<DeferredCustomData<? extends CustomData<?>>, Object> weaponCustomData) {
    public enum Operation {
        APPEND,
        REMOVE
    }

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private final List<ResourceLocation> target;
        private final Map<ResourceLocation, Operation> conditionalModifier;
        private final Map<Style, ResourceLocation> movesetModifier;
        private final List<ProviderConditional.Builder> conditionalBuilders;
        private final Map<Style, Moveset.Builder> movesetBuilders;
        private final Map<ResourceLocation, Map<DeferredCustomData<? extends CustomData<?>>, Object>> movesetCustomData;
        private final Map<DeferredCustomData<? extends CustomData<?>>, Object> weaponCustomData;
        private Builder() {
            this.target = Lists.newArrayList();
            this.conditionalModifier = Maps.newHashMap();
            this.movesetCustomData = Maps.newHashMap();
            this.weaponCustomData = Maps.newHashMap();
            this.movesetModifier = Maps.newHashMap();
            this.conditionalBuilders = Lists.newArrayList();
            this.movesetBuilders = Maps.newHashMap();
        }

        public void assemble(ResourceLocation builderId)
        {
            conditionalBuilders.forEach(builder -> {
                ResourceLocation generatedLocation = ResourceLocation.fromNamespaceAndPath(builderId.getNamespace(),
                        builderId.getPath() + "/generated/modifier/" + builder.getWieldStyle().toString().toLowerCase(Locale.ROOT));
                EpicFight.LOGGER.info("Generated conditional modifier: {}", generatedLocation);
                ConditionalManager.addConditional(generatedLocation, builder);
                addConditionalModifier(generatedLocation);
            });
            movesetBuilders.forEach((style, builder) -> {
                ResourceLocation generatedLocation = ResourceLocation.fromNamespaceAndPath(builderId.getNamespace(),
                        builderId.getPath() + "/generated/modifier/" + style.toString().toLowerCase(Locale.ROOT));
                EpicFight.LOGGER.info("Generated moveset modifier: {}", generatedLocation);
                MovesetManager.addMoveset(generatedLocation, builder);
                addMovesetModifier(style, generatedLocation);
            });
        }

        public Builder target(ResourceLocation... targets) {
            this.target.addAll(Arrays.asList(targets));
            return this;
        }

        public Builder target(DeferredWeapon... weapon)
        {
            for (var ids : weapon)
            {
                this.target.add(ids.getId());
            }
            return this;
        }

        public void addConditionalModifier(ResourceLocation key) {
            this.conditionalModifier.put(key, Operation.APPEND);
        }

        @ApiStatus.Internal
        public Builder addConditionalModifier(DeferredConditional... conditionals) {
            for (DeferredConditional conditional : conditionals) {
                this.conditionalModifier.put(conditional.getId(), Operation.APPEND);
            }
            return this;
        }

        public Builder addConditionalModifier(ProviderConditional.Builder builder) {
            this.conditionalBuilders.add(builder);
            return this;
        }

        @ApiStatus.Internal
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

        public <T> Builder setMovesetData(ResourceLocation rl, DeferredCustomData<? extends CustomData<T>> data, T value)
        {
            this.movesetCustomData.computeIfAbsent(rl, s -> Maps.newHashMap()).put(data, value);
            return this;
        }

        public <T> Builder setMovesetData(DeferredMoveset moveset, DeferredCustomData<? extends CustomData<T>> data, T value)
        {
            return this.setMovesetData(moveset.getId(), data, value);
        }

        public <T> Builder setWeaponData(DeferredCustomData<? extends CustomData<T>> data, T value)
        {
            this.weaponCustomData.put(data, value);
            return this;
        }

        public WeaponModifier build(ResourceLocation builderId) {
            assemble(builderId);
            Map<ResourceLocation, Map<DeferredCustomData<? extends CustomData<?>>, Object>> movesetCustomData = Maps.newHashMap();
            this.movesetCustomData.forEach((style, data) -> movesetCustomData.put(style, ImmutableMap.copyOf(data)));
            return new WeaponModifier(ImmutableList.copyOf(target), ImmutableMap.copyOf(conditionalModifier), ImmutableMap.copyOf(movesetModifier), ImmutableMap.copyOf(movesetCustomData), ImmutableMap.copyOf(weaponCustomData));
        }
    }
}
