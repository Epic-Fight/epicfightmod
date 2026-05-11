package yesman.epicfight.skill;

public enum SkillSlots implements SkillSlot {
	COMBO_ATTACKS(SkillCategories.BASIC_ATTACK),
	DODGE(SkillCategories.DODGE),
	PASSIVE1(SkillCategories.PASSIVE),
	PASSIVE2(SkillCategories.PASSIVE),
	PASSIVE3(SkillCategories.PASSIVE),
	// Passive slots 4..16 added so admins can raise the maxPassiveSkills game rule above 3.
	// PASSIVE1..PASSIVE3 are kept for backwards compatibility with existing addons.
	PASSIVE4(SkillCategories.PASSIVE),
	PASSIVE5(SkillCategories.PASSIVE),
	PASSIVE6(SkillCategories.PASSIVE),
	PASSIVE7(SkillCategories.PASSIVE),
	PASSIVE8(SkillCategories.PASSIVE),
	PASSIVE9(SkillCategories.PASSIVE),
	PASSIVE10(SkillCategories.PASSIVE),
	PASSIVE11(SkillCategories.PASSIVE),
	PASSIVE12(SkillCategories.PASSIVE),
	PASSIVE13(SkillCategories.PASSIVE),
	PASSIVE14(SkillCategories.PASSIVE),
	PASSIVE15(SkillCategories.PASSIVE),
	PASSIVE16(SkillCategories.PASSIVE),
	WEAPON_PASSIVE(SkillCategories.WEAPON_PASSIVE),
	WEAPON_INNATE(SkillCategories.WEAPON_INNATE),
	GUARD(SkillCategories.GUARD),
	KNOCKDOWN_WAKEUP(SkillCategories.KNOCKDOWN_WAKEUP),
	MOVER(SkillCategories.MOVER),
	IDENTITY(SkillCategories.IDENTITY),
	;

	/// The maximum number of passive slots this mod statically declares. Used as the
	/// upper bound for the [yesman.epicfight.world.gamerule.EpicFightGameRules#MAX_PASSIVE_SKILLS]
	/// game rule so the rule never asks for more slots than actually exist.
	public static final int CORE_PASSIVE_SLOT_COUNT = 16;
	
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