package yesman.epicfight.api.client.forgeevent;

import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class WeaponCategoryIconRegisterEvent extends Event implements IModBusEvent {
	final Map<WeaponCategory, ItemStack> registry;
	
	public WeaponCategoryIconRegisterEvent(Map<WeaponCategory, ItemStack> registry) {
		this.registry = registry;
	}
	
	public void registerCategory(WeaponCategory weaponCategory, Item item) {
		this.registry.put(weaponCategory, new ItemStack(item));
	}
	
	public void registerCategory(WeaponCategory weaponCategory, ItemStack itemstack) {
		this.registry.put(weaponCategory, itemstack);
	}
}
