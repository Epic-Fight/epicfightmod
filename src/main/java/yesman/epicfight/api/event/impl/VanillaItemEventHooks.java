package yesman.epicfight.api.event.impl;

import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public final class VanillaItemEventHooks {
    /// Called when gathering [Attribute] list of an item
    /// @see ItemStack#forEachModifier
    public static void onModifyItemAttribute(ItemStack itemStack, ModifierLoader modifierLoader) {
        CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(itemStack);

        if (!itemCap.isEmpty()) {
            Multimap<Holder<Attribute>, AttributeModifier> multimap = itemCap.getAttributeModifiers(null);

            for (Holder<Attribute> key : multimap.keys()) {
                for (AttributeModifier modifier : multimap.get(key)) {
                    modifierLoader.put(key, modifier, EquipmentSlotGroup.MAINHAND);
                }
            }
        }
    }

    @FunctionalInterface
    public interface ModifierLoader {
        void put(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot);
    }

    private VanillaItemEventHooks() {}
}
