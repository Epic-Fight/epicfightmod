package yesman.epicfight.world.capabilities;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BaseCapability;

public class GlobalCapability<T> extends BaseCapability<T, Void> {
	public GlobalCapability(ResourceLocation name, Class<T> type) {
		super(name, type, void.class);
	}
}
