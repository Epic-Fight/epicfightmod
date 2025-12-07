package yesman.epicfight.api.client.neoevent;

import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import yesman.epicfight.client.gui.screen.SkillBookScreen.TextureInfo;

public class AttributeIconRegisterEvent extends Event implements IModBusEvent {
	final Map<Holder<Attribute>, TextureInfo> registry;
	
	public AttributeIconRegisterEvent(Map<Holder<Attribute>, TextureInfo> registry) {
		this.registry = registry;
	}
	
	public void registerAttribute(Holder<Attribute> attirubte, TextureInfo textureInfo) {
		this.registry.put(attirubte, textureInfo);
	}
}
