package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class RenderTwoHandedRangedWeapon extends RenderItemBase {
	public RenderTwoHandedRangedWeapon(JsonElement jsonElement) {
		super(jsonElement);
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, Matrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		Matrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, InteractionHand.OFF_HAND, poses);
		
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		itemInHandRenderer.renderItem(entitypatch.getOriginal(), stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, packedLight);
		poseStack.popPose();
	}
}