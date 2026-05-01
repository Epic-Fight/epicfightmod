package yesman.epicfight.world.capabilities.item;

import net.minecraft.network.chat.Component;
import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.main.EpicFightMod;

public interface WeaponCategory extends ExtensibleEnum {
	ExtensibleEnumManager<WeaponCategory> ENUM_MANAGER = new ExtensibleEnumManager<> ("weapon_category");

    /// Returns a translation key
    /// For backward compatibility, this method is implemented as default
    default Component getTranslatable() {
        return Component.translatable(String.format("weapon_category.%s.%s", EpicFightMod.MODID, ParseUtil.toLowerCase(this.toString())));
    }
}