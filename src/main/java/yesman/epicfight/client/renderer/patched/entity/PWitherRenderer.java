package yesman.epicfight.client.renderer.patched.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.mesh.WitherMesh;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.layer.PatchedWitherArmorLayer;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;

@OnlyIn(Dist.CLIENT)
public class PWitherRenderer extends PatchedLivingEntityRenderer<WitherBoss, WitherPatch, WitherBossModel<WitherBoss>, WitherBossRenderer, WitherMesh> {
	public static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
	private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");
	
	public PWitherRenderer() {
		this.addPatchedLayer(WitherArmorLayer.class, new PatchedWitherArmorLayer());
	}
	
	@Override
	public void render(WitherBoss entityIn, WitherPatch entitypatch, WitherBossRenderer renderer, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		boolean isVisible = this.isVisible(entityIn, entitypatch);
		boolean isVisibleToPlayer = !isVisible && !entityIn.isInvisibleTo(mc.player);
		boolean isGlowing = mc.shouldEntityAppearGlowing(entityIn);
		RenderType renderType = this.getRenderType(entityIn, entitypatch, renderer, isVisible, isVisibleToPlayer, isGlowing);
		WitherMesh mesh = this.getMesh(entitypatch);
		Armature armature = entitypatch.getArmature();
		poseStack.pushPose();
		this.mulPoseStack(poseStack, armature, entityIn, entitypatch, partialTicks);
		OpenMatrix4f[] poseMatrices = this.getPoseMatrices(entitypatch, armature, partialTicks);
		
		if (renderType != null) {
			int transparencyCount = entitypatch.getTransparency();
			
			if (transparencyCount == 0) {
				if (!entitypatch.isGhost()) {
					VertexConsumer builder = buffer.getBuffer(renderType);
					mesh.drawModelWithPose(poseStack, builder, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, this.getOverlayCoord(entityIn, entitypatch, partialTicks), armature, poseMatrices);
				}
			} else {
				float transparency = (Math.abs(transparencyCount) + partialTicks) / 41.0F;
				
				if (transparencyCount < 0) {
					transparency = 1.0F - transparency;
				}
				
				renderType = EpicFightRenderTypes.getTriangulated(RenderType.entityTranslucent(WITHER_LOCATION));
				VertexConsumer builder1 = buffer.getBuffer(renderType);
				mesh.drawModelWithPose(poseStack, builder1, packedLight, 1.0F, 1.0F, 1.0F, transparency, OverlayTexture.NO_OVERLAY, armature, poseMatrices);
				
				renderType = EpicFightRenderTypes.getTriangulated(RenderType.entityTranslucent(WITHER_INVULNERABLE_LOCATION));
				VertexConsumer builder2 = buffer.getBuffer(renderType);
				mesh.drawModelWithPose(poseStack, builder2, packedLight, 1.0F, 1.0F, 1.0F, Mth.sin(transparency * 3.1415F), OverlayTexture.NO_OVERLAY, armature, poseMatrices);
			}
			
			this.renderLayer(renderer, entitypatch, entityIn, poseMatrices, buffer, poseStack, packedLight, partialTicks);
		}
		
		if (renderType != null) {
			if (Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
				for (Layer.Priority priority : Layer.Priority.HIGHEST.lowers()) {
					AnimationPlayer animPlayer = entitypatch.getClientAnimator().getCompositeLayer(priority).animationPlayer;
					float playTime = animPlayer.getPrevElapsedTime() + (animPlayer.getElapsedTime() - animPlayer.getPrevElapsedTime()) * partialTicks;
					animPlayer.getAnimation().renderDebugging(poseStack, buffer, entitypatch, playTime, partialTicks);
				}
			}
		}
		
		poseStack.popPose();
	}
	
	protected boolean isVisible(WitherBoss witherboss, WitherPatch witherpatch) {
		return !witherpatch.isGhost() || witherpatch.getTransparency() != 0;
	}
	
	@Override
	public void mulPoseStack(PoseStack poseStack, Armature armature, WitherBoss witherboss, WitherPatch entitypatch, float partialTicks) {
		super.mulPoseStack(poseStack, armature, witherboss, entitypatch, partialTicks);
        
		float f = 1.0F;
		int i = witherboss.getInvulnerableTicks();
		
		if (i > 0) {
			f -= ((float) i - partialTicks) / 440.0F;
		}

		poseStack.scale(f, f, f);
	}
	
	@Override
	public WitherMesh getMesh(WitherPatch entitypatch) {
		return Meshes.WITHER;
	}
}