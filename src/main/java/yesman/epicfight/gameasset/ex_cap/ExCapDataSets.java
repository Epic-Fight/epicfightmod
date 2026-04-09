package yesman.epicfight.gameasset.ex_cap;

import yesman.epicfight.api.ex_cap.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.core.data.ExCapDataEntry;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ExCapDataSets
{
    public static final ExCapDataEntry SWORD = new ExCapDataEntry(EpicFightMod.identifier("sword"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id(), MainConditionals.DUAL_SWORDS.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.sword2HMS.id())
    );

    public static final ExCapDataEntry AXE = new ExCapDataEntry(EpicFightMod.identifier("axe"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            );

    public static final ExCapDataEntry PICKAXE = new ExCapDataEntry(EpicFightMod.identifier("pickaxe"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            );

    public static final ExCapDataEntry SHOVEL = new ExCapDataEntry(EpicFightMod.identifier("shovel"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            );

    public static final ExCapDataEntry DAGGER = new ExCapDataEntry(EpicFightMod.identifier("dagger"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id(), MainConditionals.DUAL_DAGGERS.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.dagger1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.dagger2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            );

    public static final ExCapDataEntry SPEAR = new ExCapDataEntry(EpicFightMod.identifier("spear"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id(), MainConditionals.SHIELD_OFFHAND.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.spear1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.spear2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSpearMS.id())
            );

    public static final ExCapDataEntry GREATSWORD = new ExCapDataEntry(EpicFightMod.identifier("greatsword"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.greatsword2HMS.id())
            );

    public static final ExCapDataEntry LONGSWORD = new ExCapDataEntry(EpicFightMod.identifier("longsword"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id(), MainConditionals.SHIELD_OFFHAND.id(), MainConditionals.LIECHTENAUER_CONDITION.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.longsword1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.longsword2HMS.id())
            .addMoveset(CapabilityItem.Styles.OCHS, Movesets.liechtenauerMS.id())
            );

    public static final ExCapDataEntry TACHI = new ExCapDataEntry(EpicFightMod.identifier("tachi"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.tachi2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            );

    public static final ExCapDataEntry UCHIGATANA = new ExCapDataEntry(EpicFightMod.identifier("uchigatana"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id(), MainConditionals.UCHIGATANA_SHEATHED.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.uchigatanaBase.id())
            .addMoveset(CapabilityItem.Styles.SHEATH, Movesets.uchigatanaSheathed.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            );

    public static final ExCapDataEntry HOE = new ExCapDataEntry(EpicFightMod.identifier("hoe"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS.id())
            );

    public static final ExCapDataEntry BOW = new ExCapDataEntry(EpicFightMod.identifier("bow"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.bow.id())
            );

    public static final ExCapDataEntry CROSSBOW = new ExCapDataEntry(EpicFightMod.identifier("crossbow"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.crossBow.id())
            );

    public static final ExCapDataEntry FIST = new ExCapDataEntry(EpicFightMod.identifier("fist"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.glove.id())
            );

    public static final ExCapDataEntry SHIELD = new ExCapDataEntry(EpicFightMod.identifier("shield"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.shield.id())
            );

    public static final ExCapDataEntry TRIDENT = new ExCapDataEntry(EpicFightMod.identifier("trident"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.tridentMS.id())
            );
}