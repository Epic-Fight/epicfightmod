package yesman.epicfight.gameasset;

import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.ex_cap.core.data.ItemPreset;
import yesman.epicfight.api.ex_cap.core.data.Moveset;
import yesman.epicfight.api.ex_cap.core.managers.ItemPresetManager;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.MapCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class ItemPresets
{
    public static final ItemPreset ARMOR = ItemPresetManager.register(EpicFight.identifier("armor"), ArmorCapability.builder());

    public static final ItemPreset MAP = ItemPresetManager.register(EpicFight.identifier("map"), MapCapability.builder());

    public static final ItemPreset AXE = ItemPresetManager.register(EpicFight.identifier("axe"),
            WeaponCapability.builder()
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 10d, 0.7, 0.3)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .addConditionals(ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("axe"))
    );

    public static final ItemPreset SWORD = ItemPresetManager.register(EpicFight.identifier("sword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SWORD)
            .setTierValues(0, 0, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND,  Movesets.sword2HMS)
            .addConditionals(ProviderConditionals.DUAL_SWORDS, ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("sword"))
    );

    public static final ItemPreset GREATSWORD = ItemPresetManager.register(EpicFight.identifier("greatsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.GREATSWORD)
            .collider(ColliderPreset.GREATSWORD)
            .swingSound(EpicFightSounds.WHOOSH_BIG.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.greatsword2HMS)
            .addConditionals(ProviderConditionals.DEFAULT_2H_WIELD_STYLE)
            .addTag(EpicFight.identifier("greatsword"))
    );

    public static final ItemPreset LONGSWORD = ItemPresetManager.register(EpicFight.identifier("longsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.LONGSWORD)
            .collider(ColliderPreset.LONGSWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.longsword1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.longsword2HMS)
            .addMoveSet(CapabilityItem.Styles.OCHS, Movesets.liechtenauerMS)
            .addConditionals(ProviderConditionals.LIECHTENAUER_CONDITION, ProviderConditionals.DEFAULT_2H_WIELD_STYLE, ProviderConditionals.SHIELD_OFFHAND)
            .addTag(EpicFight.identifier("longsword"))
    );

    public static final ItemPreset UCHIGATANA = ItemPresetManager.register(EpicFight.identifier("uchigatana"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.UCHIGATANA)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.UCHIGATANA)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.uchigatanaBase)
            .addMoveSet(CapabilityItem.Styles.SHEATH, Movesets.uchigatanaSheathed)
            .addConditionals(ProviderConditionals.UCHIGATANA_SHEATHED, ProviderConditionals.DEFAULT_2H_WIELD_STYLE)
            .addTag(EpicFight.identifier("uchigatana"))
    );

    public static final ItemPreset DAGGER = ItemPresetManager.register(EpicFight.identifier("dagger"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.DAGGER)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .swingSound(EpicFightSounds.WHOOSH_SMALL.get())
            .collider(ColliderPreset.DAGGER)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.dagger1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.dagger2HMS)
            .addConditionals(ProviderConditionals.DUAL_DAGGERS, ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("dagger"))
    );

    public static final ItemPreset TRIDENT = ItemPresetManager.register(EpicFight.identifier("trident"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.TRIDENT)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.tridentMS)
            .addConditionals(ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("trident"))
    );

    public static final ItemPreset SHIELD = ItemPresetManager.register(EpicFight.identifier("shield"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHIELD)
            .offHandAlone(true)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.shield)
            .addConditionals(ProviderConditionals.SHIELD_OFFHAND)
            .addTag(EpicFight.identifier("shield"))
    );

    public static final ItemPreset PICKAXE = ItemPresetManager.register(EpicFight.identifier("pickaxe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.PICKAXE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 6d, 0.4, 0.1)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .addConditionals(ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("pickaxe"))
    );

    public static final ItemPreset SHOVEL = ItemPresetManager.register(EpicFight.identifier("shovel"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHOVEL)
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, 0.8, 0.4)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .addConditionals(ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("shovel"))
    );

    public static final ItemPreset HOE = ItemPresetManager.register(EpicFight.identifier("hoe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.HOE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, -0.4, 0.1)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS)
            .addConditionals(ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("hoe"))
    );

    public static final ItemPreset SPEAR = ItemPresetManager.register(EpicFight.identifier("spear"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SPEAR)
            .swingSound(EpicFightSounds.WHOOSH_ROD.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.spear1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.spear2HMS)
            .addConditionals(ProviderConditionals.DEFAULT_2H_WIELD_STYLE, ProviderConditionals.SHIELD_OFFHAND)
            .addTag(EpicFight.identifier("spear"))
    );

    public static final ItemPreset TACHI = ItemPresetManager.register(EpicFight.identifier("tachi"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.TACHI)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TACHI)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.tachi2HMS)
            .addConditionals(ProviderConditionals.DEFAULT_2H_WIELD_STYLE)
            .addTag(EpicFight.identifier("tachi"))
    );

    public static final ItemPreset FIST = ItemPresetManager.register(EpicFight.identifier("fist"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.FIST)
            .offHandAlone(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.COMMON, Movesets.glove)
            .addConditionals(ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("fist"))
    );

    public static final ItemPreset BOKKEN = ItemPresetManager.register(EpicFight.identifier("bokken"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SWORD)
            .setTierValues(0, 0, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS)
            .addMoveSet(CapabilityItem.Styles.OCHS, Moveset.builder()
                    .addLivingMotionsRecursive(Animations.BIPED_HOLD_GREATSWORD,
                            LivingMotions.IDLE, LivingMotions.JUMP, LivingMotions.KNEEL, LivingMotions.SNEAK,
                            LivingMotions.SWIM, LivingMotions.FLY, LivingMotions.CREATIVE_FLY, LivingMotions.CREATIVE_IDLE)
                    .addLivingMotionsRecursive(Animations.BIPED_WALK_GREATSWORD,
                            LivingMotions.WALK,
                            LivingMotions.CHASE)
                    .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
                    .addLivingMotionModifier(LivingMotions.BLOCK, Animations.GREATSWORD_GUARD)
                    .addComboAttacks(
                            Animations.GREATSWORD_AUTO1,
                            Animations.GREATSWORD_AUTO2,
                            Animations.GREATSWORD_DASH,
                            Animations.GREATSWORD_AIR_SLASH
                    )
                    .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.STEEL_WHIRLWIND.get()))
            .addConditionals(ProviderConditionals.DUAL_SWORDS, ProviderConditionals.DEFAULT_1H_WIELD_STYLE)
            .addConditional(
                    ProviderConditional.createSkillCondition(CapabilityItem.Styles.OCHS, EpicFightSkills.BERSERKER.value(),
                            SkillSlots.PASSIVE1, false, false))
            .addTag(EpicFight.identifier("sword")));

    public static final ItemPreset BOW = ItemPresetManager.register(EpicFight.identifier("bow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .addMoveSet(CapabilityItem.Styles.RANGED, Movesets.bow)
            .addConditionals(ProviderConditionals.DEFAULT_RANGED)
            .addTag(EpicFight.identifier("bow"))
    );

    public static final ItemPreset CROSSBOW = ItemPresetManager.register(EpicFight.identifier("crossbow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.AIMING)
            .addMoveSet(CapabilityItem.Styles.RANGED, Movesets.crossBow)
            .addConditionals(ProviderConditionals.DEFAULT_RANGED)
            .addTag(EpicFight.identifier("crossbow"))
    );
}
