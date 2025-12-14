package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;

public class GloveItem extends WeaponItem {
	public GloveItem(Item.Properties build, Tier materialIn) {
		super(materialIn, 2, 0.0F, build);
	}
}