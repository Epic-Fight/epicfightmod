package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class RenderTrident extends RenderItemBase {
	private static final Matrix4f TRANSFORM_WHEN_AIMING = new Matrix4f().rotate(org.joml.Math.toRadians(-80F), 1, 0, 0).translate(0.0F, 0.1F, 0.0F);
	
	public RenderTrident(JsonElement jsonElement) {
		super(jsonElement);
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, Matrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		Matrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, hand, poses);
		
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		ItemDisplayContext transformType = (hand == InteractionHand.MAIN_HAND) ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
		Minecraft mc = Minecraft.getInstance();
		mc.gameRenderer.itemInHandRenderer.renderItem(entitypatch.getOriginal(), stack, transformType, !(hand == InteractionHand.MAIN_HAND), poseStack, buffer, packedLight);
		poseStack.popPose();
	}
	
	@Override
	public Matrix4f getCorrectionMatrix(LivingEntityPatch<?> entitypatch, InteractionHand hand, Matrix4f[] poses) {
		if (entitypatch.getOriginal().getUseItemRemainingTicks() > 0) {
			Joint parentJoint = entitypatch.getParentJointOfHand(hand);
			this.transformHolder.set(TRANSFORM_WHEN_AIMING);
			this.transformHolder.mulLocal(poses[parentJoint.getId()]);
			return this.transformHolder;
		}
		
		return super.getCorrectionMatrix(entitypatch, hand, poses);
	}
}