package yesman.epicfight.world.capabilities.item;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface WeaponCategory extends ExtensibleEnum {
	ExtensibleEnumManager<WeaponCategory> ENUM_MANAGER = new ExtensibleEnumManager<> ("weapon_category");
}