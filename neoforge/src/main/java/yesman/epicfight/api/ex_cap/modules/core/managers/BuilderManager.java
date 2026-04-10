package yesman.epicfight.api.ex_cap.modules.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapBuilderCreationEvent;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

import java.util.Map;

public class BuilderManager {
    private static final Map<ResourceLocation, WeaponCapability.Builder> weaponCapabilityBuilders = Maps.newHashMap();

    public static WeaponCapability.Builder get(ResourceLocation id) {
        return weaponCapabilityBuilders.get(id);
    }

    public static void acceptEvent(ExCapBuilderCreationEvent event)
    {
        weaponCapabilityBuilders.clear();
        weaponCapabilityBuilders.putAll(event.getBuilders());
    }

    public static void acceptExport(WeaponCapabilityPresetRegistryEvent event)
    {
        weaponCapabilityBuilders.entrySet().forEach(entry -> event.getTypeEntry().put(entry.getKey(), item -> WeaponCapabilityPresets.exCapRegistration(entry, item)));
    }

    public static Map.Entry<ResourceLocation, WeaponCapability.Builder> getEntry(ResourceLocation id)
    {
        WeaponCapability.Builder builder = weaponCapabilityBuilders.get(id);
        return builder == null ? null : Map.entry(id, builder);
    }

    public static void add(ResourceLocation id, JsonElement json)
    {
        try {
            weaponCapabilityBuilders.put(id, WeaponCapability.Builder.deserializeBuilder(id, json));
        } catch (JsonParseException e)
        {
            // Log the error and skip this entry
            EpicFight.LOGGER.warn(e.getMessage());
        }
    }

    public static void add(ResourceLocation id, CompoundTag cTag)
    {
//        try {
//            weaponCapabilityBuilders.put(id, WeaponCapability.Builder.deserializeBuilder(id, cTag));
//        } catch (JsonParseException e)
//        {
//            // Log the error and skip this entry
//            EpicFight.LOGGER.warn(e.getMessage());
//        }
    }
}
