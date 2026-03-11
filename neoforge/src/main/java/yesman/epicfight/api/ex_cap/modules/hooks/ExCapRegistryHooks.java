package yesman.epicfight.api.ex_cap.modules.hooks;

import yesman.epicfight.api.ex_cap.modules.assets.Builders;
import yesman.epicfight.api.ex_cap.modules.assets.ExCapDataSets;
import yesman.epicfight.api.ex_cap.modules.assets.MainConditionals;
import yesman.epicfight.api.ex_cap.modules.assets.Movesets;
import yesman.epicfight.api.ex_cap.modules.core.events.*;
import yesman.epicfight.api.ex_cap.modules.core.events.*;
import yesman.epicfight.api.ex_cap.modules.core.managers.BuilderManager;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;

public class ExCapRegistryHooks
{
    public static void registerExCapMethods(ExCapabilityBuilderPopulationEvent event)
    {
        event.registerData(Builders.SWORD.id(), ExCapDataSets.SWORD.id());
        event.registerData(Builders.AXE.id(), ExCapDataSets.AXE.id());
        event.registerData(Builders.PICKAXE.id(), ExCapDataSets.PICKAXE.id());
        event.registerData(Builders.SHOVEL.id(), ExCapDataSets.SHOVEL.id());
        event.registerData(Builders.HOE.id(), ExCapDataSets.HOE.id());
        event.registerData(Builders.SPEAR.id(), ExCapDataSets.SPEAR.id());
        event.registerData(Builders.GREATSWORD.id(), ExCapDataSets.GREATSWORD.id());
        event.registerData(Builders.LONGSWORD.id(), ExCapDataSets.LONGSWORD.id());
        event.registerData(Builders.TACHI.id(), ExCapDataSets.TACHI.id());
        event.registerData(Builders.UCHIGATANA.id(), ExCapDataSets.UCHIGATANA.id());
        event.registerData(Builders.DAGGER.id(), ExCapDataSets.DAGGER.id());
        event.registerData(Builders.FIST.id(), ExCapDataSets.FIST.id());
        event.registerData(Builders.BOW.id(), ExCapDataSets.BOW.id());
        event.registerData(Builders.CROSSBOW.id(), ExCapDataSets.CROSSBOW.id());
        event.registerData(Builders.TRIDENT.id(), ExCapDataSets.TRIDENT.id());
        event.registerData(Builders.SHIELD.id(), ExCapDataSets.SHIELD.id());
    }

    public static void registerData(ExCapDataRegistrationEvent event)
    {
        event.addData(
                ExCapDataSets.SWORD,
                ExCapDataSets.AXE,
                ExCapDataSets.PICKAXE,
                ExCapDataSets.SHOVEL,
                ExCapDataSets.DAGGER,
                ExCapDataSets.SPEAR,
                ExCapDataSets.GREATSWORD,
                ExCapDataSets.LONGSWORD,
                ExCapDataSets.TACHI,
                ExCapDataSets.UCHIGATANA,
                ExCapDataSets.HOE,
                ExCapDataSets.BOW,
                ExCapDataSets.CROSSBOW,
                ExCapDataSets.FIST,
                ExCapDataSets.SHIELD,
                ExCapDataSets.TRIDENT
        );
    }

    public static void registerExCapBuilders(ExCapBuilderCreationEvent event)
    {
        event.addBuilder(
                Builders.GREATSWORD,
                Builders.AXE,
                Builders.SWORD,
                Builders.SPEAR,
                Builders.SHOVEL,
                Builders.PICKAXE,
                Builders.HOE,
                Builders.UCHIGATANA,
                Builders.TACHI,
                Builders.DAGGER,
                Builders.LONGSWORD,
                Builders.FIST,
                Builders.BOW,
                Builders.CROSSBOW,
                Builders.TRIDENT,
                Builders.SHIELD
        );
    }

    public static void registerConditionals(ConditionalRegistryEvent event)
    {
        event.addConditional(
                MainConditionals.DEFAULT_1H_WIELD_STYLE,
                MainConditionals.DEFAULT_2H_WIELD_STYLE,
                MainConditionals.DEFAULT_RANGED,
                MainConditionals. SHIELD_OFFHAND,
                MainConditionals.LIECHTENAUER_CONDITION,
                MainConditionals.UCHIGATANA_SHEATHED,
                MainConditionals.DUAL_DAGGERS,
                MainConditionals. DUAL_SWORDS);
    }

    public static void registerExCapMovesets(ExCapMovesetRegistryEvent event)
    {
        event.addMoveSet(
                Movesets.commonShield,
                Movesets.mountedSwordMS,
                Movesets.greatsword2HMS,
                Movesets.axeOneHandMS,
                Movesets.mountedSpearMS,
                Movesets.longsword2HMS,
                Movesets.longsword1HMS,
                Movesets.liechtenauerMS,
                Movesets.dagger1HMS,
                Movesets.dagger2HMS,
                Movesets.spear2HMS,
                Movesets.spear1HMS,
                Movesets.sword1HMS,
                Movesets.sword2HMS,
                Movesets.shield,
                Movesets.tachi2HMS,
                Movesets.uchigatanaBase,
                Movesets.uchigatanaSheathed,
                Movesets.glove,
                Movesets.bow,
                Movesets.crossBow,
                Movesets.tridentMS
        );
    }


    public static void registerWeaponCapabilities(WeaponCapabilityPresetRegistryEvent event) {
        BuilderManager.acceptExport(event);
    }
}
