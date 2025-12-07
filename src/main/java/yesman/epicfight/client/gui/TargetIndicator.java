package yesman.epicfight.client.gui;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
				return false;
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
		Matrix4f modelViewMatrix = super.getModelViewMatrixAlignedToCamera(poseStack, entity, 0.0F, entity.getBbHeight() + 0.45F, 0.0F, true, partialTicks);
		
		if (entitypatch == null) {
			drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33, 256);
		} else {
			if (entity.tickCount % 2 == 0 && !entitypatch.flashTargetIndicator(playerpatch)) {
				drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 132, 0, 167, 36, 256);
			} else {
				drawUIAsLevelModel(modelViewMatrix, BATTLE_ICON, buffers, -0.1F, -0.1F, 0.1F, 0.1F, 97, 2, 128, 33, 256);
			}
		}
	}
}