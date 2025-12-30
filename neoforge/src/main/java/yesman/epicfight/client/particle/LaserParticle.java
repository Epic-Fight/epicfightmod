package yesman.epicfight.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import yesman.epicfight.api.client.model.ClassicMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.QuaternionUtils;

public class LaserParticle extends CustomModelParticle<ClassicMesh> {
	private final float length;
	private final float xRot;
	private final float yRot;
	
	public LaserParticle(ClientLevel level, double x, double y, double z, double toX, double toY, double toZ) {
		super(level, x, y, z, 0, 0, 0, Meshes.LASER);
		this.lifetime = 5;
		
		Vec3 direction = new Vec3(toX - x, toY - y, toZ - z);
		Vec3 start = new Vec3(x, y, z);
		Vec3 destination = start.add(direction.normalize().scale(200.0D));
		BlockHitResult hitResult = level.clip(new ClipContext(start, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
		double xLength = hitResult.getLocation().x - x;
		double yLength = hitResult.getLocation().y - y;
		double zLength = hitResult.getLocation().z - z;
		double horizontalDistance = (float)Math.sqrt(xLength * xLength + zLength * zLength);
		this.length = (float)Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
		this.yRot = (float)(Math.atan2(zLength, xLength) * (180D / Math.PI)) + 90.0F + 180.0F;
		this.xRot = (float)(Math.atan2(yLength, horizontalDistance) * (180D / Math.PI));
		int smokeCount = (int)this.length * 4;
		
		for (int i = 0; i < smokeCount; i++) {
			level.addParticle(ParticleTypes.SMOKE, x + xLength / smokeCount * i, y + yLength / smokeCount * i, z + zLength / smokeCount * i, 0, 0, 0);
		}
		
		this.setBoundingBox(new AABB(x, y, z, toX, toY, toZ));
	}
	
	@Override
	protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTick) {
		poseStack.pushPose();
		Vec3 cameraPosition = camera.getPosition();
		float x = (float)(Mth.lerp(partialTick, this.xo, this.x) - cameraPosition.x());
		float y = (float)(Mth.lerp(partialTick, this.yo, this.y) - cameraPosition.y());
		float z = (float)(Mth.lerp(partialTick, this.zo, this.z) - cameraPosition.z());
		poseStack.translate(x, y, z);
		poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(180.0F - this.yRot));
		poseStack.mulPose(QuaternionUtils.XP.rotationDegrees(this.xRot));
		
		float progression = (this.age + partialTick) / (this.lifetime + 1);
		float scale = Mth.sin(progression * (float)Math.PI);
		float zScale = progression > 0.5F ? 1.0F : Mth.sin(progression * (float)Math.PI);
		
		poseStack.scale(scale, scale, zScale * this.length);
	}
	
	@Override
	public ParticleRenderType getRenderType() {
		return EpicFightParticleRenderTypes.TRANSLUCENT_GLOWING;
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double startX, double startY, double startZ, double endX, double endY, double endZ) {
			return new LaserParticle(level, startX, startY, startZ, endX, endY, endZ);
		}
	}
}