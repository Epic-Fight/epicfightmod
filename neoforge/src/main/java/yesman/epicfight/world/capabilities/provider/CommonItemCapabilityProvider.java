package yesman.epicfight.world.capabilities.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import yesman.epicfight.api.ex_cap.modules.assets.Builders;
import yesman.epicfight.api.ex_cap.modules.core.managers.BuilderManager;
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
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.MapCapability;
import yesman.epicfight.world.capabilities.item.RuntimeCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

public final class CommonItemCapabilityProvider implements ICapabilityProvider<ItemStack, Void, CapabilityItem> {
	public static final CommonItemCapabilityProvider INSTANCE = new CommonItemCapabilityProvider();
	
	private CommonItemCapabilityProvider() {}
	
	private final Map<Class<? extends Item>, Function<Item, ? extends CapabilityItem.Builder<?>>> typedCapabilities = new HashMap<> ();
	private final Map<Item, CapabilityItem> capabilities = new HashMap<> ();
	
	public void registerWeaponTypesByClass() {
		this.typedCapabilities.put(ArmorItem.class, (item) -> ArmorCapability.builder().byItem(item));
		this.typedCapabilities.put(ShieldItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.SHIELD.id()), item));
        this.typedCapabilities.put(SwordItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.SWORD.id()), item));
        this.typedCapabilities.put(PickaxeItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.PICKAXE.id()), item));
        this.typedCapabilities.put(AxeItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.AXE.id()), item));
        this.typedCapabilities.put(ShovelItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.SHOVEL.id()), item));
        this.typedCapabilities.put(HoeItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.HOE.id()), item));
        this.typedCapabilities.put(BowItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.BOW.id()), item));
        this.typedCapabilities.put(CrossbowItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.CROSSBOW.id()), item));
		this.typedCapabilities.put(MapItem.class, (item) -> MapCapability.builder());
	}
	
	public void put(Item item, CapabilityItem cap) {
		this.capabilities.put(item, cap);
	}
	
	public CapabilityItem get(Item item) {
		return capabilities.getOrDefault(item, this.typedCapabilities.containsKey(item.getClass()) ? this.typedCapabilities.get(item.getClass()).apply(item).build() : null);
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
						capability = this.typedCapabilities.get(clazz).apply(item).build();
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