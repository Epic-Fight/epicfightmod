package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.Factions;

public class EvokerPatch<T extends SpellcasterIllager> extends AbstractIllagerPatch<T> {
	public EvokerPatch(T original) {
		super(original, Factions.ILLAGER);
	}
	
	@Override
	protected void initAI() {
		
	}
	
	@Override
	public void initAnimator(Animator animator) {
		super.initAnimator(animator);
		
		animator.addLivingAnimation(LivingMotions.SPELLCAST, Animations.EVOKER_CAST_SPELL);
	}
	
	@Override
	public void updateMotion(boolean considerInaction) {
		if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.INACTION;
		} else {
			if (this.getOriginal().isCastingSpell()) {
				currentLivingMotion = LivingMotions.SPELLCAST;
			} else {
				super.commonMobUpdateMotion(considerInaction);
			}
		}
	}
	
	@Override
	public void setAIAsInfantry(boolean holdingRanedWeapon) {

	}
	
	@Override
	public void setAIAsMounted(Entity ridingEntity) {

	}
}