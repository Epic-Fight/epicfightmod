package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class TachiItem extends TieredWeaponItem {
	public static ItemAttributeModifiers createTachiAttributes(Tier iter) {
		return TieredWeaponItem.createAttributes(iter, 4.0F, -2.8F, 0.0F);
	}
	
	public TachiItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}
}