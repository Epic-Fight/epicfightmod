package net.forixaim.ex_cap.modules.assets;

import net.forixaim.ex_cap.modules.core.ExCapData;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ExCapDataSets
{
    public static final ExCapData SWORD = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle, MainConditionals.dualSwords)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.sword2HMS.id())
            .build();

    public static final ExCapData AXE = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            .build();

    public static final ExCapData PICKAXE = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            .build();

    public static final ExCapData SHOVEL = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS.id())
            .build();

    public static final ExCapData DAGGER = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle, MainConditionals.dualDaggers)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.dagger1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.dagger2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            .build();

    public static final ExCapData SPEAR = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle, MainConditionals.shieldOffHand)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.spear1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.spear2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSpearMS.id())
            .build();

    public static final ExCapData GREATSWORD = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.greatsword2HMS.id())
            .build();

    public static final ExCapData LONGSWORD = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle, MainConditionals.shieldOffHand, MainConditionals.liechtenauerCondition)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.longsword1HMS.id())
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.longsword2HMS.id())
            .addMoveset(CapabilityItem.Styles.OCHS, Movesets.liechtenauerMS.id())
            .build();

    public static final ExCapData TACHI = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.tachi2HMS.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            .build();

    public static final ExCapData UCHIGATANA = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle, MainConditionals.uchigatanaSheathed)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.uchigatanaBase.id())
            .addMoveset(CapabilityItem.Styles.SHEATH, Movesets.uchigatanaSheathed.id())
            .addMoveset(CapabilityItem.Styles.MOUNT, Movesets.mountedSwordMS.id())
            .build();

    public static final ExCapData HOE = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.sword1HMS.id())
            .build();

    public static final ExCapData BOW = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.bow.id())
            .build();

    public static final ExCapData CROSSBOW = ExCapData.builder()
            .addConditional(MainConditionals.default2HWieldStyle)
            .addMoveset(CapabilityItem.Styles.TWO_HAND, Movesets.crossBow.id())
            .build();

    public static final ExCapData FIST = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.glove.id())
            .build();

    public static final ExCapData SHIELD = ExCapData.builder()
            .addConditional(MainConditionals.default1HWieldStyle)
            .addMoveset(CapabilityItem.Styles.ONE_HAND, Movesets.shield.id())
            .build();

    public static final ExCapData TRIDENT = ExCapData.builder()
            .addConditional(MainConditionals.defaultRanged)
            .addMoveset(CapabilityItem.Styles.RANGED, Movesets.tridentMS.id())
            .build();
}
