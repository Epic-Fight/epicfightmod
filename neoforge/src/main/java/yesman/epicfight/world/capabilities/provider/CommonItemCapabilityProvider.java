package yesman.epicfight.world.capabilities.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import yesman.epicfight.gameasset.ItemPresets;
import yesman.epicfight.api.ex_cap.core.data.ItemPreset;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import yesman.epicfight.world.capabilities.item.*;

public final class CommonItemCapabilityProvider implements ICapabilityProvider<ItemStack, Void, CapabilityItem> {
	public static final CommonItemCapabilityProvider INSTANCE = new CommonItemCapabilityProvider();
	
	private CommonItemCapabilityProvider() {}
	
	private final Map<Class<? extends Item>, ItemPreset> typedCapabilities = new HashMap<> ();
	private final Map<Item, CapabilityItem> capabilities = new HashMap<> ();
	
	public void registerWeaponTypesByClass() {
		this.typedCapabilities.put(ArmorItem.class, ItemPresets.ARMOR);
		this.typedCapabilities.put(ShieldItem.class, ItemPresets.SHIELD);
        this.typedCapabilities.put(SwordItem.class, ItemPresets.SWORD);
        this.typedCapabilities.put(PickaxeItem.class, ItemPresets.PICKAXE);
        this.typedCapabilities.put(AxeItem.class, ItemPresets.AXE);
        this.typedCapabilities.put(ShovelItem.class, ItemPresets.SHOVEL);
        this.typedCapabilities.put(HoeItem.class, ItemPresets.HOE);
        this.typedCapabilities.put(BowItem.class, ItemPresets.BOW);
        this.typedCapabilities.put(CrossbowItem.class, ItemPresets.CROSSBOW);
		this.typedCapabilities.put(MapItem.class, ItemPresets.MAP);
	}
	
	public void put(Item item, CapabilityItem cap) {
		this.capabilities.put(item, cap);
	}
	
	public CapabilityItem get(Item item) {
		return capabilities.getOrDefault(item, getDefault(item));
	}

	private CapabilityItem getDefault(Item item)
	{
		ItemPreset itemPreset = this.typedCapabilities.getOrDefault(item.getClass(), null);
		CapabilityItem.Builder<?> result = null;
		if (itemPreset != null)
		{
			if (itemPreset.template() instanceof WeaponCapability.Builder)
			{
				result = WeaponCapabilityPresets.exCapRegistration(itemPreset.template(), item);
			}
			else if (itemPreset.template() instanceof ArmorCapability.Builder builder)
			{
				result = builder.byItem(item);
			}
			else
			{
				result = itemPreset.template();
			}
		}

		return result != null ? result.build() : null;
	}
	
	public void clear() {
		this.capabilities.clear();
	}
	
	public void addDefaultItems() {
		BuiltInRegistries.ITEM.entrySet().stream().filter(entry -> !this.capabilities.containsKey(entry.getValue())).forEach(entry -> {
			Function<Item, ? extends CapabilityItem.Builder<?>> type = null;
			Item item = entry.getValue();
			
			if (item instanceof BlockItem) {
				return;
			}
			
			for (Map.Entry<ResourceLocation, ItemKeywordReloadListener.ItemRegex> regexEntry : ItemKeywordReloadListener.getRegexes().entrySet()) {
				if (regexEntry.getValue().matchesAny(entry.getKey().location().toString())) {
					type = WeaponTypeReloadListener.get(regexEntry.getKey());
					
					if (type != null) {
						this.capabilities.put(item, type.apply(item).build());
						break;
					}
				}
			}
			
			if (type == null) {
				Class<?> clazz = item.getClass();
				CapabilityItem capability = null;
				
				for (; clazz != null && capability == null; clazz = clazz.getSuperclass()) {
					if (this.typedCapabilities.containsKey(clazz)) {
						capability = getDefault(item);
					}
				}
				
				if (capability != null) {
					this.capabilities.put(item, capability);
				}
			}
		});
	}
	
	@Override
	public @Nullable CapabilityItem getCapability(ItemStack itemstack, Void context) {
		if (this.capabilities.containsKey(itemstack.getItem())) {
			CapabilityItem itemCapability = this.capabilities.get(itemstack.getItem());
			
			if (itemCapability instanceof RuntimeCapability) {
				return itemCapability.findRecursive(itemstack);
			}
			
			return itemCapability;
		}
		
		return null;
	}
}