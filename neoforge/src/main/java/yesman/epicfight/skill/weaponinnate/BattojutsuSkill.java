package yesman.epicfight.skill.weaponinnate;

import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class BattojutsuSkill extends ConditionalWeaponInnateSkill {
	public BattojutsuSkill(ConditionalWeaponInnateSkill.Builder builder) {
		super(builder);
	}
	
	@Override
	public void playSkillAnimation(ServerPlayerPatch executor) {
		boolean isSheathed = executor.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().getDataValue(EpicFightSkillDataKeys.SHEATH);
		
		if (isSheathed) {
			executor.playAnimationSynchronized(this.attackAnimations[this.getAnimationInCondition(executor)], -0.65F);
		} else {
			executor.playAnimationSynchronized(this.attackAnimations[this.getAnimationInCondition(executor)], 0);
		}
	}
}