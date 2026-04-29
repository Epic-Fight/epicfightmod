package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.ex_cap.core.data.modifier.WeaponModifier;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.Map;

public class ModifierManager {
    private static final Map<ResourceLocation, WeaponModifier> BUILDERS = Maps.newHashMap();

    public static void acceptEvent()
    {
        BUILDERS.clear();
        EpicFightRegistries.MODIFIERS.entrySet().forEach(
                entry -> BUILDERS.put(entry.getKey().location(), entry.getValue().build()));
    }

    public static void add(ResourceLocation rl, CompoundTag tag)
    {
        //TODO: Implement
    }

    public static void modify()
    {
        BUILDERS.forEach((entry, builder) -> ItemPresetManager.modify(builder));
    }
}
