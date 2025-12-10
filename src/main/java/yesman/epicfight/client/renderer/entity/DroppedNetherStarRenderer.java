package yesman.epicfight.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import yesman.epicfight.client.renderer.LightningRenderHelper;

public class DroppedNetherStarRenderer extends ItemEntityRenderer {
	public DroppedNetherStarRenderer(Context context) {
		super(context);
	}
	
	@Override
	public void render(ItemEntity entityIn, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
		super.render(entityIn, yRot, partialTicks, poseStack, multiBufferSource, packedLight);
		
		poseStack.pushPose();
		poseStack.translate(0.0D, entityIn.getBbHeight() + Mth.sin(((float) entityIn.getAge() + partialTicks) / 10.0F + entityIn.bobOffs) * 0.1F + 0.1F, 0.0D);
		VertexConsumer vertexBuilder = multiBufferSource.getBuffer(RenderType.lightning());
		float progression = (entityIn.tickCount + partialTicks) * 0.01F;
		float repeater = ((float)Math.sin(progression * 5.0F) + 1.0F) * 0.5F;
		LightningRenderHelper.renderCyclingLight(vertexBuilder, poseStack, 32, 0, 255, 9, 0.05F, progression, repeater);
		poseStack.popPose();
	}
}