package yesman.epicfight.gameasset.ex_cap;

import yesman.epicfight.api.ex_cap.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditionalType;
import net.minecraft.world.InteractionHand;

import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class MainConditionals
{
    public static final ConditionalEntry DEFAULT_1H_WIELD_STYLE =
            new ConditionalEntry(
                    EpicFightMod.identifier("default_1h_wield_style"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DEFAULT)
                            .setWieldStyle(CapabilityItem.Styles.ONE_HAND)
                            .isVisibleOffHand(true)
            );

    public static final ConditionalEntry DEFAULT_2H_WIELD_STYLE =
            new ConditionalEntry(
                    EpicFightMod.identifier("default_2h_wield_style"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DEFAULT)
                            .isVisibleOffHand(false)
                            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            );

    public static final ConditionalEntry DEFAULT_RANGED =
            new ConditionalEntry(
                    EpicFightMod.identifier("default_ranged"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DEFAULT)
                            .isVisibleOffHand(false)
                            .setWieldStyle(CapabilityItem.Styles.RANGED)
            );

    public static final ConditionalEntry SHIELD_OFFHAND =
            new ConditionalEntry(
                    EpicFightMod.identifier("shield_offhand"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.WEAPON_CATEGORY)
                            .setHand(InteractionHand.OFF_HAND)
                            .setCategory(CapabilityItem.WeaponCategories.SHIELD)
                            .setWieldStyle(CapabilityItem.Styles.ONE_HAND)
                            .isVisibleOffHand(true)
            );

    public static final ConditionalEntry LIECHTENAUER_CONDITION =
            new ConditionalEntry(
                    EpicFightMod.identifier("liechtenauer_condition"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.SKILL_ACTIVATION)
                            .setSlot(SkillSlots.WEAPON_INNATE)
                            .setSkillToCheck(EpicFightSkills.LIECHTENAUER)
                            .isVisibleOffHand(false)
                            .setWieldStyle(CapabilityItem.Styles.OCHS)
            );

    public static final ConditionalEntry UCHIGATANA_SHEATHED =
            new ConditionalEntry(
                    EpicFightMod.identifier("uchigatana_sheathed"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DATA_KEY)
                            .isVisibleOffHand(false)
                            .setSlot(SkillSlots.WEAPON_PASSIVE)
                            .setSkillToCheck(EpicFightSkills.BATTOJUTSU_PASSIVE)
                            .setWieldStyle(CapabilityItem.Styles.SHEATH)
                            .setKey(SkillDataKeys.SHEATH.get())
            );

    public static final ConditionalEntry DUAL_DAGGERS =
            new ConditionalEntry(
                    EpicFightMod.identifier("dual_daggers"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.WEAPON_CATEGORY)
                            .setCategory(CapabilityItem.WeaponCategories.DAGGER)
                            .isVisibleOffHand(true)
                            .setHand(InteractionHand.OFF_HAND)
                            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            );

    public static final ConditionalEntry DUAL_SWORDS =
            new ConditionalEntry(
                    EpicFightMod.identifier("dual_swords"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.WEAPON_CATEGORY)
                            .setCategory(CapabilityItem.WeaponCategories.SWORD)
                            .isVisibleOffHand(true)
                            .setHand(InteractionHand.OFF_HAND)
                            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            );
}