package yesman.epicfight.registry.deferred;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.api.ex_cap.core.data.Moveset;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.deferred.holders.DeferredMoveset;

public class MovesetRegister extends DeferredRegister<Moveset.Builder> {
    protected MovesetRegister(ResourceKey<? extends Registry<Moveset.Builder>> registryKey, String namespace) {
        super(registryKey, namespace);
    }

    public static MovesetRegister create(String namespace)
    {
        return new MovesetRegister(EpicFightRegistries.Keys.MOVESETS, namespace);

    }


    public DeferredMoveset register(String name, Moveset.Builder builder) {
        this.register(name, key -> builder);

        ResourceKey<Moveset.Builder> key = ResourceKey.create(
                this.getRegistryKey(),
                ResourceLocation.fromNamespaceAndPath(this.getNamespace(), name)
        );

        return new DeferredMoveset(key);
    }
}
