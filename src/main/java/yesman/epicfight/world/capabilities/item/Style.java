package yesman.epicfight.world.capabilities.item;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

public interface Style extends ExtendableEnum {
	ExtendableEnumManager<Style> ENUM_MANAGER = new ExtendableEnumManager<> ("style");
	
	boolean canUseOffhand();
}