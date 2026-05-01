package yesman.epicfight.skill.dodge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class KnockdownWakeupSkill extends DodgeSkill {
	public KnockdownWakeupSkill(DodgeSkill.Builder<?> builder) {
		super(builder);
	}
	
	@Override @ClientOnly
	public void gatherArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
		LocalPlayerPatch executor = container.getClientExecutor();
		LocalPlayer localPlayer = executor.getOriginal();
		float pulse = (float)executor.getOriginal().getAttributeValue(Attributes.SNEAKING_SPEED);
		InputUtils.sneakingTick(localPlayer, false, pulse);
		
		final MovementDirection movementDirection = MovementDirection.fromInputState(InputManager.getInputState(localPlayer.input));
        final int horizon = movementDirection.horizontal();
        final float yRot = Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
		
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