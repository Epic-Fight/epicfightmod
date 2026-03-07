package net.forixaim.ex_cap.modules.assets;

import net.forixaim.ex_cap.modules.core.ExCapData;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ExCapDataSets
{
    public static final ExCapData SWORD = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle, MainConditionals.dualSwords)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.sword2HMS)
            .build();

    public static final ExCapData AXE = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .build();

    public static final ExCapData PICKAXE = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .build();

    public static final ExCapData SHOVEL = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS)
            .build();

    public static final ExCapData DAGGER = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle, MainConditionals.dualDaggers)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.dagger1HMS)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.dagger2HMS)
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS)
            .build();

    public static final ExCapData SPEAR = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle, MainConditionals.shieldOffHand)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.spear1HMS)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.spear2HMS)
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSpearMS)
            .build();

    public static final ExCapData GREATSWORD = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.greatsword2HMS)
            .build();

    public static final ExCapData LONGSWORD = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle, MainConditionals.shieldOffHand, MainConditionals.liechtenauerCondition)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.longsword1HMS)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.longsword2HMS)
            .addMoveset(CapabilityItem.Styles.OCHS, Movesets.liechtenauerMS)
            .build();

    public static final ExCapData TACHI = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.tachi2HMS)
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS)
            .build();

    public static final ExCapData UCHIGATANA = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle, MainConditionals.uchigatanaSheathed)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.uchigatanaBase)
            .addMoveset(CapabilityItem.Styles.SHEATH, Movesets.uchigatanaSheathed)
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS)
            .build();

    public static final ExCapData HOE = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS)
            .build();

    public static final ExCapData BOW = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.glove)
            .build();

    public static final ExCapData CROSSBOW = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.crossBow)
            .build();

    public static final ExCapData FIST = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.glove)
            .build();

    public static final ExCapData SHIELD = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.shield)
            .build();

    public static final ExCapData TRIDENT = ExCapData.builder()
            .addConditional(MainConditionals.defaultRanged)
            .addMoveset(CapabilityItem.Styles.RANGED, Movesets.tridentMS)
            .build();
}
