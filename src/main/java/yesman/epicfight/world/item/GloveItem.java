package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GloveItem extends WeaponItem {
	public GloveItem(Item.Properties properties) {
		super(properties);
	}
	
	@Override
    public int getEnchantmentValue() {
        return 22;
    }
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return toRepair.getItem() == Items.IRON_NUGGET;
	}
}