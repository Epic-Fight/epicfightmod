package yesman.epicfight.registry.deferred_shim;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/// Fabric-compatible replacement for NeoForge's [DeferredRegister].
///
/// Queues registrations and processes them when [#accept()] is called
/// (typically from the mod initializer).
///
/// @param <T> the registry element type
public class DeferredRegisterShim<T> {
    private final ResourceKey<?> registryKey;
    private final String modId;
    private final List<DeferredHolderShim<T, ?>> entries = new ArrayList<>();
    private boolean accepted = false;

    public DeferredRegisterShim(ResourceKey<?> registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public String getNamespace() {
        return modId;
    }

    @SuppressWarnings("unchecked")
    public <R> ResourceKey<Registry<R>> getRegistryKey() {
        return (ResourceKey<Registry<R>>) registryKey;
    }

    public <I extends T> DeferredHolderShim<T, I> register(String name, Supplier<I> supplier) {
        if (accepted) {
            throw new IllegalStateException("Cannot register after accept() has been called");
        }
        DeferredHolderShim<T, I> holder = new DeferredHolderShim<>(registryKey, ResourceLocation.fromNamespaceAndPath(modId, name), supplier);
        entries.add(holder);
        return holder;
    }

    /// Adds a pre-created DeferredHolderShim subclass to the entries list.
    /// Used by ItemPresetRegister to register DeferredWeapon/DeferredPreset instances
    /// directly, so that accept() binds the same instance that is returned to the caller.
    protected <I extends T> DeferredHolderShim<T, I> addEntry(DeferredHolderShim<T, I> holder) {
        if (accepted) {
            throw new IllegalStateException("Cannot register after accept() has been called");
        }
        entries.add(holder);
        return holder;
    }

    /// Overload that accepts a Function<ResourceLocation, I> like NeoForge's DeferredRegister.
    public <I extends T> DeferredHolderShim<T, I> register(String name, java.util.function.Function<ResourceLocation, I> factory) {
        if (accepted) {
            throw new IllegalStateException("Cannot register after accept() has been called");
        }
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(modId, name);
        DeferredHolderShim<T, I> holder = new DeferredHolderShim<>(registryKey, location, () -> factory.apply(location));
        entries.add(holder);
        return holder;
    }

    public java.util.Set<DeferredHolderShim<T, ?>> getEntries() {
        return new java.util.LinkedHashSet<>(entries);
    }
    public void accept() {
        if (accepted) {
            return;
        }
        accepted = true;

        Registry<T> registry = getRegistry();
        for (DeferredHolderShim<T, ?> entry : entries) {
            entry.bind(registry);
        }
    }

    @SuppressWarnings("unchecked")
    private Registry<T> getRegistry() {
        Registry<?> registry = BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (registry == null) {
            throw new IllegalStateException("Registry not found for key: " + registryKey.location());
        }
        return (Registry<T>) registry;
    }
}
