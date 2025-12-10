package yesman.epicfight.client.particle;

import java.util.function.Consumer;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.EntitySnapshot;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EntityAfterimageParticle extends CustomModelParticle<SkinnedMesh> {
	protected final EntitySnapshot<?> entitySnapshot;
	protected final Consumer<EntityAfterimageParticle> ticktask;
	protected float alphaO;

	public EntityAfterimageParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, EntitySnapshot<?> entitySnapshot, Consumer<EntityAfterimageParticle> ticktask) {
		super(level, x, y, z, xd, yd, zd, (AssetAccessor<SkinnedMesh>)null);
		
		this.entitySnapshot = entitySnapshot;
		this.ticktask = ticktask;
		this.rCol = 1.0F;
		this.gCol = 1.0F;
		this.bCol = 1.0F;
		this.alphaO = 1.0F;
		this.alpha = 1.0F;
		this.yawO = entitySnapshot.getYRot();
		this.yaw = entitySnapshot.getYRot();
	}
	
	@Override
	public void tick() {
		super.tick();
		this.alphaO = this.alpha;
		this.ticktask.accept(this);
	}
	
	@Override
	public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
		float alpha = Mth.lerp(partialTicks, this.alphaO, this.alpha);
		int lightColor = this.getLightColor(partialTicks);
		PoseStack poseStack = new PoseStack();
		this.setupPoseStack(poseStack, camera, partialTicks);
		MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
		this.entitySnapshot.renderTextured(poseStack, (MultiBufferSource)buffers, EpicFightRenderTypes::entityAfterimageStencil, Mesh.DrawingFunction.POSITION_TEX, 0, 0.0F, 0.0F, 0.0F, 1.0F);
		this.entitySnapshot.renderItems(poseStack, (MultiBufferSource)buffers, EpicFightRenderTypes.itemAfterimageStencil(), Mesh.DrawingFunction.POSITION_TEX, lightColor, 1.0F);
		buffers.endLastBatch();
		
		this.entitySnapshot.renderTextured(poseStack, (MultiBufferSource) buffers, EpicFightRenderTypes::entityAfterimageTranslucent, Mesh.DrawingFunction.NEW_ENTITY, lightColor, this.rCol, this.gCol, this.bCol, alpha);
		this.entitySnapshot.renderItems(poseStack, (MultiBufferSource) buffers, EpicFightRenderTypes.itemAfterimageTranslucent(), Mesh.DrawingFunction.NEW_ENTITY, lightColor, alpha);
		buffers.endLastBatch();
		
		this.revert(poseStack);
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return EpicFightParticleRenderTypes.ENTITY_PARTICLE;
	}
	
	@Override
	protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTick) {
		poseStack.pushPose();
		poseStack.mulPoseMatrix(RenderSystem.getModelViewStack().last().pose());
		RenderSystem.getModelViewStack().pushPose();
		RenderSystem.getModelViewStack().setIdentity();
		RenderSystem.applyModelViewMatrix();
		Vec3 cameraPosition = camera.getPosition();
		float x = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPosition.x());
		float y = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPosition.y());
		float z = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPosition.z());
		poseStack.translate(x, y, z);
		Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
		/**
		 * rotate the particle by entity's model matrix
		 * uncomment and revert to this code when problem occurs with model matrix
		 */
		/**
		float roll = Mth.rotLerp(partialTick, this.oRoll, this.roll);
		float pitch = Mth.rotLerp(partialTick, this.pitchO, this.pitch);
		float yaw = Mth.rotLerp(partialTick, this.yawO, this.yaw);
		rotation.mul((Quaternionfc)QuaternionUtils.YP.rotationDegrees(180.0F - this.yaw));
		rotation.mul((Quaternionfc) QuaternionUtils.XP.rotationDegrees(pitch));
		rotation.mul((Quaternionfc) QuaternionUtils.ZP.rotationDegrees(roll));
		**/
		rotation.mul((Quaternionfc)QuaternionUtils.YP.rotationDegrees(180.0F));
		poseStack.mulPose(rotation);
		poseStack.mulPoseMatrix(OpenMatrix4f.exportToMojangMatrix(this.entitySnapshot.getModelMatrix()));
		
		float scale = Mth.lerp(partialTick, this.scaleO, this.scale);
		poseStack.translate(0.0F, this.entitySnapshot.getHeightHalf(), 0.0F);
		poseStack.scale(scale, scale, scale);
		poseStack.translate(0.0F, -this.entitySnapshot.getHeightHalf(), 0.0F);
	}
	
	@Override
	protected void revert(PoseStack poseStack) {
		poseStack.popPose();
		RenderSystem.getModelViewStack().popPose();
		RenderSystem.applyModelViewMatrix();
	}

	public static class WhiteAfterimageParticle extends EntityAfterimageParticle {
		public WhiteAfterimageParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, EntitySnapshot<?> entitySnapshot, Consumer<EntityAfterimageParticle> ticktask) {
			super(level, x, y, z, xd, yd, zd, entitySnapshot, ticktask);
		}

		@Override
		public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks) {
			float alpha = Mth.lerp(partialTicks, this.alphaO, this.alpha);
			int lightColor = this.getLightColor(partialTicks);
			PoseStack poseStack = new PoseStack();
			this.setupPoseStack(poseStack, camera, partialTicks);
			MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
			this.entitySnapshot.renderTextured(poseStack, buffers, EpicFightRenderTypes::entityAfterimageStencil, Mesh.DrawingFunction.POSITION_TEX, 0, 0.0F, 0.0F, 0.0F, 1.0F);
			this.entitySnapshot.renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageStencil(), Mesh.DrawingFunction.POSITION_TEX, lightColor, 1.0F);
			buffers.endLastBatch();
			
			this.entitySnapshot.render(poseStack, buffers, EpicFightRenderTypes.entityAfterimageWhite(), Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP, lightColor, this.rCol, this.gCol, this.bCol, alpha);
			this.entitySnapshot.renderItems(poseStack, buffers, EpicFightRenderTypes.itemAfterimageWhite(), Mesh.DrawingFunction.POSITION_TEX_COLOR_LIGHTMAP, lightColor, alpha);
			buffers.endLastBatch();
			this.revert(poseStack);
		}
	}

	public static class AdrenalineParticleProvider implements ParticleProvider<SimpleParticleType> {
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			Entity entity = level.getEntity((int) Double.doubleToLongBits(xSpeed));
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
			
			if (entitypatch != null) {
				EntitySnapshot<?> entitySnapshot = entitypatch.captureEntitySnapshot();
				
				if (entitySnapshot != null) {
					EntityAfterimageParticle adrenalineparticle = new EntityAfterimageParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, entitySnapshot,
						particle -> {
							particle.alpha -= 0.025F;
							particle.scale += (-0.0025F * particle.age * particle.age + 1.0F) * 0.1F;
						}
					);
					
					adrenalineparticle.setLifetime(20);
					adrenalineparticle.setAlpha(0.6F);
					
					return adrenalineparticle;
				}
			}
			
			return null;
		}
	}

	public static class WhiteAfterimageProvider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			Entity entity = level.getEntity((int)Double.doubleToLongBits(xSpeed));
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
			
			if (entitypatch != null) {
				EntitySnapshot<?> entitySnapshot = entitypatch.captureEntitySnapshot();
				
				if (entitySnapshot != null) {
					EntityAfterimageParticle.WhiteAfterimageParticle afterimage = new EntityAfterimageParticle.WhiteAfterimageParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, entitySnapshot, particle -> {
						particle.alpha = (float)(particle.lifetime - particle.age) / particle.lifetime;
					});
					afterimage.setLifetime(20);
					
					return afterimage;
				}
			}
			
			return null;
		}
	}
}
