package yesman.epicfight.mixin.client;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import yesman.epicfight.client.ClientEngine;

@Mixin(value = ParticleEngine.class)
public class MixinParticleEngine {
	@Shadow
	@Final
	private static List<ParticleRenderType> RENDER_ORDER;
	
	@Shadow
	@Mutable
	private Map<ParticleRenderType, Queue<Particle>> particles;
	
	@Inject(at = @At(value = "TAIL"), method = "<init>")
	private void epicfight$constructor(ClientLevel pLevel, TextureManager pTextureManager, CallbackInfo callbackInfo) {
		this.particles = Maps.newTreeMap(ClientEngine.makeCustomLowestParticleRenderTypeComparator(RENDER_ORDER));
	}
}
