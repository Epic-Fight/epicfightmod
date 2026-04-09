package yesman.epicfight.world.capabilities.provider;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;

import net.minecraft.core.Direction;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.ex_cap.core.managers.BuilderManager;
import yesman.epicfight.gameasset.ex_cap.Builders;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.MapCapability;
import yesman.epicfight.world.capabilities.item.TagBasedSeparativeCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;

public class ItemCapabilityProvider implements ICapabilityProvider, NonNullSupplier<CapabilityItem> {
	private static final Map<Class<? extends Item>, Function<Item, CapabilityItem.Builder>> CAPABILITY_BY_CLASS = Maps.newHashMap();
	private static final Map<Item, CapabilityItem> CAPABILITIES = Maps.newHashMap();
	
	public static void registerWeaponTypesByClass() {
		CAPABILITY_BY_CLASS.put(ArmorItem.class, (item) -> ArmorCapability.builder().item(item));
        CAPABILITY_BY_CLASS.put(SwordItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.SWORD.id()), item));
        CAPABILITY_BY_CLASS.put(PickaxeItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.PICKAXE.id()), item));
        CAPABILITY_BY_CLASS.put(AxeItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.AXE.id()), item));
        CAPABILITY_BY_CLASS.put(ShovelItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.SHOVEL.id()), item));
        CAPABILITY_BY_CLASS.put(HoeItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.HOE.id()), item));
        CAPABILITY_BY_CLASS.put(BowItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.BOW.id()), item));
        CAPABILITY_BY_CLASS.put(CrossbowItem.class, item -> WeaponCapabilityPresets.exCapRegistration(BuilderManager.getEntry(Builders.CROSSBOW.id()), item));
		CAPABILITY_BY_CLASS.put(MapItem.class, (item) -> MapCapability.builder());
	}
	
	public static void put(Item item, CapabilityItem cap) {
		CAPABILITIES.put(item, cap);
	}
	
	public static CapabilityItem get(Item item) {
		return CAPABILITIES.getOrDefault(item, CAPABILITY_BY_CLASS.containsKey(item.getClass()) ? CAPABILITY_BY_CLASS.get(item.getClass()).apply(item).build() : null);
	}
	
	public static void clear() {
		CAPABILITIES.clear();
	}
	
	public static void addDefaultItems() {
		ForgeRegistries.ITEMS.getEntries().stream().filter(entry -> !CAPABILITIES.containsKey(entry.getValue())).forEach(entry -> {
			Function<Item, CapabilityItem.Builder> type = null;
			Item item = entry.getValue();
			
			if (item instanceof BlockItem) {
				return;
			}
			
			for (Map.Entry<ResourceLocation, ItemKeywordReloadListener.ItemRegex> regexEntry : ItemKeywordReloadListener.getRegexes().entrySet()) {
				if (regexEntry.getValue().matchesAny(entry.getKey().location().toString())) {
					type = WeaponTypeReloadListener.get(regexEntry.getKey());
					
					if (type != null) {
						CAPABILITIES.put(item, type.apply(item).build());
						break;
					}
				}
			}
			
			if (type == null) {
				Class<?> clazz = item.getClass();
				CapabilityItem capability = null;
				
				for (; clazz != null && capability == null; clazz = clazz.getSuperclass()) {
					if (CAPABILITY_BY_CLASS.containsKey(clazz)) {
						capability = CAPABILITY_BY_CLASS.get(clazz).apply(item).build();
					}
				}
				
				if (capability != null) {
					CAPABILITIES.put(item, capability);
				}
			}
		});
	}
	
	private CapabilityItem capability;
	private final LazyOptional<CapabilityItem> optional = LazyOptional.of(this);
	
	public ItemCapabilityProvider(ItemStack itemstack) {
		this.capability = CAPABILITIES.get(itemstack.getItem());
		
		if (this.capability instanceof TagBasedSeparativeCapability) {
			this.capability = this.capability.getResult(itemstack);
		}
	}
	
	public boolean hasCapability() {
		return this.capability != null;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == EpicFightCapabilities.CAPABILITY_ITEM ? this.optional.cast() : LazyOptional.empty();
	}
	
	@Override
	public CapabilityItem get() {
		return this.capability;
	}
}