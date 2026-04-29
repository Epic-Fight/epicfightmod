package yesman.epicfight.api.ex_cap.assets;

import yesman.epicfight.api.ex_cap.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.core.managers.ConditionalManager;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditionalType;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.EpicFight;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;

public class MainConditionals
{
    public static final ConditionalEntry DEFAULT_1H_WIELD_STYLE = ConditionalManager.register(
            EpicFight.identifier("default_1h_wield_style"),
            ProviderConditional.createDefault(CapabilityItem.Styles.ONE_HAND, true));

    public static final ConditionalEntry DEFAULT_2H_WIELD_STYLE = ConditionalManager.register(
            EpicFight.identifier("default_2h_wield_style"),
            ProviderConditional.createDefault(CapabilityItem.Styles.TWO_HAND, false));

    public static final ConditionalEntry DEFAULT_RANGED = ConditionalManager.register(
            EpicFight.identifier("default_ranged"),
            ProviderConditional.createDefault(CapabilityItem.Styles.RANGED, false));

    public static final ConditionalEntry SHIELD_OFFHAND = ConditionalManager.register(
            EpicFight.identifier("shield_offhand"),
            ProviderConditional.createWeaponCategory(CapabilityItem.Styles.ONE_HAND, CapabilityItem.WeaponCategories.SHIELD, InteractionHand.OFF_HAND, true));

    public static final ConditionalEntry LIECHTENAUER_CONDITION = ConditionalManager.register(
            EpicFight.identifier("liechtenauer_condition"),
            ProviderConditional.createSkillCondition(CapabilityItem.Styles.OCHS, EpicFightSkills.LIECHTENAUER.get(), SkillSlots.WEAPON_INNATE, true, false));

    public static final ConditionalEntry UCHIGATANA_SHEATHED = ConditionalManager.register(
            EpicFight.identifier("uchigatana_sheathed"),
            ProviderConditional.createSkillDataKey(CapabilityItem.Styles.SHEATH, EpicFightSkills.BATTOJUTSU_PASSIVE.get(), SkillSlots.WEAPON_PASSIVE, EpicFightSkillDataKeys.SHEATH, false));

    public static final ConditionalEntry DUAL_DAGGERS = ConditionalManager.register(
            EpicFight.identifier("dual_daggers"),
            ProviderConditional.createWeaponCategory(CapabilityItem.Styles.TWO_HAND, CapabilityItem.WeaponCategories.DAGGER, InteractionHand.OFF_HAND, true));

    public static final ConditionalEntry DUAL_SWORDS = ConditionalManager.register(
            EpicFight.identifier("dual_swords"),
            ProviderConditional.createWeaponCategory(CapabilityItem.Styles.TWO_HAND, CapabilityItem.WeaponCategories.SWORD, InteractionHand.OFF_HAND, true));
}
