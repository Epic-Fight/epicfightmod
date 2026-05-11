package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;
import yesman.epicfight.api.utils.ParseUtil;

import java.util.*;

public interface WeaponCategory extends ExtensibleEnum {
	ExtensibleEnumManager<WeaponCategory> ENUM_MANAGER = new ExtensibleEnumManager<> ("weapon_category");

    /// Returns a translation key
    /// For backward compatibility, this method is implemented as default
    default Component getTranslatable() {
        return Component.translatable(String.format("weapon_category.%s.%s", EpicFight.MODID, ParseUtil.toLowerCase(this.toString())));
    }

    default List<WeaponCategory> getParents() {
        return ImmutableList.of();
    }
}