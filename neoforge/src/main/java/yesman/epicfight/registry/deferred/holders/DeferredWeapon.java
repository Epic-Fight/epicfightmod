package yesman.epicfight.registry.deferred.holders;

import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;


public final class DeferredWeapon extends DeferredPreset<WeaponCapability.Builder> {
    @ApiStatus.Internal
    public DeferredWeapon(ResourceKey<CapabilityItem.Builder<?>> key) {
        super(key);
    }
}
