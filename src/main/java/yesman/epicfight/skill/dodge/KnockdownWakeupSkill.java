package yesman.epicfight.skill.dodge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class KnockdownWakeupSkill extends DodgeSkill {
	public KnockdownWakeupSkill(DodgeSkill.Builder<?> builder) {
		super(builder);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void gatherArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
		LocalPlayerPatch executor = container.getClientExecutor();
		Input input = executor.getOriginal().input;
		float sneakingSpeed = (float)executor.getOriginal().getAttributeValue(Attributes.SNEAKING_SPEED);
		input.tick(false, sneakingSpeed);
		
        int left = input.left ? 1 : 0;
        int right = input.right ? -1 : 0;
		int horizon = left + right;
		float yRot = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
		
		arguments.putInt("direction", horizon >= 0 ? 0 : 1);
		arguments.putFloat("yRot", yRot);
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		EntityState playerState = executor.getEntityState();
		float elapsedTime = executor.getAnimator().getPlayerFor(null).getElapsedTime();
		return !(executor.isInAir() || (playerState.hurt() && !playerState.knockDown())) && !executor.getOriginal().isInWater() && !executor.getOriginal().onClimbable() && elapsedTime > 0.7F;
	}
}