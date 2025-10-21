package yesman.epicfight.world.capabilities.item;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface Style extends ExtensibleEnum {
	ExtensibleEnumManager<Style> ENUM_MANAGER = new ExtensibleEnumManager<> ("style");
	
	boolean canUseOffhand();
}