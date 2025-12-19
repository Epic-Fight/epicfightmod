package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
import yesman.epicfight.main.EpicFightMod;

public interface SkillCategory extends ExtendableEnum {
    ResourceLocation DEFAULT_BOOK_ICON = EpicFightMod.identifier("skillbook");
	
	ExtendableEnumManager<SkillCategory> ENUM_MANAGER = new ExtendableEnumManager<> ("skill_category");
	
	/**
	 * Determines if the skill should be saved in NBT
	 */
	boolean shouldSave();
	
	/**
	 * Determines if the skill should be synched to clients
	 */
	boolean shouldSynchronize();
	
	/**
	 * Determines if the skill is modifiable by player, through skill books, commands, or {@link SkillEditScreen}
	 */
	boolean learnable();
	
	/**
	 * Texture location of the Skill book icon in inventory UI
	 */
	default ResourceLocation bookIcon() {
		return DEFAULT_BOOK_ICON;
	}
}