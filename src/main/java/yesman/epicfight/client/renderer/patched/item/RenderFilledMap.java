package yesman.epicfight.client.renderer.patched.item;

import org.joml.Matrix4f;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RenderFilledMap extends RenderItemBase {
	private static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.parse("textures/map/map_background.png"));
	
	public RenderFilledMap(JsonElement jsonElement) {
		super(jsonElement);
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, hand, poses);
		
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		
		if (hand == InteractionHand.MAIN_HAND && entitypatch.getOriginal().getOffhandItem().isEmpty()) {
			poseStack.scale(2.0F, 2.0F, 2.0F);
		}
		
		itemInHandRenderer.renderMap(poseStack, buffer, packedLight, stack);
		VertexConsumer vertexconsumer = buffer.getBuffer(MAP_BACKGROUND);
	    Matrix4f matrix4f = poseStack.last().pose();
		
		vertexconsumer.addVertex(matrix4f, -7.0F, -7.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setLight(packedLight);
		vertexconsumer.addVertex(matrix4f, 135.0F, -7.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setLight(packedLight);
		vertexconsumer.addVertex(matrix4f, 135.0F, 135.0F, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setLight(packedLight);
		vertexconsumer.addVertex(matrix4f, -7.0F, 135.0F, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setLight(packedLight);

		poseStack.popPose();
    }
}