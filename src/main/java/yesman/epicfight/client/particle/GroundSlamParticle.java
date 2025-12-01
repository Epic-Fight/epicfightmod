package yesman.epicfight.client.particle;

import java.util.Random;

import org.joml.Math;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.world.level.block.FractureBlockState;

@OnlyIn(Dist.CLIENT)
public class GroundSlamParticle extends NoRenderParticle {
	protected GroundSlamParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, BlockPos bp, BlockState bs) {
		super(level, x, y, z, dx, dy, dz);
		
		if (bs.isAir()) {
			bs = level.getBlockState(bp.below()); // one more step chance
		}
		
		if (bs instanceof FractureBlockState fractureBlockState) {
			bs = fractureBlockState.getOriginalBlockState(bp);
			
			if (bs == null) {
				bs = level.getBlockState(bp);
			}
		}
		
		if (!bs.shouldSpawnParticlesOnBreak()) {
			return;
		}
		
		Minecraft mc = Minecraft.getInstance();
		
		for (int i = 0; i < (int)dy; i ++) {
			Matrix4f mat = new Matrix4f().rotation((float) Math.toRadians(Math.random() * 360.0F), 0.0F, 1.0F, 0.0F);
			Vector3f positionVec = Matrix4fUtils.transform3v(mat, new Vector3f(0, 0, 1), new Vector3f()).mul((float)dx);
			Vector3f moveVec = Matrix4fUtils.transform3v(mat, new Vector3f(0, 0, 1), new Vector3f()).mul((float)dz);
			
			Particle blockParticle = new TerrainParticle(level, x + positionVec.x, y, z + positionVec.z, 0, 0, 0, bs, bp);
			blockParticle.setParticleSpeed((moveVec.x + (Math.random() - 0.5)) * 0.3D, (Math.random()) * 0.5D, (moveVec.z + (Math.random() - 0.5)) * 0.3D);
			blockParticle.setLifetime(60 + (new Random().nextInt(20)));
			
			Particle smokeParticle = mc.particleEngine.createParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x + positionVec.x * 0.5D, y + 1.5D, z + positionVec.z * 0.5D, 0, 0, 0); 
			smokeParticle.setParticleSpeed(moveVec.x * 0.1D, Math.random() * 0.05D, moveVec.z * 0.1D);
			smokeParticle.scale(3.0F);
			smokeParticle.setAlpha(0.33F);
			mc.particleEngine.add(blockParticle);
			mc.particleEngine.add(smokeParticle);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			BlockPos blockpos = new BlockPos.MutableBlockPos(x, y, z);
			BlockState blockstate = level.getBlockState(blockpos);
			if (blockstate == null) return null; 
			
			return new GroundSlamParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, blockpos, blockstate);
		}
	}
}