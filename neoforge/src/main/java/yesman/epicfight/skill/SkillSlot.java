package yesman.epicfight.skill;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface SkillSlot extends ExtensibleEnum {
	ExtensibleEnumManager<SkillSlot> ENUM_MANAGER = new ExtensibleEnumManager<> ("skill_slot");
	
	SkillCategory category();
}