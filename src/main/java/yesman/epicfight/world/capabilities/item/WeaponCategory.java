package yesman.epicfight.world.capabilities.item;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

public interface WeaponCategory extends ExtendableEnum {
	ExtendableEnumManager<WeaponCategory> ENUM_MANAGER = new ExtendableEnumManager<> ("weapon_category");
}