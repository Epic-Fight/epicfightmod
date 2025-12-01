package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class PatchedElytraLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>> extends PatchedLayer<E, T, M, ElytraLayer<E, M>> {
	@Override
	protected void renderLayer(T entitypatch, E livingentity, ElytraLayer<E, M> vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, Matrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		if (vanillaLayer.shouldRender(livingentity.getItemBySlot(EquipmentSlot.CHEST), livingentity)) {
			vanillaLayer.getParentModel().copyPropertiesTo(vanillaLayer.elytraModel);
			Matrix4f modelMatrix = new Matrix4f();
			modelMatrix.scale(-0.9F, -0.9F, 0.9F).translate(0.0F, -0.5F, -0.1F).mulLocal(poses[8]);
			poseStack.pushPose();
			MathUtils.mulStack(poseStack, modelMatrix);
			vanillaLayer.render(poseStack, buffer, packedLight, livingentity, livingentity.walkAnimation.position(), livingentity.walkAnimation.speed(), partialTicks, bob, yRot, xRot);
			poseStack.popPose();
		}
	}
}