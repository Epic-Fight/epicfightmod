package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.mixin.client.MixinEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class PatchedEntityRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, R extends EntityRenderer<E>, AM extends SkinnedMesh> {
	public void render(E entity, T entitypatch, R renderer, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		RenderNameTagEvent renderNameplateEvent = new RenderNameTagEvent(entity, entity.getDisplayName(), renderer, poseStack, buffer, packedLight, partialTicks);
		MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
		
		MixinEntityRenderer entityRendererAccessor = (MixinEntityRenderer)renderer;
		
		if ((entityRendererAccessor.invokeShouldShowName(entity) || renderNameplateEvent.getResult() == Result.ALLOW) && renderNameplateEvent.getResult() != Result.DENY) {
			entityRendererAccessor.invokeRenderNameTag(entity, renderNameplateEvent.getContent(), poseStack, buffer, packedLight);
		}
	}
	
	public void mulPoseStack(PoseStack poseStack, Armature armature, E entity, T entitypatch, float partialTicks) {
		OpenMatrix4f modelMatrix = entitypatch.getModelMatrix(partialTicks);
        poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(180.0F));
        MathUtils.mulStack(poseStack, modelMatrix);
        
        if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
        	poseStack.translate(0.0D, entity.getBbHeight() + 0.1F, 0.0D);
        	poseStack.mulPose(QuaternionUtils.ZP.rotationDegrees(180.0F));
		}
	}
	
	public void setArmaturePose(T entitypatch, Armature armature, float partialTicks) {
		Pose pose = entitypatch.getAnimator().getPose(partialTicks);
        this.setJointTransforms(entitypatch, armature, pose, partialTicks);
        armature.setPose(pose);
	}
	
	public AssetAccessor<AM> getMeshProvider(T entitypatch) {
		return this.getDefaultMesh();
	}
	
	public abstract AssetAccessor<AM> getDefaultMesh();
	
	/**
	 * Developers shouldn't implement any interpolations in this method
	 * Use {@link LivingEntityPatch#poseTick} instead
	 */
	public void setJointTransforms(T entitypatch, Armature armature, Pose pose, float partialTicks) {
	}
}