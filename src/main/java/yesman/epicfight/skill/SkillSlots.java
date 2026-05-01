package yesman.epicfight.skill;

public enum SkillSlots implements SkillSlot {
	COMBO_ATTACKS(SkillCategories.BASIC_ATTACK),
	DODGE(SkillCategories.DODGE),
	PASSIVE1(SkillCategories.PASSIVE),
	PASSIVE2(SkillCategories.PASSIVE),
	PASSIVE3(SkillCategories.PASSIVE),
	WEAPON_PASSIVE(SkillCategories.WEAPON_PASSIVE),
	WEAPON_INNATE(SkillCategories.WEAPON_INNATE),
	GUARD(SkillCategories.GUARD),
	KNOCKDOWN_WAKEUP(SkillCategories.KNOCKDOWN_WAKEUP),
	MOVER(SkillCategories.MOVER),
	IDENTITY(SkillCategories.IDENTITY),
	;
	
	SkillCategory category;
	int id;
	
	SkillSlots(SkillCategory category) {
		this.category = category;
		this.id = SkillSlot.ENUM_MANAGER.assign(this);
	}
	
	@Override
	public SkillCategory category() {
		return this.category;
	}
	
	@Override
	public int universalOrdinal() {
		return this.id;
	}
}