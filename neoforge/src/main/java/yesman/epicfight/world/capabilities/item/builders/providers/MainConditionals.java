package yesman.epicfight.world.capabilities.item.builders.providers;

import net.minecraft.world.InteractionHand;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class MainConditionals
{
    public static ProviderConditional default1HWieldStyle = ProviderConditional.builder()
            .setType(ProviderConditionalType.DEFAULT)
            .setWieldStyle(CapabilityItem.Styles.ONE_HAND)
            .isVisibleOffHand(true)
            .build();

    public static ProviderConditional default2HWieldStyle = ProviderConditional.builder()
            .setType(ProviderConditionalType.DEFAULT)
            .isVisibleOffHand(false)
            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            .build();

    public static ProviderConditional defaultRanged = ProviderConditional.builder()
            .setType(ProviderConditionalType.DEFAULT)
            .isVisibleOffHand(false)
            .setWieldStyle(CapabilityItem.Styles.RANGED)
            .build();

    public static ProviderConditional swordShieldLS = ProviderConditional.builder()
            .setType(ProviderConditionalType.WEAPON_CATEGORY)
            .setHand(InteractionHand.OFF_HAND)
            .setCategory(CapabilityItem.WeaponCategories.SHIELD)
            .setWieldStyle(CapabilityItem.Styles.ONE_HAND)
            .isVisibleOffHand(true)
            .build();

    public static ProviderConditional liechtenauerCondition = ProviderConditional.builder()
            .setType(ProviderConditionalType.SKILL_ACTIVATION)
            .setSlot(SkillSlots.WEAPON_INNATE)
            .setSkillToCheck(EpicFightSkills.LIECHTENAUER.value())
            .isVisibleOffHand(false)
            .setWieldStyle(CapabilityItem.Styles.OCHS).build();

    public static ProviderConditional uchigatanaSheathed = ProviderConditional.builder()
            .setType(ProviderConditionalType.DATA_KEY)
            .isVisibleOffHand(false)
            .setSlot(SkillSlots.WEAPON_PASSIVE)
            .setSkillToCheck(EpicFightSkills.BATTOJUTSU_PASSIVE.value())
            .setWieldStyle(CapabilityItem.Styles.SHEATH)
            .setKey(EpicFightSkillDataKeys.SHEATH)
            .build();

    public static ProviderConditional dualDaggers = ProviderConditional.builder()
            .setType(ProviderConditionalType.WEAPON_CATEGORY)
            .setCategory(CapabilityItem.WeaponCategories.DAGGER)
            .isVisibleOffHand(true)
            .setHand(InteractionHand.OFF_HAND)
            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            .build();

    public static ProviderConditional dualSwords = ProviderConditional.builder()
            .setType(ProviderConditionalType.WEAPON_CATEGORY)
            .setCategory(CapabilityItem.WeaponCategories.SWORD)
            .isVisibleOffHand(true)
            .setHand(InteractionHand.OFF_HAND)
            .setWieldStyle(CapabilityItem.Styles.TWO_HAND)
            .build();
}
