package yesman.epicfight.registry.deferred.holders;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.world.capabilities.item.custom.CustomData;

public class DeferredCustomData<T extends CustomData<?>> extends DeferredHolder<CustomData<?>, T> {
    protected DeferredCustomData(ResourceKey<CustomData<?>> key) {
        super(key);
    }
}
