package net.forixaim.ex_cap.modules.assets;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkills;
import net.forixaim.ex_cap.modules.core.MoveSet;

@SuppressWarnings("unchecked")
public class Movesets 
{
    public static MoveSet.MoveSetBuilder mountedSwordMS = MoveSet.builder()
            .addComboAttacks(Animations.SWORD_MOUNT_ATTACK);

    public static MoveSet.MoveSetBuilder greatsword2HMS = MoveSet.builder()
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_GREATSWORD,
                    LivingMotions.IDLE, LivingMotions.JUMP, LivingMotions.KNEEL, LivingMotions.SNEAK,
                    LivingMotions.SWIM, LivingMotions.FLY, LivingMotions.CREATIVE_FLY, LivingMotions.CREATIVE_IDLE)
            .addLivingMotionsRecursive(Animations.BIPED_WALK_GREATSWORD,
                    LivingMotions.WALK,
                    LivingMotions.CHASE)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.GREATSWORD_GUARD)
            .addComboAttacks(
                    Animations.GREATSWORD_AUTO1,
                    Animations.GREATSWORD_AUTO2,
                    Animations.GREATSWORD_DASH,
                    Animations.GREATSWORD_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.STEEL_WHIRLWIND.get());

    public static MoveSet.MoveSetBuilder axeOneHandMS = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.SWORD_GUARD)
            .addComboAttacks(
                    Animations.AXE_AUTO1,
                    Animations.AXE_AUTO2,
                    Animations.AXE_DASH,
                    Animations.AXE_AIRSLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.THE_GUILLOTINE.get());

    public static MoveSet.MoveSetBuilder rangedMoveSet = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT)
            .addLivingMotionModifier(LivingMotions.AIM, Animations.BIPED_BOW_AIM)
            .addLivingMotionModifier(LivingMotions.WALK, Animations.BIPED_WALK)
            .addLivingMotionModifier(LivingMotions.IDLE, Animations.BIPED_IDLE)
            .addComboAttacks(Animations.FIST_AUTO1, Animations.FIST_AUTO2, Animations.FIST_AUTO3, Animations.FIST_DASH, Animations.FIST_AIR_SLASH)
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.RELENTLESS_COMBO.get());

    public static MoveSet.MoveSetBuilder mountedSpearMS = MoveSet.builder()
            .addComboAttacks(
                    Animations.SPEAR_MOUNT_ATTACK
            );

    public static MoveSet.MoveSetBuilder longsword2HMS = MoveSet.builder()
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_LONGSWORD,
                    LivingMotions.IDLE, LivingMotions.SNEAK, LivingMotions.KNEEL,
                    LivingMotions.JUMP, LivingMotions.SWIM)
            .addLivingMotionsRecursive(Animations.BIPED_WALK_LONGSWORD,
                    LivingMotions.WALK, LivingMotions.CHASE)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
            .addComboAttacks(
                    Animations.LONGSWORD_AUTO1,
                    Animations.LONGSWORD_AUTO2,
                    Animations.LONGSWORD_AUTO3,
                    Animations.LONGSWORD_DASH,
                    Animations.LONGSWORD_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.LIECHTENAUER.get())
            .shouldRenderSheath(livingEntityPatch -> true);

    public static MoveSet.MoveSetBuilder longsword1HMS = MoveSet.builder()
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_LONGSWORD,
                    LivingMotions.IDLE, LivingMotions.SNEAK, LivingMotions.KNEEL,
                    LivingMotions.JUMP, LivingMotions.SWIM)
            .addLivingMotionsRecursive(Animations.BIPED_WALK_LONGSWORD,
                    LivingMotions.WALK, LivingMotions.CHASE)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
            .addComboAttacks(
                    Animations.LONGSWORD_AUTO1,
                    Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3,
                    Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.SHARP_STAB.get());

    public static MoveSet.MoveSetBuilder liechtenauerMS = MoveSet.builder()
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_LIECHTENAUER,
                    LivingMotions.IDLE, LivingMotions.SNEAK, LivingMotions.KNEEL,
                    LivingMotions.JUMP, LivingMotions.SWIM)
            .addLivingMotionsRecursive( Animations.BIPED_WALK_LIECHTENAUER,
                    LivingMotions.WALK, LivingMotions.CHASE)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_HOLD_LIECHTENAUER)
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
            .addComboAttacks(
                    Animations.LONGSWORD_LIECHTENAUER_AUTO1,
                    Animations.LONGSWORD_LIECHTENAUER_AUTO2,
                    Animations.LONGSWORD_LIECHTENAUER_AUTO3,
                    Animations.LONGSWORD_DASH,
                    Animations.LONGSWORD_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.SHARP_STAB.get());

    public static MoveSet.MoveSetBuilder dagger1HMS = MoveSet.builder()
            .addComboAttacks(
                    Animations.DAGGER_AUTO1,
                    Animations.DAGGER_AUTO2,
                    Animations.DAGGER_AUTO3,
                    Animations.DAGGER_DASH,
                    Animations.DAGGER_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.EVISCERATE.get());

    public static MoveSet.MoveSetBuilder dagger2HMS = MoveSet.builder()
            .addComboAttacks(
                    Animations.DAGGER_DUAL_AUTO1,
                    Animations.DAGGER_DUAL_AUTO2,
                    Animations.DAGGER_DUAL_AUTO3,
                    Animations.DAGGER_DUAL_AUTO4,
                    Animations.DAGGER_DUAL_DASH,
                    Animations.DAGGER_DUAL_AIR_SLASH
            )
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_DUAL_WEAPON,
                    LivingMotions.IDLE, LivingMotions.KNEEL, LivingMotions.WALK, LivingMotions.CHASE,
                    LivingMotions.SNEAK, LivingMotions.SWIM, LivingMotions.FLOAT, LivingMotions.FALL)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_DUAL)
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.BLADE_RUSH.get());

    public static MoveSet.MoveSetBuilder spear2HMS = MoveSet.builder()
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_SPEAR,
                    LivingMotions.IDLE, LivingMotions.SWIM)
            .addLivingMotionsRecursive(Animations.BIPED_WALK_SPEAR,
                    LivingMotions.WALK, LivingMotions.CHASE)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.SPEAR_GUARD)
            .addComboAttacks(
                    Animations.SPEAR_TWOHAND_AUTO1, Animations.SPEAR_TWOHAND_AUTO2,
                    Animations.SPEAR_DASH,
                    Animations.SPEAR_TWOHAND_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.GRASPING_SPIRE.get());

    public static MoveSet.MoveSetBuilder spear1HMS = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
            .addComboAttacks(
                    Animations.SPEAR_ONEHAND_AUTO,
                    Animations.SPEAR_DASH,
                    Animations.SPEAR_ONEHAND_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.HEARTPIERCER.get());

    public static MoveSet.MoveSetBuilder sword1HMS = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.SWORD_GUARD)
            .addComboAttacks(
                    Animations.SWORD_AUTO1,
                    Animations.SWORD_AUTO2, Animations.SWORD_AUTO3,
                    Animations.SWORD_DASH, Animations.SWORD_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.SWEEPING_EDGE.get());

    public static MoveSet.MoveSetBuilder sword2HMS = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.SWORD_DUAL_GUARD)
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_DUAL_WEAPON,
                    LivingMotions.IDLE, LivingMotions.KNEEL, LivingMotions.WALK, LivingMotions.CHASE,
                    LivingMotions.SNEAK, LivingMotions.SWIM, LivingMotions.FLOAT, LivingMotions.FALL)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_DUAL)
            .addComboAttacks(
                    Animations.SWORD_DUAL_AUTO1,
                    Animations.SWORD_DUAL_AUTO2, Animations.SWORD_DUAL_AUTO3,
                    Animations.SWORD_DUAL_DASH, Animations.SWORD_DUAL_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.DANCING_EDGE.get());

    public static MoveSet.MoveSetBuilder shield = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.BIPED_BLOCK);

    public static MoveSet.MoveSetBuilder tachi2HMS = MoveSet.builder()
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_TACHI,
                    LivingMotions.IDLE,
                    LivingMotions.KNEEL, LivingMotions.WALK, LivingMotions.CHASE, LivingMotions.RUN,
                    LivingMotions.SNEAK, LivingMotions.SWIM, LivingMotions.FLOAT, LivingMotions.FALL)
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
            .addComboAttacks(
                    Animations.TACHI_AUTO1,
                    Animations.TACHI_AUTO2,
                    Animations.TACHI_AUTO3,
                    Animations.TACHI_DASH,
                    Animations.LONGSWORD_AIR_SLASH
            )
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.RUSHING_TEMPO.get());

    public static MoveSet.MoveSetBuilder uchigatanaBase = MoveSet.builder()
            .addComboAttacks(Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2,
                    Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH,
                    Animations.UCHIGATANA_AIR_SLASH)
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_UCHIGATANA,
                    LivingMotions.IDLE, LivingMotions.KNEEL,
                    LivingMotions.SWIM, LivingMotions.FALL,
                    LivingMotions.FALL)
            .addLivingMotionsRecursive(Animations.BIPED_WALK_UCHIGATANA,
                    LivingMotions.CHASE, LivingMotions.WALK, LivingMotions.SNEAK)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA)
            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.UCHIGATANA_GUARD)
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.BATTOJUTSU.get())
            .setPassiveSkill(EpicFightSkills.BATTOJUTSU_PASSIVE.get())
            .shouldRenderSheath(livingEntityPatch -> true);

    public static MoveSet.MoveSetBuilder uchigatanaSheathed = MoveSet.builder()
            .addComboAttacks(Animations.UCHIGATANA_SHEATHING_AUTO,
                    Animations.UCHIGATANA_SHEATHING_DASH,
                    Animations.UCHIGATANA_SHEATH_AIR_SLASH)
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_UCHIGATANA_SHEATHING,
                    LivingMotions.IDLE, LivingMotions.KNEEL,
                    LivingMotions.SWIM, LivingMotions.FALL,
                    LivingMotions.FALL, LivingMotions.SNEAK, LivingMotions.CHASE)
            .addLivingMotionModifier(LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA_SHEATHING)
            .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA_SHEATHING)
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.BATTOJUTSU.get())
            .setPassiveSkill(EpicFightSkills.BATTOJUTSU_PASSIVE.get())
            .shouldRenderSheath(livingEntityPatch -> true);

    public static MoveSet.MoveSetBuilder glove = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.IDLE, Animations.BIPED_IDLE)
            .addLivingMotionModifier(LivingMotions.WALK, Animations.BIPED_WALK)
            .addComboAttacks(Animations.FIST_AUTO1, Animations.FIST_AUTO2, Animations.FIST_AUTO3, Animations.FIST_DASH, Animations.FIST_AIR_SLASH)
            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.RELENTLESS_COMBO.get());

    public static MoveSet.MoveSetBuilder bow = MoveSet.builder()
            .addLivingMotionModifier(LivingMotions.AIM, Animations.BIPED_BOW_AIM)
            .addLivingMotionModifier(LivingMotions.IDLE, Animations.BIPED_IDLE)
            .addLivingMotionModifier(LivingMotions.WALK, Animations.BIPED_WALK)
            .addLivingMotionModifier(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT)
            .setMotionPredicate((entityPatch, interactionHand) -> entityPatch.getOriginal().isUsingItem() && entityPatch.getOriginal().getUseItem().getUseAnimation() == UseAnim.BOW ? LivingMotions.AIM : null)
            .addComboAttacks(Animations.FIST_AUTO1, Animations.FIST_AUTO2, Animations.FIST_AUTO3, Animations.FIST_DASH, Animations.FIST_AIR_SLASH);

    public static MoveSet.MoveSetBuilder crossBow = MoveSet.builder()
            .addLivingMotionsRecursive(Animations.BIPED_HOLD_CROSSBOW,
                    LivingMotions.IDLE, LivingMotions.KNEEL, LivingMotions.WALK, LivingMotions.RUN,
                    LivingMotions.SWIM, LivingMotions.FALL, LivingMotions.FLOAT, LivingMotions.SNEAK)
            .setMotionPredicate((entityPatch, interactionHand) -> entityPatch.getEntityState().canUseItem() &&
                    entityPatch.getOriginal().getMainHandItem().getItem() instanceof ProjectileWeaponItem &&
                    CrossbowItem.isCharged(entityPatch.getOriginal().getMainHandItem())
                    ? LivingMotions.AIM : null)
            .addLivingMotionModifier(LivingMotions.AIM, Animations.BIPED_CROSSBOW_AIM)
            .addLivingMotionModifier(LivingMotions.RELOAD, Animations.BIPED_CROSSBOW_RELOAD)
            .addLivingMotionModifier(LivingMotions.SHOT, Animations.BIPED_CROSSBOW_SHOT);

    public static MoveSet.MoveSetBuilder tridentMS = MoveSet.builder()
            .addComboAttacks(
                    Animations.TRIDENT_AUTO1,
                    Animations.TRIDENT_AUTO2,
                    Animations.TRIDENT_AUTO3,
                    Animations.SPEAR_DASH,
                    Animations.SPEAR_ONEHAND_AIR_SLASH
            )
            .setMotionPredicate((entityPatch, interactionHand) -> entityPatch.getOriginal().isUsingItem() && entityPatch.getOriginal().getUseItem().getUseAnimation() == UseAnim.SPEAR ? LivingMotions.AIM : null)
            .addLivingMotionModifier(LivingMotions.AIM, Animations.BIPED_JAVELIN_AIM)
            .addLivingMotionModifier(LivingMotions.SHOT, Animations.BIPED_JAVELIN_THROW)
            .addMountAttacks(Animations.SPEAR_MOUNT_ATTACK)
            .addInnateSkill( (itemStack, playerPatch) ->
            {
                if (itemStack.getEnchantmentLevel(playerPatch.getLevel().holderOrThrow(Enchantments.RIPTIDE)) > 0) {
                    return EpicFightSkills.TSUNAMI.get();
                } else if (itemStack.getEnchantmentLevel(playerPatch.getLevel().holderOrThrow(Enchantments.CHANNELING)) > 0) {
                    return EpicFightSkills.WRATHFUL_LIGHTING.get();
                } else if (itemStack.getEnchantmentLevel(playerPatch.getLevel().holderOrThrow(Enchantments.LOYALTY)) > 0) {
                    return EpicFightSkills.EVERLASTING_ALLEGIANCE.get();
                } else {
                    return EpicFightSkills.GRASPING_SPIRE.get();
                }
            });
}
