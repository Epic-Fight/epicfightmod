package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedBeeStingerLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends PlayerModel<E>> extends PatchedStuckInBodyLayer<E, T, M, BeeStingerLayer<E, M>> {
	private static final ResourceLocation BEE_STINGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_stinger.png");
	
	protected void renderStuckItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float pf1, float pf2, float pf3, float partialTick) {
		float f = Mth.sqrt(pf1 * pf1 + pf3 * pf3);
		float f1 = (float) (Math.atan2((double) pf1, (double) pf3) * (double) (180F / (float) Math.PI));
		float f2 = (float) (Math.atan2((double) pf2, (double) f) * (double) (180F / (float) Math.PI));
		poseStack.translate(0.0F, 0.0F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(f1 - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(f2));
		poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
		poseStack.scale(0.03125F, 0.03125F, 0.03125F);
		poseStack.translate(2.5F, 0.0F, 0.0F);
		VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));
		
		for (int i = 0; i < 4; ++i) {
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			PoseStack.Pose posestack$pose = poseStack.last();
			vertex(vertexconsumer, posestack$pose, -4.5F, -1, 0.0F, 0.0F, packedLight);
			vertex(vertexconsumer, posestack$pose, 4.5F, -1, 0.125F, 0.0F, packedLight);
			vertex(vertexconsumer, posestack$pose, 4.5F, 1, 0.125F, 0.0625F, packedLight);
			vertex(vertexconsumer, posestack$pose, -4.5F, 1, 0.0F, 0.0625F, packedLight);
		}
	}
	
	private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float x, int y, float z, float u, int v) {
		vertexConsumer.addVertex(pose, x, (float) y, 0.0F).setColor(255, 255, 255, 255).setUv(z, u).setOverlay(OverlayTexture.NO_OVERLAY).setLight(v).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}
	
	@Override
	protected int numStuck(E entity) {
		return entity.getStingerCount();
	}
}
