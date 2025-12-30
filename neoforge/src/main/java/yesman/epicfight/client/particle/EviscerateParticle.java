package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.registry.entries.EpicFightParticles;

public class EviscerateParticle extends NoRenderParticle {
	protected EviscerateParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		
		for(int i = 0; i < 50; i++) {
			Vec3 rot = MathUtils.getVectorForRotation(0, (float)motionY);
			double particleMotionX = rot.x * this.random.nextFloat() * -0.5F;
			double particleMotionZ = rot.z * this.random.nextFloat() * -0.5F;
			this.level.addParticle(EpicFightParticles.BLOOD.get(), this.x, this.y, this.z, particleMotionX, 0.0D, particleMotionZ);
		}
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new EviscerateParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}
}