package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class DaggerItem extends WeaponItem {
	public DaggerItem(Item.Properties build, Tier materialIn) {
		super(materialIn, 1, -1.6F, build);
	}
}