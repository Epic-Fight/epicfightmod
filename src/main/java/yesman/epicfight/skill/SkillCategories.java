package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.main.EpicFightMod;

public enum SkillCategories implements SkillCategory {
	BASIC_ATTACK(false, false, false),
	AIR_ATTACK(false, false, false),
	DODGE(true, true, true, ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skillbook_dodge")),
	PASSIVE(true, true, true, ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skillbook_passive")),
	WEAPON_PASSIVE(false, false, false),
	WEAPON_INNATE(false, true, false),
	GUARD(true, true, true, ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skillbook_guard")),
	KNOCKDOWN_WAKEUP(false, false, false),
	MOVER(true, true, true, ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skillbook_mover")),
	IDENTITY(true, true, true, ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skillbook_identity"));
	
	
	boolean shouldSave;
	boolean shouldSyncronize;
	boolean modifiable;
	int id;
	ResourceLocation bookIcon;
	
	SkillCategories(boolean shouldSave, boolean shouldSyncronizedAllPlayers, boolean modifiable) {
		this.shouldSave = shouldSave;
		this.shouldSyncronize = shouldSyncronizedAllPlayers;
		this.modifiable = modifiable;
		this.id = SkillCategory.ENUM_MANAGER.assign(this);
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
		return this.bookIcon == null ? SkillCategory.DEFAULT_BOOK_ICON : this.bookIcon;
	}
}
