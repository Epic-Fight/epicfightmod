package yesman.epicfight.mixin.client;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;

@Mixin(value = ThrownTridentRenderer.class)
public abstract class MixinThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
	protected MixinThrownTridentRenderer(Context p_174008_) {
		super(p_174008_);
	}
	
	@Inject(
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
            ordinal = 0
        ),
        method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
    )
	private void epicfight$render(ThrownTrident tridentEntity, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiSourceBuffer, int packedLight, CallbackInfo info) {
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(tridentEntity, ThrownTridentPatch.class);
		
		if (tridentPatch != null) {
			if (tridentPatch.isInnateActivated() && tridentEntity.getOwner() != null) {
				Entity owner = tridentEntity.getOwner();
				Vec3 toOwner = owner.position().subtract(tridentEntity.position());
				Vec3 toOwnerHorizontalNorm = owner.position().subtract(tridentEntity.position()).subtract(0, toOwner.y, 0).normalize();
				Vec3 toOwnerNorm = toOwner.normalize();
				Vec3 rotAxis = toOwnerHorizontalNorm.cross(toOwnerNorm).normalize();
				float deg = (float) MathUtils.getAngleBetween(toOwnerNorm, toOwnerHorizontalNorm);
				
				poseStack.mulPose(QuaternionUtils.rotationDegrees(new Vector3f((float)rotAxis.x, (float)rotAxis.y, (float)rotAxis.z), deg));
				poseStack.mulPose(QuaternionUtils.XP.rotationDegrees(90.0F));
				poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(Mth.lerp(partialTicks, tridentEntity.xRotO, tridentEntity.getXRot()) + 90.0F));
				poseStack.translate(0.0D, -0.8D, -0.0D);
				
				tridentPatch.renderXRot = tridentEntity.getXRot();
				tridentPatch.renderXRotO = tridentEntity.xRotO;
				tridentPatch.renderYRot = tridentEntity.getYRot();
				tridentPatch.renderYRotO = tridentEntity.yRotO;
				tridentEntity.xRotO = -90.0F;
				tridentEntity.yRotO = 90.0F;
				tridentEntity.setXRot(-90.0F);
				tridentEntity.setYRot(90.0F);
			}
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
	private void epicfight$renderPost(ThrownTrident tridentEntity, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiSourceBuffer, int packedLight, CallbackInfo info) {
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(tridentEntity, ThrownTridentPatch.class);
		
		if (tridentPatch != null) {
			if (tridentPatch.isInnateActivated()) {
				tridentEntity.xRotO = tridentPatch.renderXRotO;
				tridentEntity.yRotO = tridentPatch.renderYRotO;
				tridentEntity.setXRot(tridentPatch.renderXRot);
				tridentEntity.setYRot(tridentPatch.renderYRot);
			}
		}
	}
}