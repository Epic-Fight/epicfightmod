package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;
import yesman.epicfight.main.EpicFightMod;

public interface SkillCategory extends ExtendableEnum {
	static final ResourceLocation DEFAULT_BOOK_ICON = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skillbook");
	
	ExtendableEnumManager<SkillCategory> ENUM_MANAGER = new ExtendableEnumManager<> ("skill_category");
	
	boolean shouldSave();
	
	boolean shouldSynchronize();
	
	boolean learnable();
	
	default ResourceLocation bookIcon() {
		return DEFAULT_BOOK_ICON;
	}
}