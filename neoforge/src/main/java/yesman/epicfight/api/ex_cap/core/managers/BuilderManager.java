package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.core.data.BuilderEntry;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

import java.util.Map;
import java.util.function.Function;

@ApiStatus.Experimental
public class BuilderManager {
    private static final Map<ResourceLocation, CapabilityItem.Builder<?>> BUILDERS = Maps.newHashMap();
    private static final Map<ResourceLocation, CapabilityItem.Builder<?>> REGISTERED_BUILDERS = Maps.newHashMap();
    private static boolean FROZEN = false;

    public static CapabilityItem.Builder<?> get(ResourceLocation id) {
        return BUILDERS.get(id);
    }

    public static CapabilityItem.Builder<?> get(BuilderEntry entry)
    {
        return get(entry.id());
    }

    /**
     * Registers a weapon capability builder into the global registry and returns a handle for reusability.
     * <p>
     * This method centralizes the registration process, ensuring the builder is indexed by its
     * identifier before being wrapped in an entry object.
     * </p>
     * @param id      The unique {@link ResourceLocation} for the builder (e.g., "modid:weapon_type")
     * @param builder The {@link CapabilityItem.Builder} instance defining the weapon's properties
     * @return A {@link BuilderEntry} containing the ID and the builder, used for referencing
     * the capability in other registries or inheritance.
     */
    public static BuilderEntry register(ResourceLocation id, CapabilityItem.Builder<?> builder) {
        if (FROZEN)
            throw new UnsupportedOperationException("Registry is frozen, cannot register " + id.toString());
        builder.identifier(id);
        REGISTERED_BUILDERS.put(id, builder);
        return new BuilderEntry(id, builder);
    }

    @ApiStatus.Internal
    public static void acceptEvent()
    {
        BUILDERS.clear();
        BUILDERS.putAll(REGISTERED_BUILDERS);
    }

    @ApiStatus.Internal
    public static void export(Map<ResourceLocation, Function<Item, ? extends CapabilityItem.Builder<?>>> event)
    {
        BUILDERS.forEach((entry, builder) -> event.put(entry, item -> WeaponCapabilityPresets.exCapRegistration(builder, item)));
    }

    public static void add(ResourceLocation id, CompoundTag cTag)
    {

    }
}
