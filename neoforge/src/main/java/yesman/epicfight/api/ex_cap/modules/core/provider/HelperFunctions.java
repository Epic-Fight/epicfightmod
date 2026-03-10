package yesman.epicfight.api.ex_cap.modules.core.provider;

import net.minecraft.world.InteractionHand;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class HelperFunctions
{
	//Initial Helper Functions
	public static boolean itemCheck(LivingEntityPatch<?> entityPatch, WeaponCategory category, InteractionHand hand)
	{
		return entityPatch.getHoldingItemCapability(hand).getWeaponCategory() == category;
	}
	public static boolean skillCheck(LivingEntityPatch<?> entityPatch, Skill skill, SkillSlot slot)
	{
		if (entityPatch instanceof PlayerPatch<?> patch)
		{
			if (slot == null)
				return (patch.getSkill(skill)) != null;
			return patch.getSkill(slot).hasSkill(skill);
		}
		return false;
	}
}
