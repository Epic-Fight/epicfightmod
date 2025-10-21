package yesman.epicfight.world.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.main.EpicFightMod;

public class UchigatanaItem extends WeaponItem {
	public static ItemAttributeModifiers createUchigatanaAttributes() {
		return TieredWeaponItem.createAttributes(6.0F, -2.0F);
	}
	
	public UchigatanaItem(Item.Properties build) {
		super(build);
	}
	
	@Override
    public int getEnchantmentValue() {
        return 22;
    }
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return toRepair.getItem() == Items.IRON_BARS;
	}
    
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		tooltipComponents.add(Component.literal(""));
		tooltipComponents.add(Component.translatable(EpicFightMod.format("item.%s.uchigatana.tooltip")));
	}
}