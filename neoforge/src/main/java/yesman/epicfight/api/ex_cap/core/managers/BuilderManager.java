package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.deferred.holders.DeferredPreset;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

import java.util.Map;
import java.util.function.Function;

@ApiStatus.Experimental
public class BuilderManager {
    private static final Map<ResourceLocation, CapabilityItem.Builder<?>> BUILDERS = Maps.newHashMap();

    public static CapabilityItem.Builder<?> get(ResourceLocation id) {
        return BUILDERS.get(id);
    }

    public static CapabilityItem.Builder<?> get(DeferredPreset<? extends CapabilityItem.Builder<?>> entry)
    {
        return get(entry.getId());
    }


    @ApiStatus.Internal
    public static void acceptEvent() {
        BUILDERS.clear();
        for (var entry : EpicFightRegistries.BUILDERS.entrySet()) {
            BUILDERS.put(entry.getKey().location(), entry.getValue());
        }

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
