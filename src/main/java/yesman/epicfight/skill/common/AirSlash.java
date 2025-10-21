package yesman.epicfight.skill.common;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class AirSlash extends Skill {
	public static SkillBuilder<?> createAirSlashBuilder() {
		return new SkillBuilder<> (AirSlash::new).setCategory(SkillCategories.AIR_ATTACK).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.STAMINA);
	}
	
	public AirSlash(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@Override
	public boolean canExecute(SkillContainer container) {
		return (!container.getExecutor().getOriginal().onGround() && !container.getExecutor().getOriginal().isInWater() && container.getExecutor().getOriginal().getDeltaMovement().y > 0.03D);
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executer) {
		EntityState playerState = executer.getEntityState();
		Player player = executer.getOriginal();
		
		return !(player.isPassenger() || player.isSpectator() || executer.isInAir() || !playerState.canBasicAttack());
	}
	
	@Override
	public void executeOnServer(SkillContainer skillContainer, CompoundTag args) {
		List<AnimationAccessor<? extends AttackAnimation>> motions = skillContainer.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND).getAutoAttackMotion(skillContainer.getExecutor());
		
		if (motions == null) {
			return;
		}
		
		AnimationAccessor<? extends AttackAnimation> attackMotion = motions.get(motions.size() - 1);
		
		if (attackMotion != null) {
			super.executeOnServer(skillContainer, args);
			skillContainer.getExecutor().playAnimationSynchronized(attackMotion, 0);
		}
	}
}