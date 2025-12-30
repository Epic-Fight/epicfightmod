package yesman.epicfight.world.capabilities.item;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.item.ItemStack;
import yesman.epicfight.data.conditions.Condition;

public class RuntimeCapability extends CapabilityItem {
	private final List<Pair<Condition<ItemStack>, CapabilityItem>> variations;
	private final CapabilityItem defaultCapability;
	
	public RuntimeCapability(List<Pair<Condition<ItemStack>, CapabilityItem>> variations, CapabilityItem defaultCapability) {
		super(CapabilityItem.builder().category(WeaponCategories.NOT_WEAPON));
		
		this.variations = variations;
		this.defaultCapability = defaultCapability;
	}
	
	@Override
	public CapabilityItem findRecursive(ItemStack itemstack) {
		for (Pair<Condition<ItemStack>, CapabilityItem> pair : this.variations) {
			if (pair.getFirst().predicate(itemstack)) {
				return pair.getSecond().findRecursive(itemstack);
			}
		}
		
		return this.defaultCapability.findRecursive(itemstack);
	}
}