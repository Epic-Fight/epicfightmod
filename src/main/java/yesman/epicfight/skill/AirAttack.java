package yesman.epicfight.skill;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class AirAttack extends Skill {
	public static SkillBuilder<AirAttack> createAirAttackBuilder() {
		return new SkillBuilder<AirAttack>().setCategory(SkillCategories.AIR_ATTACK).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.STAMINA);
	}
	
	public AirAttack(SkillBuilder<? extends AirAttack> builder) {
		super(builder);
	}
	
	@Override
	public boolean canExecute(SkillContainer container) {
		return !container.getExecutor().getOriginal().onGround() && !container.getExecutor().getOriginal().isInWater();
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		EntityState playerState = executor.getEntityState();
		Player player = executor.getOriginal();
		
		return !(player.isPassenger() || player.isSpectator() || executor.isInAir() || !playerState.canBasicAttack());
	}
	
	@Override
	public void executeOnServer(SkillContainer skillContainer, FriendlyByteBuf args) {
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