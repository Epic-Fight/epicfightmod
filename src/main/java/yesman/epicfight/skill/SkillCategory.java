package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

import java.util.List;

public interface SkillCategory extends ExtendableEnum {
	ExtendableEnumManager<SkillCategory> ENUM_MANAGER = new ExtendableEnumManager<> ("skill_category");
	
	boolean shouldSave();
	List<SkillCategory> getEnums();
	
	boolean shouldSynchronize();
	
	boolean learnable();
}