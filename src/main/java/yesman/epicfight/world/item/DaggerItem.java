package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class DaggerItem extends TieredWeaponItem {
	public static ItemAttributeModifiers createDaggerAttributes(Tier iter) {
		return TieredWeaponItem.createAttributes(iter, 1.0F, -1.6F, 0.0F);
	}
	
	public DaggerItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}
}