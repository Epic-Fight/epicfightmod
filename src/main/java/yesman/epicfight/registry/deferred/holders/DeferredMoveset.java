package yesman.epicfight.registry.deferred.holders;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.data.Moveset;

public final class DeferredMoveset extends DeferredHolder<Moveset.Builder, Moveset.Builder> {
    @ApiStatus.Internal
    public DeferredMoveset(ResourceKey<Moveset.Builder> key) {
        super(key);
    }
}
