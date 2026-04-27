package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.ex_cap.core.data.modifier.WeaponModifier;

import java.util.Map;

public class ModifierManager
{
    private static final Map<ResourceLocation, WeaponModifier> MODIFIERS = Maps.newHashMap();
    private static final Map<ResourceLocation, WeaponModifier> REGISTERED_MODIFIERS = Maps.newHashMap();

    public static WeaponModifier register(ResourceLocation id, WeaponModifier.ModifierBuilder builder)
    {
        WeaponModifier  modifier = build(id, builder);
        REGISTERED_MODIFIERS.put(id, modifier);
        return modifier;
    }


    private static WeaponModifier build(ResourceLocation id, WeaponModifier.ModifierBuilder builder)
    {
        return new WeaponModifier(id, builder.target, builder.conditionals, builder.moveSetModifier, builder.type);
    }

}
