package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class LongswordItem extends TieredWeaponItem {
	public static ItemAttributeModifiers createLongswordAttributes(Tier iter) {
		return TieredWeaponItem.createAttributes(iter, 4.0F, -2.8F, 0.0F);
	}
	
	public LongswordItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}
}