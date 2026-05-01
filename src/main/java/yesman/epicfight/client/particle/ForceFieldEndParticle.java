package yesman.epicfight.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.LightningRenderHelper;

public class ForceFieldEndParticle extends Particle {
	private boolean init;
	
	protected ForceFieldEndParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z);
		this.lifetime = 10;
		Minecraft mc = Minecraft.getInstance();
		mc.particleEngine.add(new DustParticle.ExpansiveMetaParticle(level, x, y, z, 6.0D, 80));
	}
	
	public ParticleRenderType getRenderType() {
		return EpicFightParticleRenderTypes.LIGHTNING;
	}
	
	@Override
	public void render(VertexConsumer vertexBuilder, Camera camera, float parttialTick) {
		PoseStack poseStack = new PoseStack();
		Vec3 vec3 = camera.getPosition();
		float f = (float) (Mth.lerp(parttialTick, this.xo, this.x) - vec3.x());
		float f1 = (float) (Mth.lerp(parttialTick, this.yo, this.y) - vec3.y());
		float f2 = (float) (Mth.lerp(parttialTick, this.zo, this.z) - vec3.z());
		poseStack.translate(f, f1, f2);
		
		if (this.age > 0) {
			float progression = ((float)this.age + parttialTick) / (float)this.lifetime;
			LightningRenderHelper.renderFlashingLight(vertexBuilder, poseStack, 255, 0, 255, 15, 1.0F, progression);
		}
		
		if (!this.init) {
			RenderEngine.getInstance().getOverlayManager().flickering("flickering", 0.05F, 1.2F);
			this.init = true;
		}
	}
	
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType typeIn, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			return new ForceFieldEndParticle(level, x, y, z);
		}
	}
}