package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RenderTrident extends RenderItemBase {
	private static final OpenMatrix4f TRANSFORM_WHEN_AIMING = new OpenMatrix4f().rotateDeg(-80F, Vec3f.X_AXIS).translate(0.0F, 0.1F, 0.0F).unmodifiable();
	
	public RenderTrident(JsonElement jsonElement) {
		super(jsonElement);
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, hand, poses);
		
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		ItemDisplayContext transformType = (hand == InteractionHand.MAIN_HAND) ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
		Minecraft mc = Minecraft.getInstance();
		mc.gameRenderer.itemInHandRenderer.renderItem(entitypatch.getOriginal(), stack, transformType, !(hand == InteractionHand.MAIN_HAND), poseStack, buffer, packedLight);
		poseStack.popPose();
	}
	
	@Override
	public OpenMatrix4f getCorrectionMatrix(LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses) {
		if (entitypatch.getOriginal().getUseItemRemainingTicks() > 0) {
			Joint parentJoint = entitypatch.getParentJointOfHand(hand);
			this.transformHolder.load(TRANSFORM_WHEN_AIMING);
			this.transformHolder.mulFront(poses[parentJoint.getId()]);
			return this.transformHolder;
		}
		
		return super.getCorrectionMatrix(entitypatch, hand, poses);
	}
}