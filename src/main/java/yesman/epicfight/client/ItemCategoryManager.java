package yesman.epicfight.client;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

/** Maintains automatic combat/mining item categories without a configuration screen. */
public final class ItemCategoryManager {
	private static final Set<Class<? extends Item>> WEAPON_CLASSES = new HashSet<>();
	private static final Set<Class<? extends Item>> TOOL_CLASSES = new HashSet<>();

	static {
		WEAPON_CLASSES.add(SwordItem.class);
		WEAPON_CLASSES.add(BowItem.class);
		WEAPON_CLASSES.add(CrossbowItem.class);
		WEAPON_CLASSES.add(TridentItem.class);
		TOOL_CLASSES.add(AxeItem.class);
		TOOL_CLASSES.add(HoeItem.class);
		TOOL_CLASSES.add(PickaxeItem.class);
		TOOL_CLASSES.add(ShovelItem.class);
	}

	public static void resetItems() {
		ClientConfig.combatCategorizedItems.clear();
		ClientConfig.miningCategorizedItems.clear();
		BuiltInRegistries.ITEM.forEach(item -> {
			if (isWeapon(item)) {
				ClientConfig.combatCategorizedItems.add(item);
			} else {
				ClientConfig.miningCategorizedItems.add(item);
			}
		});
	}

	private static boolean isWeapon(Item item) {
		Class<?> itemClass = item.getClass();
		while (itemClass != Item.class) {
			if (WEAPON_CLASSES.contains(itemClass)) {
				return true;
			}
			if (TOOL_CLASSES.contains(itemClass)) {
				return false;
			}
			itemClass = itemClass.getSuperclass();
		}
		CapabilityItem capability = EpicFightCapabilities.getItemStackCapability(item.getDefaultInstance());
		return capability instanceof WeaponCapability;
	}

	private ItemCategoryManager() {
	}
}
