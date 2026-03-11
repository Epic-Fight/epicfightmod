package yesman.epicfight.api.ex_cap.modules.assets;

import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapDataEntry;
import yesman.epicfight.EpicFight;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ExCapDataSets
{
    public static final ExCapDataEntry SWORD = new ExCapDataEntry(EpicFight.identifier("sword"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id(), MainConditionals.DUAL_SWORDS.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.sword2HMS.id())
    );

    public static final ExCapDataEntry AXE = new ExCapDataEntry(EpicFight.identifier("axe"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            );

    public static final ExCapDataEntry PICKAXE = new ExCapDataEntry(EpicFight.identifier("pickaxe"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            );

    public static final ExCapDataEntry SHOVEL = new ExCapDataEntry(EpicFight.identifier("shovel"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            );

    public static final ExCapDataEntry DAGGER = new ExCapDataEntry(EpicFight.identifier("dagger"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id(), MainConditionals.DUAL_DAGGERS.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.dagger1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.dagger2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            );

    public static final ExCapDataEntry SPEAR = new ExCapDataEntry(EpicFight.identifier("spear"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id(), MainConditionals.SHIELD_OFFHAND.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.spear1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.spear2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSpearMS.id())
            );

    public static final ExCapDataEntry GREATSWORD = new ExCapDataEntry(EpicFight.identifier("greatsword"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.greatsword2HMS.id())
            );

    public static final ExCapDataEntry LONGSWORD = new ExCapDataEntry(EpicFight.identifier("longsword"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id(), MainConditionals.SHIELD_OFFHAND.id(), MainConditionals.LIECHTENAUER_CONDITION.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.longsword1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.longsword2HMS.id())
            .addMoveset(CapabilityItem.Styles.OCHS, Movesets.liechtenauerMS.id())
            );

    public static final ExCapDataEntry TACHI = new ExCapDataEntry(EpicFight.identifier("tachi"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.tachi2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            );

    public static final ExCapDataEntry UCHIGATANA = new ExCapDataEntry(EpicFight.identifier("uchigatana"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id(), MainConditionals.UCHIGATANA_SHEATHED.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.uchigatanaBase.id())
            .addMoveset(CapabilityItem.Styles.SHEATH, Movesets.uchigatanaSheathed.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            );

    public static final ExCapDataEntry HOE = new ExCapDataEntry(EpicFight.identifier("hoe"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS.id())
            );

    public static final ExCapDataEntry BOW = new ExCapDataEntry(EpicFight.identifier("bow"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.bow.id())
            );

    public static final ExCapDataEntry CROSSBOW = new ExCapDataEntry(EpicFight.identifier("crossbow"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_2H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.crossBow.id())
            );

    public static final ExCapDataEntry FIST = new ExCapDataEntry(EpicFight.identifier("fist"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.glove.id())
            );

    public static final ExCapDataEntry SHIELD = new ExCapDataEntry(EpicFight.identifier("shield"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.shield.id())
            );

    public static final ExCapDataEntry TRIDENT = new ExCapDataEntry(EpicFight.identifier("trident"), ExCapData.builder()
            .addConditional(MainConditionals.DEFAULT_1H_WIELD_STYLE.id())
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.tridentMS.id())
            );
}
