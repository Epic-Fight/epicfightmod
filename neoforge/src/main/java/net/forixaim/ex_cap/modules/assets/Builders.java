package net.forixaim.ex_cap.modules.assets;

import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.item.*;

public class Builders
{
    public static final WeaponCapability.Builder AXE = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.AXE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 10d, 0.7, 0.3);

    public static final WeaponCapability.Builder SWORD = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SWORD)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.SWORD)
            .setTierValues(0, 0, 0.0, 0.0);

    public static final WeaponCapability.Builder HOE = WeaponCapability.builder()
                .category(CapabilityItem.WeaponCategories.HOE)
                .hitSound(EpicFightSounds.BLADE_HIT.get())
                .collider(ColliderPreset.TOOLS)
                .setTierValues(0, 0d, -0.4, 0.1);

    public static final WeaponCapability.Builder PICKAXE = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.PICKAXE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 6d, 0.4, 0.1);

    public static final WeaponCapability.Builder SHOVEL = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHOVEL)
            .collider(ColliderPreset.TOOLS)
            .setTierValues(0, 0d, 0.8, 0.4);

    public static final WeaponCapability.Builder SPEAR = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SPEAR)
            .swingSound(EpicFightSounds.WHOOSH_ROD.get())
            .collider(ColliderPreset.SPEAR)
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0);

    public static final WeaponCapability.Builder GREATSWORD = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.GREATSWORD)
            .collider(ColliderPreset.GREATSWORD)
            .swingSound(EpicFightSounds.WHOOSH_BIG.get())
            .canBePlacedOffhand(false)
            .reach(1.0F)
            .setTierValues(0, 0d, 0.0, 0.0);

    public static final WeaponCapability.Builder UCHIGATANA = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.UCHIGATANA)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.UCHIGATANA)
            .canBePlacedOffhand(false)
            .setTierValues(0, 0d, 0.0, 0.0);

    public static final WeaponCapability.Builder TACHI = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.TACHI)
            .collider(ColliderPreset.TACHI)
            .canBePlacedOffhand(false)
            .setTierValues(0, 0d, 0.0, 0.0);

    public static final WeaponCapability.Builder DAGGER = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.DAGGER)
            .swingSound(EpicFightSounds.WHOOSH_SMALL.get())
            .collider(ColliderPreset.DAGGER)
            .setTierValues(0, 0d, 0.0, 0.0);

    public static final WeaponCapability.Builder LONGSWORD = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.LONGSWORD)
            .collider(ColliderPreset.LONGSWORD)
            .canBePlacedOffhand(false)
            .setTierValues(0, 0d, 0.0, 0.0);

    public static final WeaponCapability.Builder FIST = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.FIST)
            .setTierValues(0, 0d, 0.0, 0.0);

    public static final WeaponCapability.Builder BOW =
            WeaponCapability.builder()
                    .zoomInType(CapabilityItem.ZoomInType.USE_TICK);

    public static final WeaponCapability.Builder CROSSBOW =
            WeaponCapability.builder()
                    .zoomInType(CapabilityItem.ZoomInType.AIMING);

    public static final WeaponCapability.Builder TRIDENT =
            WeaponCapability.builder()
                    .zoomInType(CapabilityItem.ZoomInType.USE_TICK)
                    .collider(ColliderPreset.SPEAR)
                    .category(CapabilityItem.WeaponCategories.TRIDENT);

    public static final WeaponCapability.Builder SHIELD = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.SHIELD);

}
