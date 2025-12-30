package yesman.epicfight.api.client.event.types.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.client.gui.screen.SkillBookScreen.TextureInfo;

import java.util.Map;

public class RegisterAttributeIconEvent extends Event {
	final Map<Holder<Attribute>, TextureInfo> registry;
	
	public RegisterAttributeIconEvent(Map<Holder<Attribute>, TextureInfo> registry) {
		this.registry = registry;
	}
	
	public void registerAttribute(Holder<Attribute> attirubte, TextureInfo textureInfo) {
		this.registry.put(attirubte, textureInfo);
	}
}
