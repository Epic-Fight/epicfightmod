package yesman.epicfight.registry.entries;

import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.ex_cap.data.Moveset;
import yesman.epicfight.api.ex_cap.data.modifier.WeaponModifier;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.deferred.ModifierRegister;
import yesman.epicfight.registry.deferred.holders.DeferredModifier;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public final class EpicFightModifiers {
    private EpicFightModifiers() {}

    public static final ModifierRegister REGISTRY = ModifierRegister.create(EpicFight.MODID);

    public static final DeferredModifier BOKKEN = REGISTRY.registerModifier("bokken", () -> WeaponModifier.builder()
            .target(EpicFightItemCapabilityPresets.BOKKEN)
            .addConditionalModifier(ProviderConditional.createSkillCondition(CapabilityItem.Styles.OCHS, EpicFightSkills.BERSERKER, SkillSlots.PASSIVE1, false, false))
            .addMovesetModifier(CapabilityItem.Styles.OCHS, Moveset.builder()
                    .addLivingMotionsRecursive(Animations.BIPED_HOLD_GREATSWORD,
                            LivingMotions.IDLE, LivingMotions.JUMP, LivingMotions.KNEEL, LivingMotions.SNEAK,
                            LivingMotions.SWIM, LivingMotions.FLY, LivingMotions.CREATIVE_FLY, LivingMotions.CREATIVE_IDLE)
                    .addLivingMotionsRecursive(Animations.BIPED_WALK_GREATSWORD,
                            LivingMotions.WALK, LivingMotions.CHASE)
                    .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
                    .addLivingMotionModifier(LivingMotions.BLOCK, Animations.GREATSWORD_GUARD)
                    .addComboAttacks(
                            Animations.GREATSWORD_AUTO1, Animations.GREATSWORD_AUTO2,
                            Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH
                    )
                    .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.STEEL_WHIRLWIND.get())));
}
