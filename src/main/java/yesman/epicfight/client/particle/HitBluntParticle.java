package yesman.epicfight.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import yesman.epicfight.registry.entries.EpicFightParticles;

public class HitBluntParticle extends HitParticle {
	public HitBluntParticle(ClientLevel world, double x, double y, double z, double argX, double argY, double argZ, SpriteSet animatedSprite) {
		super(world, x, y, z, animatedSprite);
	    this.rCol = 1.0F;
	    this.gCol = 1.0F;
	    this.bCol = 1.0F;
	    this.quadSize = 1.0F;
		this.lifetime = 2;
		double d = 1.0F;
		
		for (int i = 0; i < 7; i++) {
			double particleMotionX = this.random.nextDouble() * d;
			d = d * (this.random.nextBoolean() ? 1.0D : -1.0D);
			double particleMotionZ = this.random.nextDouble() * d;
			d = d * (this.random.nextBoolean() ? 1.0D : -1.0D);
			this.level.addParticle(EpicFightParticles.DUST_EXPANSIVE.get(), this.x, this.y, this.z, particleMotionX, this.random.nextDouble() * 0.5D, particleMotionZ);
		}
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet spriteSet;
		public Provider(SpriteSet spriteSet) {
	         this.spriteSet = spriteSet;
	    }
	    
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			HitBluntParticle particle = new HitBluntParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
			return particle;
		}
	}
}