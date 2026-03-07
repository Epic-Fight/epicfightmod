package net.forixaim.ex_cap.modules.hooks;

import net.forixaim.ex_cap.modules.assets.Builders;
import net.forixaim.ex_cap.modules.assets.ExCapDataSets;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.event.types.registry.ExCapabilityBuilderPopulationEvent;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

public class ExCapRegistryHooks
{
    public static void registerExCapMethods(ExCapabilityBuilderPopulationEvent event)
    {
        event.registerData(Builders.SWORD, ExCapDataSets.SWORD);
        event.registerData(Builders.AXE, ExCapDataSets.AXE);
        event.registerData(Builders.PICKAXE, ExCapDataSets.PICKAXE);
        event.registerData(Builders.SHOVEL, ExCapDataSets.SHOVEL);
        event.registerData(Builders.HOE, ExCapDataSets.HOE);

        event.registerData(Builders.SPEAR, ExCapDataSets.SPEAR);
        event.registerData(Builders.GREATSWORD, ExCapDataSets.GREATSWORD);
        event.registerData(Builders.LONGSWORD, ExCapDataSets.LONGSWORD);
        event.registerData(Builders.TACHI, ExCapDataSets.TACHI);
        event.registerData(Builders.UCHIGATANA, ExCapDataSets.UCHIGATANA);
        event.registerData(Builders.DAGGER, ExCapDataSets.DAGGER);

        event.registerData(Builders.FIST, ExCapDataSets.FIST);

        event.registerData(Builders.BOW, ExCapDataSets.BOW);
        event.registerData(Builders.CROSSBOW, ExCapDataSets.CROSSBOW);
        event.registerData(Builders.TRIDENT, ExCapDataSets.TRIDENT);

        event.registerData(Builders.SHIELD, ExCapDataSets.SHIELD);
    }

    public static void registerWeaponCapabilities(WeaponCapabilityPresetRegistryEvent event) {
        event.getTypeEntry().put(EpicFight.identifier("sword"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.SWORD, item));
        event.getTypeEntry().put(EpicFight.identifier("axe"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.AXE, item));
        event.getTypeEntry().put(EpicFight.identifier("pickaxe"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.PICKAXE, item));
        event.getTypeEntry().put(EpicFight.identifier("shovel"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.SHOVEL, item));
        event.getTypeEntry().put(EpicFight.identifier("bow"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.BOW, item));
        event.getTypeEntry().put(EpicFight.identifier("crossbow"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.CROSSBOW, item));
        event.getTypeEntry().put(EpicFight.identifier("trident"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.TRIDENT, item));
        event.getTypeEntry().put(EpicFight.identifier("hoe"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.HOE, item));
        event.getTypeEntry().put(EpicFight.identifier("spear"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.SPEAR, item));
        event.getTypeEntry().put(EpicFight.identifier("greatsword"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.GREATSWORD, item));
        event.getTypeEntry().put(EpicFight.identifier("longsword"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.LONGSWORD, item));
        event.getTypeEntry().put(EpicFight.identifier("tachi"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.TACHI, item));
        event.getTypeEntry().put(EpicFight.identifier("uchigatana"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.UCHIGATANA, item));
        event.getTypeEntry().put(EpicFight.identifier("dagger"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.DAGGER, item));
        event.getTypeEntry().put(EpicFight.identifier("fist"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.FIST, item));
        event.getTypeEntry().put(EpicFight.identifier("shield"), item -> WeaponCapabilityPresets.exCapRegistration(Builders.SHIELD, item));
    }
}
