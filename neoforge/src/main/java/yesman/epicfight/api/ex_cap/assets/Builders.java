package yesman.epicfight.api.ex_cap.assets;

import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.core.data.BuilderEntry;
import yesman.epicfight.api.ex_cap.core.managers.BuilderManager;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.MapCapability;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class Builders
{
    public static final BuilderEntry ARMOR = BuilderManager.register(EpicFight.identifier("armor"), ArmorCapability.builder());

    public static final BuilderEntry MAP = BuilderManager.register(EpicFight.identifier("map"), MapCapability.builder());

    public static final BuilderEntry AXE = BuilderManager.register(EpicFight.identifier("axe"),
            WeaponCapability.builder()
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 10d, 0.7, 0.3)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .addConditionals(MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("axe"))
    );

    public static final BuilderEntry SWORD = BuilderManager.register(EpicFight.identifier("sword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SWORD)
            .setTierValues(0, 0, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.sword2HMS)
            .addConditionals(MainConditionals.DUAL_SWORDS, MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("sword"))
    );

    public static final BuilderEntry GREATSWORD = BuilderManager.register(EpicFight.identifier("greatsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.GREATSWORD)
            .collider(ColliderPreset.GREATSWORD)
            .swingSound(EpicFightSounds.WHOOSH_BIG.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.greatsword2HMS)
            .addConditionals(MainConditionals.DEFAULT_2H_WIELD_STYLE)
            .addTag(EpicFight.identifier("greatsword"))
    );

    public static final BuilderEntry LONGSWORD = BuilderManager.register(EpicFight.identifier("longsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.LONGSWORD)
            .collider(ColliderPreset.LONGSWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.longsword1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.longsword2HMS)
            .addMoveSet(CapabilityItem.Styles.OCHS, Movesets.liechtenauerMS)
            .addConditionals(MainConditionals.LIECHTENAUER_CONDITION, MainConditionals.DEFAULT_2H_WIELD_STYLE, MainConditionals.SHIELD_OFFHAND)
            .addTag(EpicFight.identifier("longsword"))
    );

    public static final BuilderEntry UCHIGATANA = BuilderManager.register(EpicFight.identifier("uchigatana"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.UCHIGATANA)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.UCHIGATANA)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.uchigatanaBase)
            .addMoveSet(CapabilityItem.Styles.SHEATH, Movesets.uchigatanaSheathed)
            .addConditionals(MainConditionals.UCHIGATANA_SHEATHED, MainConditionals.DEFAULT_2H_WIELD_STYLE)
            .addTag(EpicFight.identifier("uchigatana"))
    );

    public static final BuilderEntry DAGGER = BuilderManager.register(EpicFight.identifier("dagger"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.DAGGER)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .swingSound(EpicFightSounds.WHOOSH_SMALL.get())
            .collider(ColliderPreset.DAGGER)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.dagger1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.dagger2HMS)
            .addConditionals(MainConditionals.DUAL_DAGGERS, MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("dagger"))
    );

    public static final BuilderEntry TRIDENT = BuilderManager.register(EpicFight.identifier("trident"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.TRIDENT)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.tridentMS)
            .addConditionals(MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("trident"))
    );

    public static final BuilderEntry SHIELD = BuilderManager.register(EpicFight.identifier("shield"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHIELD)
            .offHandAlone(true)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.shield)
            .addConditionals(MainConditionals.SHIELD_OFFHAND)
            .addTag(EpicFight.identifier("shield"))
    );

    public static final BuilderEntry PICKAXE = BuilderManager.register(EpicFight.identifier("pickaxe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.PICKAXE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 6d, 0.4, 0.1)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .addConditionals(MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("pickaxe"))
    );

    public static final BuilderEntry SHOVEL = BuilderManager.register(EpicFight.identifier("shovel"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHOVEL)
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, 0.8, 0.4)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .addConditionals(MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("shovel"))
    );

    public static final BuilderEntry HOE = BuilderManager.register(EpicFight.identifier("hoe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.HOE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, -0.4, 0.1)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .addConditionals(MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("hoe"))
    );

    public static final BuilderEntry SPEAR = BuilderManager.register(EpicFight.identifier("spear"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SPEAR)
            .swingSound(EpicFightSounds.WHOOSH_ROD.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.spear1HMS)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.spear2HMS)
            .addConditionals(MainConditionals.DEFAULT_2H_WIELD_STYLE, MainConditionals.SHIELD_OFFHAND)
            .addTag(EpicFight.identifier("spear"))
    );

    public static final BuilderEntry TACHI = BuilderManager.register(EpicFight.identifier("tachi"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.TACHI)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TACHI)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.TWO_HAND, Movesets.tachi2HMS)
            .addConditionals(MainConditionals.DEFAULT_2H_WIELD_STYLE)
            .addTag(EpicFight.identifier("tachi"))
    );

    public static final BuilderEntry FIST = BuilderManager.register(EpicFight.identifier("fist"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.FIST)
            .offHandAlone(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addMoveSet(CapabilityItem.Styles.COMMON, Movesets.glove)
            .addConditionals(MainConditionals.DEFAULT_1H_WIELD_STYLE)
            .addTag(EpicFight.identifier("fist"))
    );

    public static final BuilderEntry BOW = BuilderManager.register(EpicFight.identifier("bow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .addMoveSet(CapabilityItem.Styles.RANGED, Movesets.bow)
            .addConditionals(MainConditionals.DEFAULT_RANGED)
            .addTag(EpicFight.identifier("bow"))
    );

    public static final BuilderEntry CROSSBOW = BuilderManager.register(EpicFight.identifier("crossbow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.AIMING)
            .addMoveSet(CapabilityItem.Styles.RANGED, Movesets.crossBow)
            .addConditionals(MainConditionals.DEFAULT_RANGED)
            .addTag(EpicFight.identifier("crossbow"))
    );
}
