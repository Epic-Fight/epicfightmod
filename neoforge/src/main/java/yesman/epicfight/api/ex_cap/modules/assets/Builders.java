package yesman.epicfight.api.ex_cap.modules.assets;

import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.modules.core.data.BuilderEntry;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class Builders
{

    public static final BuilderEntry AXE = new BuilderEntry(EpicFight.identifier("axe"), WeaponCapability.builder()
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 10d, 0.7, 0.3)
            .addTag(EpicFight.identifier("axe"))
    );

    public static final BuilderEntry SWORD = new BuilderEntry(EpicFight.identifier("sword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SWORD)
            .setTierValues(0, 0, 0.0, 0.0)
            .addTag(EpicFight.identifier("sword"))
    );

    public static final BuilderEntry HOE = new BuilderEntry(EpicFight.identifier("hoe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.HOE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, -0.4, 0.1)
            .addTag(EpicFight.identifier("hoe"))
    );

    public static final BuilderEntry PICKAXE = new BuilderEntry(EpicFight.identifier("pickaxe"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.PICKAXE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 6d, 0.4, 0.1)
            .addTag(EpicFight.identifier("pickaxe"))
    );

    public static final BuilderEntry SHOVEL = new BuilderEntry(EpicFight.identifier("shovel"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHOVEL)
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, 0.8, 0.4)
            .addTag(EpicFight.identifier("shovel"))
    );

    public static final BuilderEntry SPEAR = new BuilderEntry(EpicFight.identifier("spear"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SPEAR)
            .swingSound(EpicFightSounds.WHOOSH_ROD.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFight.identifier("spear"))
    );

    public static final BuilderEntry GREATSWORD = new BuilderEntry(EpicFight.identifier("greatsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.GREATSWORD)
            .collider(ColliderPreset.GREATSWORD)
            .swingSound(EpicFightSounds.WHOOSH_BIG.get())
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFight.identifier("greatsword"))
    );

    public static final BuilderEntry UCHIGATANA = new BuilderEntry(EpicFight.identifier("uchigatana"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.UCHIGATANA)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.UCHIGATANA)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFight.identifier("uchigatana"))
    );

    public static final BuilderEntry TACHI = new BuilderEntry(EpicFight.identifier("tachi"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.TACHI)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TACHI)
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFight.identifier("tachi"))
    );

    public static final BuilderEntry DAGGER = new BuilderEntry(EpicFight.identifier("dagger"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.DAGGER)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .swingSound(EpicFightSounds.WHOOSH_SMALL.get())
            .collider(ColliderPreset.DAGGER)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFight.identifier("dagger"))
    );

    public static final BuilderEntry LONGSWORD = new BuilderEntry(EpicFight.identifier("longsword"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.LONGSWORD)
            .collider(ColliderPreset.LONGSWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .canBePlacedOffhand(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFight.identifier("longsword"))
    );

    public static final BuilderEntry FIST = new BuilderEntry(EpicFight.identifier("fist"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.FIST)
            .offHandAlone(true)
            .setTierValues(0, 0d, 0.0, 0.0)
            .addTag(EpicFight.identifier("fist"))
    );

    public static final BuilderEntry BOW = new BuilderEntry(EpicFight.identifier("bow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .addTag(EpicFight.identifier("bow"))
    );

    public static final BuilderEntry CROSSBOW = new BuilderEntry(EpicFight.identifier("crossbow"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.AIMING)
            .addTag(EpicFight.identifier("crossbow"))
    );

    public static final BuilderEntry TRIDENT = new BuilderEntry(EpicFight.identifier("trident"), WeaponCapability.builder()
            .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SPEAR)
            .category(CapabilityItem.WeaponCategories.TRIDENT)
            .addTag(EpicFight.identifier("trident"))
    );

    public static final BuilderEntry SHIELD = new BuilderEntry(EpicFight.identifier("shield"), WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHIELD)
            .offHandAlone(true)
            .addTag(EpicFight.identifier("shield"))
    );
}
