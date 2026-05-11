package yesman.epicfight.registry.entries;

import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.deferred.ItemPresetRegister;
import yesman.epicfight.registry.deferred.holders.DeferredPreset;
import yesman.epicfight.registry.deferred.holders.DeferredWeapon;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.MapCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public final class EpicFightItemCapabilityPresets
{
    private EpicFightItemCapabilityPresets() {}
    public static final ItemPresetRegister REGISTRY = ItemPresetRegister.create(EpicFight.MODID);

    /* --- Non-Weapon Presets --- */

    public static final DeferredPreset<ArmorCapability.Builder> ARMOR = REGISTRY.registerPreset("armor",
            ArmorCapability::builder
    );

    public static final DeferredPreset<CapabilityItem.Builder<?>> MAP = REGISTRY.registerPreset("map",
            MapCapability::builder
    );

    /* --- Weapon Presets --- */

    public static final DeferredWeapon AXE = REGISTRY.registerWeapon("axe",
            () -> WeaponCapability.builder()
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.TOOLS)
                    .setTierValues(0, 10d, 0.7, 0.3)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.AXE_1H)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_1H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("axe"))
    );

    public static final DeferredWeapon SWORD = REGISTRY.registerWeapon("sword",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.SWORD)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.SWORD)
                    .setTierValues(0, 0, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.SWORD_1H)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.SWORD_DUAL)
                    .addConditionals(EpicFightProviderConditionals.DUAL_SWORDS, EpicFightProviderConditionals.DEFAULT_1H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("sword"))
    );

    public static final DeferredWeapon GREATSWORD = REGISTRY.registerWeapon("greatsword",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.GREATSWORD)
                    .collider(ColliderPreset.GREATSWORD)
                    .swingSound(EpicFightSounds.WHOOSH_BIG)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .canBePlacedOffhand(false)
                    .reach(1.0F)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.GREATSWORD_2H)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("greatsword"))
    );

    public static final DeferredWeapon LONGSWORD = REGISTRY.registerWeapon("longsword",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.LONGSWORD)
                    .collider(ColliderPreset.LONGSWORD)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .canBePlacedOffhand(true)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.LONGSWORD_1H)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.LONGSWORD_2H)
                    .addMoveset(CapabilityItem.Styles.OCHS, EpicFightMovesets.LIECHTENAUER)
                    .addConditionals(EpicFightProviderConditionals.LIECHTENAUER_CONDITION, EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE, EpicFightProviderConditionals.SHIELD_OFFHAND)
                    .addTag(EpicFight.identifier("longsword"))
    );

    public static final DeferredWeapon UCHIGATANA = REGISTRY.registerWeapon("uchigatana",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.UCHIGATANA)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.UCHIGATANA)
                    .canBePlacedOffhand(true)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.UCHIGATANA_BASE)
                    .addMoveset(CapabilityItem.Styles.SHEATH, EpicFightMovesets.UCHIGATANA_SHEATHED)
                    .addConditionals(EpicFightProviderConditionals.UCHIGATANA_SHEATHED, EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("uchigatana"))
    );

    public static final DeferredWeapon DAGGER = REGISTRY.registerWeapon("dagger",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.DAGGER)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .swingSound(EpicFightSounds.WHOOSH_SMALL)
                    .collider(ColliderPreset.DAGGER)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.DAGGER_1H)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.DAGGER_DUAL)
                    .addConditionals(EpicFightProviderConditionals.DUAL_DAGGERS, EpicFightProviderConditionals.DEFAULT_1H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("dagger"))
    );

    public static final DeferredWeapon TRIDENT = REGISTRY.registerWeapon("trident",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.TRIDENT)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.SPEAR)
                    .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.TRIDENT)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_1H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("trident"))
    );

    public static final DeferredWeapon SHIELD = REGISTRY.registerWeapon("shield",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.SHIELD)
                    .offHandAlone(true)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.SHIELD)
                    .addConditionals(EpicFightProviderConditionals.SHIELD_OFFHAND)
                    .addTag(EpicFight.identifier("shield"))
    );

    public static final DeferredWeapon PICKAXE = REGISTRY.registerWeapon("pickaxe", () ->
            WeaponCapability.builder().parent(AXE).addTag(EpicFight.identifier("pickaxe")));
    public static final DeferredWeapon SHOVEL = REGISTRY.registerWeapon("shovel", () ->
            WeaponCapability.builder().parent(AXE).addTag(EpicFight.identifier("shovel")));

    public static final DeferredWeapon HOE = REGISTRY.registerWeapon("hoe",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.HOE)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.TOOLS)
                    .setTierValues(0, 0d, -0.4, 0.1)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.SWORD_1H)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_1H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("hoe"))
    );

    /**
     * Example weapon
     */
    public static final DeferredWeapon BOKKEN = REGISTRY.registerWeapon("bokken", () ->
            WeaponCapability.builder()
                    .parent(SWORD)
                    .addMoveset(CapabilityItem.Styles.SHEATH, EpicFightMovesets.TACHI_2H)
                    .addConditionals(ProviderConditional.createSkillCondition(CapabilityItem.Styles.SHEATH, EpicFightSkills.SWORD_MASTER,
                            SkillSlots.PASSIVE1, false, false))
                    .addTag(EpicFight.identifier("bokken")));

    public static final DeferredWeapon SPEAR = REGISTRY.registerWeapon("spear",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.SPEAR)
                    .swingSound(EpicFightSounds.WHOOSH_ROD)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.SPEAR)
                    .canBePlacedOffhand(false)
                    .reach(1.0F)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.SPEAR_1H)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.SPEAR_2H)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE, EpicFightProviderConditionals.SHIELD_OFFHAND)
                    .addTag(EpicFight.identifier("spear"))
    );

    public static final DeferredWeapon TACHI = REGISTRY.registerWeapon("tachi",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.TACHI)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.TACHI)
                    .canBePlacedOffhand(true)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.TACHI_2H)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("tachi"))
    );

    public static final DeferredWeapon FIST = REGISTRY.registerWeapon("fist",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.FIST)
                    .offHandAlone(true)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.COMMON, EpicFightMovesets.GLOVE)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_1H_WIELD_STYLE)
                    .addTag(EpicFight.identifier("fist"))
    );

    public static final DeferredWeapon BOW = REGISTRY.registerWeapon("bow",
            () -> WeaponCapability.builder()
                    .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
                    .addMoveset(CapabilityItem.Styles.RANGED, EpicFightMovesets.BOW)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_RANGED)
                    .addTag(EpicFight.identifier("bow"))
    );

    public static final DeferredWeapon CROSSBOW = REGISTRY.registerWeapon("crossbow",
            () -> WeaponCapability.builder()
                    .zoomInType(CapabilityItem.ZoomInType.AIMING)
                    .addMoveset(CapabilityItem.Styles.RANGED, EpicFightMovesets.CROSSBOW)
                    .addConditionals(EpicFightProviderConditionals.DEFAULT_RANGED)
                    .addTag(EpicFight.identifier("crossbow"))
    );
}
