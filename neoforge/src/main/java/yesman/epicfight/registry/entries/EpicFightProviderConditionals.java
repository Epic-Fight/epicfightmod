package yesman.epicfight.registry.entries;

import net.minecraft.world.InteractionHand;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.registry.deferred.ProviderConditionalRegister;
import yesman.epicfight.registry.deferred.holders.DeferredConditional;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public final class EpicFightProviderConditionals {
    private EpicFightProviderConditionals() {}

    public static final ProviderConditionalRegister REGISTRY = ProviderConditionalRegister.create(EpicFight.MODID);

    public static final DeferredConditional DEFAULT_1H_WIELD_STYLE = REGISTRY.register(
            "default_1h_wield_style",
            ProviderConditional.createDefault(CapabilityItem.Styles.ONE_HAND, true)
    );

    public static final DeferredConditional DEFAULT_2H_WIELD_STYLE = REGISTRY.register(
            "default_2h_wield_style",
            ProviderConditional.createDefault(CapabilityItem.Styles.TWO_HAND, false)
    );

    public static final DeferredConditional DEFAULT_RANGED = REGISTRY.register(
            "default_ranged",
            ProviderConditional.createDefault(CapabilityItem.Styles.RANGED, false)
    );

    public static final DeferredConditional SHIELD_OFFHAND = REGISTRY.register(
            "shield_offhand",
            ProviderConditional.createWeaponCategory(CapabilityItem.Styles.ONE_HAND, CapabilityItem.WeaponCategories.SHIELD, InteractionHand.OFF_HAND, true)
    );

    public static final DeferredConditional LIECHTENAUER_CONDITION = REGISTRY.register(
            "liechtenauer_condition",
            ProviderConditional.createSkillCondition(CapabilityItem.Styles.OCHS, EpicFightSkills.LIECHTENAUER, SkillSlots.WEAPON_INNATE, true, false)
    );

    public static final DeferredConditional UCHIGATANA_SHEATHED = REGISTRY.register(
            "uchigatana_sheathed",
            ProviderConditional.createSkillDataKey(CapabilityItem.Styles.SHEATH, EpicFightSkills.BATTOJUTSU_PASSIVE, SkillSlots.WEAPON_PASSIVE, EpicFightSkillDataKeys.SHEATH, false)
    );

    public static final DeferredConditional DUAL_DAGGERS = REGISTRY.register(
            "dual_daggers",
            ProviderConditional.createWeaponCategory(CapabilityItem.Styles.TWO_HAND, CapabilityItem.WeaponCategories.DAGGER, InteractionHand.OFF_HAND, true)
    );

    public static final DeferredConditional DUAL_SWORDS = REGISTRY.register(
            "dual_swords",
            ProviderConditional.createWeaponCategory(CapabilityItem.Styles.TWO_HAND, CapabilityItem.WeaponCategories.SWORD, InteractionHand.OFF_HAND, true)
    );
}