package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface Faction extends ExtensibleEnum {
	ExtensibleEnumManager<Faction> ENUM_MANAGER = new ExtensibleEnumManager<> ("faction");
	
	public ResourceLocation healthBarTexture();
	
	public int healthBarIndex();
	
	public int damageColor();
}