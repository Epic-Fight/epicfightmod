package yesman.epicfight.api.client.forgeevent;

import java.util.Map;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.client.gui.screen.SkillBookScreen.TextureInfo;

public class AttributeIconRegisterEvent extends Event implements IModBusEvent {
	final Map<Attribute, TextureInfo> registry;
	
	public AttributeIconRegisterEvent(Map<Attribute, TextureInfo> registry) {
		this.registry = registry;
	}
	
	public void registerAttribute(Attribute attirubte, TextureInfo textureInfo) {
		this.registry.put(attirubte, textureInfo);
	}
}
