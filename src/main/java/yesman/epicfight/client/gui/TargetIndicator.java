package yesman.epicfight.client.gui;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.api.neoevent.playerpatch.TargetIndicatorCheckEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class TargetIndicator extends EntityUI {
	@Override
	public boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks) {
		if (!ClientConfig.showTargetIndicator) {
			return false;
		} else {
			if (playerpatch != null && entity != playerpatch.getTarget()) {
				return false;
			} else if (entity.isInvisibleTo(playerpatch.getOriginal()) || !entity.isAlive() || entity == playerpatch.getOriginal()) {
				return true;
			} else if (entity.distanceToSqr(Minecraft.getInstance().getCameraEntity()) >= 400) {
				return false;
			} else if (entity instanceof Player player) {
				return !player.isSpectator();
			}
		}
		
		return true;
	}
	
	@Override
	public void draw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource buffers, float partialTicks) {
		poseStack.pushPose();
		
		super.setupPoseStack(poseStack, entity, 0.0F, entity.getBbHeight() + 0.45F, 0.0F, true, partialTicks);
		
		if (entitypatch == null) {
			drawUIAsLevelModel(poseStack.last(), BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33, 256);
		} else {
			TargetIndicatorCheckEvent event = new TargetIndicatorCheckEvent(playerpatch, entitypatch);
			PlayerPatchEvent.postAndFireSkillListeners(event);
			
			switch (event.getIndicatorType()) {
			case NORMAL -> {
				drawUIAsLevelModel(poseStack.last(), BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33, 256);
			}
			case FLASH -> {
				if (entity.tickCount % 2 == 0) {
					drawUIAsLevelModel(poseStack.last(), BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 132, 0, 167, 36, 256);
				} else {
					drawUIAsLevelModel(poseStack.last(), BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33, 256);
				}
			}
			}
		}
		
		poseStack.popPose();
	}
}