package yesman.epicfight.skill;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public enum SkillCategories implements SkillCategory {
	BASIC_ATTACK(false, false, false),
	AIR_ATTACK(false, false, false),
	DODGE(true, true, true),
	PASSIVE(true, true, true),
	WEAPON_PASSIVE(false, false, false),
	WEAPON_INNATE(false, true, false),
	GUARD(true, true, true),
	KNOCKDOWN_WAKEUP(false, false, false),
	MOVER(true, true, true),
	IDENTITY(true, true, true);
	
	boolean shouldSave;
	boolean shouldSyncronize;
	boolean modifiable;
	int id;
	
	SkillCategories(boolean shouldSave, boolean shouldSyncronizedAllPlayers, boolean modifiable) {
		this.shouldSave = shouldSave;
		this.shouldSyncronize = shouldSyncronizedAllPlayers;
		this.modifiable = modifiable;
		this.id = SkillCategory.ENUM_MANAGER.assign(this);
	}

	public boolean shouldSave() {
		return this.shouldSave;
	}
	
	public boolean shouldSynchronize() {
		return this.shouldSyncronize;
	}
	
	public boolean learnable() {
		return this.modifiable;
	}

	@Override
	public int universalOrdinal() {
		return this.id;
	}
}