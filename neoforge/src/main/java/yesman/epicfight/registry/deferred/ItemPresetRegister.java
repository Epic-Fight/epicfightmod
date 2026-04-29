package yesman.epicfight.registry.deferred;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.deferred.holders.DeferredPreset;
import yesman.epicfight.registry.deferred.holders.DeferredWeapon;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.function.Supplier;

public final class ItemPresetRegister extends DeferredRegister<CapabilityItem.Builder<?>> {
    private ItemPresetRegister(ResourceKey<? extends Registry<CapabilityItem.Builder<?>>> registryKey, String namespace) {
        super(registryKey, namespace);
    }

    public static ItemPresetRegister create(String namespace) {
        return new ItemPresetRegister(EpicFightRegistries.Keys.BUILDERS, namespace);
    }

    public DeferredWeapon registerWeapon(String name, Supplier<WeaponCapability.Builder> builder) {

        ResourceKey<CapabilityItem.Builder<?>> key = ResourceKey.create(
                EpicFightRegistries.Keys.BUILDERS,
                ResourceLocation.fromNamespaceAndPath(this.getNamespace(), name)
        );
        this.register(name, builder);

        return new DeferredWeapon(key);
    }

    @Override
    public <I extends CapabilityItem.Builder<?>> @NotNull DeferredHolder<CapabilityItem.Builder<?>, I> register(@NotNull String name, @NotNull Supplier<? extends I> sup) {
        return super.register(name, sup);
    }

    /**
     * Registers a generic preset and returns a specialized holder.
     * Used for advanced builders that don't fall under the standard 'Weapon' category.
     */
    public <T extends CapabilityItem.Builder<?>> DeferredPreset<T> registerPreset(String name, Supplier<T> builder) {
        ResourceKey<CapabilityItem.Builder<?>> key = ResourceKey.create(
                this.getRegistryKey(),
                ResourceLocation.fromNamespaceAndPath(this.getNamespace(), name)
        );

        this.register(name, builder);

        return new DeferredPreset<>(key);
    }
}
