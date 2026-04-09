package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import yesman.epicfight.api.ex_cap.core.events.ExCapBuilderCreationEvent;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.main.EpicFightMod;
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
            EpicFightMod.LOGGER.warn(e.getMessage());
        }

    }
}