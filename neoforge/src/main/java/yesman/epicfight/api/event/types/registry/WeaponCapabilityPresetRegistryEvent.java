package yesman.epicfight.api.event.types.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Map;
import java.util.function.Function;

public class WeaponCapabilityPresetRegistryEvent extends Event {
	private final Map<ResourceLocation, Function<Item, ? extends CapabilityItem.Builder<?>>> typeEntry;
	
	public WeaponCapabilityPresetRegistryEvent(Map<ResourceLocation, Function<Item, ? extends CapabilityItem.Builder<?>>> typeEntry) {
		this.typeEntry = typeEntry;
	}
	
	public Map<ResourceLocation, Function<Item, ? extends CapabilityItem.Builder<?>>> getTypeEntry() {
		return this.typeEntry;
	}
}