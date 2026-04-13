package yesman.epicfight.gameasset.ex_cap;

import yesman.epicfight.api.ex_cap.core.data.BuilderEntry;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class Builders
{

    public static final BuilderEntry AXE = new BuilderEntry(EpicFightMod.identifier("axe"), WeaponCapability.builder()
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 10d, 0.7, 0.3)
            .addTag(EpicFightMod.identifier("axe"))
    );

    public static final BuilderEntry SWORD = new BuilderEntry(EpicFightMod.identifier("sword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SWORD)
            .setTierValues(0, 0, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("sword"))
    );

    public static final BuilderEntry HOE = new BuilderEntry(EpicFightMod.identifier("hoe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.HOE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, -0.4, 0.1)
            .addTag(EpicFightMod.identifier("hoe"))
    );

    public static final BuilderEntry PICKAXE = new BuilderEntry(EpicFightMod.identifier("pickaxe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.PICKAXE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 6d, 0.4, 0.1)
            .addTag(EpicFightMod.identifier("pickaxe"))
    );

    public static final BuilderEntry SHOVEL = new BuilderEntry(EpicFightMod.identifier("shovel"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHOVEL)
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, 0.8, 0.4)
            .addTag(EpicFightMod.identifier("shovel"))
    );

    public static final BuilderEntry SPEAR = new BuilderEntry(EpicFightMod.identifier("spear"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SPEAR)
            .swingSound(EpicFightSounds.WHOOSH_ROD.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("spear"))
    );

    public static final BuilderEntry GREATSWORD = new BuilderEntry(EpicFightMod.identifier("greatsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.GREATSWORD)
            .collider(ColliderPreset.GREATSWORD)
            .swingSound(EpicFightSounds.WHOOSH_BIG.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("greatsword"))
    );

    public static final BuilderEntry UCHIGATANA = new BuilderEntry(EpicFightMod.identifier("uchigatana"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.UCHIGATANA)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.UCHIGATANA)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("uchigatana"))
    );

    public static final BuilderEntry TACHI = new BuilderEntry(EpicFightMod.identifier("tachi"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.TACHI)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TACHI)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("tachi"))
    );

    public static final BuilderEntry DAGGER = new BuilderEntry(EpicFightMod.identifier("dagger"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.DAGGER)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .swingSound(EpicFightSounds.WHOOSH_SMALL.get())
            .collider(ColliderPreset.DAGGER)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("dagger"))
    );

    public static final BuilderEntry LONGSWORD = new BuilderEntry(EpicFightMod.identifier("longsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.LONGSWORD)
            .collider(ColliderPreset.LONGSWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("longsword"))
    );

    public static final BuilderEntry FIST = new BuilderEntry(EpicFightMod.identifier("fist"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.FIST)
            .offHandAlone(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFightMod.identifier("fist"))
    );

    public static final BuilderEntry BOW = new BuilderEntry(EpicFightMod.identifier("bow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .category(CapabilityItem.WeaponCategories.BOW)
            .addTag(EpicFightMod.identifier("bow"))
    );

    public static final BuilderEntry CROSSBOW = new BuilderEntry(EpicFightMod.identifier("crossbow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.AIMING)
            .category(CapabilityItem.WeaponCategories.CROSSBOW)
            .addTag(EpicFightMod.identifier("crossbow"))
    );

    public static final BuilderEntry TRIDENT = new BuilderEntry(EpicFightMod.identifier("trident"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .category(CapabilityItem.WeaponCategories.TRIDENT)
            .addTag(EpicFightMod.identifier("trident"))
    );

    public static final BuilderEntry SHIELD = new BuilderEntry(EpicFightMod.identifier("shield"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHIELD)
            .offHandAlone(true)
            .addTag(EpicFightMod.identifier("shield"))
    );
}