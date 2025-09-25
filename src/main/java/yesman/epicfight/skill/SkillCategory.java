package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;
import yesman.epicfight.main.EpicFightMod;

public interface SkillCategory extends ExtensibleEnum {
	static final ResourceLocation DEFAULT_BOOK_ICON = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skillbook");
	
	ExtensibleEnumManager<SkillCategory> ENUM_MANAGER = new ExtensibleEnumManager<> ("skill_category");
	
	boolean shouldSave();
	
	boolean shouldSynchronize();
	
	boolean learnable();
	
	default ResourceLocation bookIcon() {
		return DEFAULT_BOOK_ICON;
	}
}