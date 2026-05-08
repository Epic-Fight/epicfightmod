package yesman.epicfight.api.ex_cap.managers;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.data.modifier.WeaponModifier;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.Map;

@ApiStatus.Experimental
public class ModifierManager {
    private static final Map<ResourceLocation, WeaponModifier> BUILDERS = Maps.newHashMap();

    public static void acceptEvent()
    {
        BUILDERS.clear();
        EpicFightRegistries.MODIFIERS.entrySet().forEach(
                entry -> BUILDERS.put(entry.getKey().location(), entry.getValue().build(entry.getKey().location())));
    }


    public static void add(ResourceLocation rl, CompoundTag tag)
    {
        //TODO: Implement
    }

    public static void modify()
    {
        BUILDERS.forEach((entry, builder) -> ItemPresetManager.modify(builder));
    }

    public static void modifyMovesets()
    {
        BUILDERS.forEach((entry, builder) -> MovesetManager.modifyData(builder));
    }
}
