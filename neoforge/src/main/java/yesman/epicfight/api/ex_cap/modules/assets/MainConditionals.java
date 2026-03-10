package yesman.epicfight.api.ex_cap.modules.assets;

import yesman.epicfight.api.ex_cap.modules.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditional;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditionalType;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.EpicFight;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class MainConditionals
{
    public static final ConditionalEntry DEFAULT_1H_WIELD_STYLE =
            new ConditionalEntry(
                    EpicFight.identifier("default_1h_wield_style"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DEFAULT)
                            .setWieldStyle(CapabilityItem.Styles.ONE_HAND)
                            .isVisibleOffHand(true)
            );

    public static final ConditionalEntry DEFAULT_2H_WIELD_STYLE =
            new ConditionalEntry(
                    EpicFight.identifier("default_2h_wield_style"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DEFAULT)
                            .isVisibleOffHand(false)
                            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            );

    public static final ConditionalEntry DEFAULT_RANGED =
            new ConditionalEntry(
                    EpicFight.identifier("default_ranged"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DEFAULT)
                            .isVisibleOffHand(false)
                            .setWieldStyle(CapabilityItem.Styles.RANGED)
            );

    public static final ConditionalEntry SHIELD_OFFHAND =
            new ConditionalEntry(
                    EpicFight.identifier("shield_offhand"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.WEAPON_CATEGORY)
                            .setHand(InteractionHand.OFF_HAND)
                            .setCategory(CapabilityItem.WeaponCategories.SHIELD)
                            .setWieldStyle(CapabilityItem.Styles.ONE_HAND)
                            .isVisibleOffHand(true)
            );

    public static final ConditionalEntry LIECHTENAUER_CONDITION =
            new ConditionalEntry(
                    EpicFight.identifier("liechtenauer_condition"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.SKILL_ACTIVATION)
                            .setSlot(SkillSlots.WEAPON_INNATE)
                            .setSkillToCheck(EpicFightSkills.LIECHTENAUER.value())
                            .isVisibleOffHand(false)
                            .setWieldStyle(CapabilityItem.Styles.OCHS)
            );

    public static final ConditionalEntry UCHIGATANA_SHEATHED =
            new ConditionalEntry(
                    EpicFight.identifier("uchigatana_sheathed"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.DATA_KEY)
                            .isVisibleOffHand(false)
                            .setSlot(SkillSlots.WEAPON_PASSIVE)
                            .setSkillToCheck(EpicFightSkills.BATTOJUTSU_PASSIVE.value())
                            .setWieldStyle(CapabilityItem.Styles.SHEATH)
                            .setKey(EpicFightSkillDataKeys.SHEATH)
            );

    public static final ConditionalEntry DUAL_DAGGERS =
            new ConditionalEntry(
                    EpicFight.identifier("dual_daggers"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.WEAPON_CATEGORY)
                            .setCategory(CapabilityItem.WeaponCategories.DAGGER)
                            .isVisibleOffHand(true)
                            .setHand(InteractionHand.OFF_HAND)
                            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            );

    public static final ConditionalEntry DUAL_SWORDS =
            new ConditionalEntry(
                    EpicFight.identifier("dual_swords"),
                    ProviderConditional.builder()
                            .setType(ProviderConditionalType.WEAPON_CATEGORY)
                            .setCategory(CapabilityItem.WeaponCategories.SWORD)
                            .isVisibleOffHand(true)
                            .setHand(InteractionHand.OFF_HAND)
                            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            );
}
