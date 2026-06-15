package yesman.epicfight.compat.mca.entity_patch;

import net.conczin.mca.entity.VillagerEntityMCA;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.ZombiePatch;

public class MCAVillagerEntityPatch extends HumanoidMobPatch<VillagerEntityMCA> {
    public MCAVillagerEntityPatch(VillagerEntityMCA original) {
        super(original, Factions.VILLAGER);
    }

    @Override
    public void onConstructed(VillagerEntityMCA original) {
        super.onConstructed(original);
    }

    @Override
    protected void initAnimator(Animator animator) {
        super.initAnimator(animator);
        animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
        animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_WALK);
        animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
        animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
        animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
    }

    @Override
    public void updateMotion(boolean considerInaction) {
        this.commonAggressiveMobUpdateMotion(considerInaction);
    }
}
