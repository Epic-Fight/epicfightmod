package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.main.EpicFightMod;

public enum SkillCategories implements SkillCategory {
	BASIC_ATTACK(false, false, false),
	DODGE(true, true, true, EpicFightMod.identifier("skillbook_dodge")),
	PASSIVE(true, true, true, EpicFightMod.identifier("skillbook_passive")),
	WEAPON_PASSIVE(false, false, false),
	WEAPON_INNATE(false, true, false),
	GUARD(true, true, true, EpicFightMod.identifier("skillbook_guard")),
	KNOCKDOWN_WAKEUP(false, false, false),
	MOVER(true, true, true, EpicFightMod.identifier("skillbook_mover")),
	IDENTITY(true, true, true, EpicFightMod.identifier("skillbook_identity"));
	
	
	final boolean shouldSave;
	final boolean shouldSyncronize;
	final boolean modifiable;
	final int id;
	final ResourceLocation bookIcon;
	
	SkillCategories(boolean shouldSave, boolean shouldSyncronizedAllPlayers, boolean modifiable) {
		this.shouldSave = shouldSave;
		this.shouldSyncronize = shouldSyncronizedAllPlayers;
		this.modifiable = modifiable;
		this.id = SkillCategory.ENUM_MANAGER.assign(this);
		this.bookIcon = SkillCategory.DEFAULT_BOOK_ICON;
	}
	
	SkillCategories(boolean shouldSave, boolean shouldSyncronizedAllPlayers, boolean modifiable, ResourceLocation bookIcon) {
		this.shouldSave = shouldSave;
		this.shouldSyncronize = shouldSyncronizedAllPlayers;
		this.modifiable = modifiable;
		this.id = SkillCategory.ENUM_MANAGER.assign(this);
		this.bookIcon = bookIcon;
	}
	
	@Override
	public boolean shouldSave() {
		return this.shouldSave;
	}
	
	@Override
	public boolean shouldSynchronize() {
		return this.shouldSyncronize;
	}
	
	@Override
	public boolean learnable() {
		return this.modifiable;
	}
	
	@Override
	public int universalOrdinal() {
		return this.id;
	}
	
	@Override
	public ResourceLocation bookIcon() {
		return this.bookIcon;
	}
}
