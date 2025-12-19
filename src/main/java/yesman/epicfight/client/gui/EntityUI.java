package yesman.epicfight.client.gui;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import org.joml.Matrix4f;

public abstract class EntityUI {
	public static final List<EntityUI> ENTITY_UI_LIST = Lists.newArrayList();
	public static final TargetIndicator TARGET_INDICATOR = new TargetIndicator();
	public static final HealthBar HEALTH_BAR  = new HealthBar();
    public static final ResourceLocation BATTLE_ICON = EpicFightMod.identifier("textures/gui/battle_icons.png");
	
	public EntityUI() {
		ENTITY_UI_LIST.add(this);
	}
	
	public static void drawUIAsLevelModel(Matrix4f matrix, ResourceLocation textureLocation, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int minU, int minV, int maxU, int maxV, int uvSize) {
		float uvSizeInvert = 1.0F / uvSize;
		
		drawUIAsLevelModel(matrix, textureLocation, buffer, minX, minY, maxX, maxY, minU * uvSizeInvert, minV * uvSizeInvert, maxU * uvSizeInvert, maxV * uvSizeInvert);
	}
	
	public static void drawUIAsLevelModel(Matrix4f matrix, ResourceLocation textureLocation, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, float minU, float minV, float maxU, float maxV) {
		VertexConsumer vertexConsumer = buffer.getBuffer(EpicFightRenderTypes.entityUITexture(textureLocation));
		
		vertexConsumer.vertex(matrix, minX, minY, 0).uv(minU, maxV).endVertex();
        vertexConsumer.vertex(matrix, maxX, minY, 0).uv(maxU, maxV).endVertex();
        vertexConsumer.vertex(matrix, maxX, maxY, 0).uv(maxU, minV).endVertex();
        vertexConsumer.vertex(matrix, minX, maxY, 0).uv(minU, minV).endVertex();
	}
	
	public static void drawColoredQuadAsLevelModel(Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int packedColor) {
		VertexConsumer vertexConsumer = buffer.getBuffer(EpicFightRenderTypes.entityUIColor());
		
		vertexConsumer.vertex(matrix, minX, minY, 0).color(packedColor).endVertex();
        vertexConsumer.vertex(matrix, maxX, minY, 0).color(packedColor).endVertex();
        vertexConsumer.vertex(matrix, maxX, maxY, 0).color(packedColor).endVertex();
        vertexConsumer.vertex(matrix, minX, maxY, 0).color(packedColor).endVertex();
	}
	
	public static void drawColoredQuadAsLevelModel(Matrix4f matrix, MultiBufferSource buffer, float minX, float minY, float maxX, float maxY, int r, int g, int b, int a) {
		VertexConsumer vertexConsumer = buffer.getBuffer(EpicFightRenderTypes.entityUIColor());
		
		vertexConsumer.vertex(matrix, minX, minY, 0).color(r, g, b, a).endVertex();
        vertexConsumer.vertex(matrix, maxX, minY, 0).color(r, g, b, a).endVertex();
        vertexConsumer.vertex(matrix, maxX, maxY, 0).color(r, g, b, a).endVertex();
        vertexConsumer.vertex(matrix, minX, maxY, 0).color(r, g, b, a).endVertex();
	}
	
	public final Matrix4f getModelViewMatrixAlignedToCamera(PoseStack poseStack, LivingEntity entity, float x, float y, float z, boolean lockRotation, float partialTicks) {
		float posX = (float)Mth.lerp(partialTicks, entity.xOld, entity.getX());
		float posY = (float)Mth.lerp(partialTicks, entity.yOld, entity.getY());
		float posZ = (float)Mth.lerp(partialTicks, entity.zOld, entity.getZ());
		
		poseStack.pushPose();
		poseStack.translate(-posX, -posY, -posZ);
		poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(180.0F));

		float screenX = posX + x;
		float screenY = posY + y;
		float screenZ = posZ + z;

		OpenMatrix4f viewMatrix = OpenMatrix4f.importFromMojangMatrix(poseStack.last().pose());
		OpenMatrix4f finalMatrix = new OpenMatrix4f();
		finalMatrix.translate(new Vec3f(-screenX, screenY, -screenZ));
		poseStack.popPose();
		
		if (lockRotation) {
			finalMatrix.m00 = viewMatrix.m00;
			finalMatrix.m01 = viewMatrix.m10;
			finalMatrix.m02 = viewMatrix.m20;
			finalMatrix.m10 = viewMatrix.m01;
			finalMatrix.m11 = viewMatrix.m11;
			finalMatrix.m12 = viewMatrix.m21;
			finalMatrix.m20 = viewMatrix.m02;
			finalMatrix.m21 = viewMatrix.m12;
			finalMatrix.m22 = viewMatrix.m22;
		}
		
		finalMatrix.mulFront(viewMatrix);
		
		return OpenMatrix4f.exportToMojangMatrix(finalMatrix);
	}
	
	public abstract boolean shouldDraw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, float partialTicks);
	
	public abstract void draw(LivingEntity entity, @Nullable LivingEntityPatch<?> entitypatch, LocalPlayerPatch playerpatch, PoseStack poseStack, MultiBufferSource multiBufferSource, float partialTicks);
}