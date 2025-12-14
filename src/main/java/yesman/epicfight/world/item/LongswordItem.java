package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class LongswordItem extends WeaponItem {
	public LongswordItem(Item.Properties build, Tier materialIn) {
		super(materialIn, 4, -2.8F, build);
	}
}