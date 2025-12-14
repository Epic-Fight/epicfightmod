package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class SpearItem extends WeaponItem {
	public SpearItem(Item.Properties build, Tier materialIn) {
		super(materialIn, 3, -2.8F, build);
	}
}