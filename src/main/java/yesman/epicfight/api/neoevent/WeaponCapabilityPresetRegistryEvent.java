package yesman.epicfight.api.neoevent;

import java.util.Map;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class WeaponCapabilityPresetRegistryEvent extends Event implements IModBusEvent {
	private final Map<ResourceLocation, Function<Item, ? extends CapabilityItem.Builder<?>>> typeEntry;
	
	public WeaponCapabilityPresetRegistryEvent(Map<ResourceLocation, Function<Item, ? extends CapabilityItem.Builder<?>>> typeEntry) {
		this.typeEntry = typeEntry;
	}
	
	public Map<ResourceLocation, Function<Item, ? extends CapabilityItem.Builder<?>>> getTypeEntry() {
		return this.typeEntry;
	}
}