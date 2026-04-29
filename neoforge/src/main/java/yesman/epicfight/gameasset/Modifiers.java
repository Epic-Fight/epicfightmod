package yesman.epicfight.gameasset;

import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.core.data.modifier.WeaponModifier;
import yesman.epicfight.api.ex_cap.core.managers.ModifierManager;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class Modifiers
{
    public static WeaponModifier BOKKEN_MODIFIER = ModifierManager.register(EpicFight.identifier("bokken_mod"), WeaponModifier.builder()
            .setTarget(ItemPresets.BOKKEN).modifyMoveset(CapabilityItem.Styles.SHEATH, Movesets.greatsword2HMS)
            .addConditional(ProviderConditional.createSkillCondition(CapabilityItem.Styles.SHEATH, EpicFightSkills.SWORD_MASTER .value(),
                    SkillSlots.PASSIVE1, false, false)));
}
