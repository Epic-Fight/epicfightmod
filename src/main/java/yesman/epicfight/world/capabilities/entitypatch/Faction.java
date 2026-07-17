package yesman.epicfight.world.capabilities.entitypatch;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface Faction extends ExtensibleEnum {
	ExtensibleEnumManager<Faction> ENUM_MANAGER = new ExtensibleEnumManager<> ("faction");
	
	public int damageColor();
}
