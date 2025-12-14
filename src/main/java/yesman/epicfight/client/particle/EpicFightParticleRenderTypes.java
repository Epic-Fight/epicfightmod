package yesman.epicfight.client.particle;

import java.util.function.Function;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class EpicFightParticleRenderTypes {
	public static final ParticleRenderType PARTICLE_MODEL_NO_NORMAL = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.disableCull();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.depthMask(true);
			RenderSystem.setShader(GameRenderer::getParticleShader);

			bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.PARTICLE);
		}
		
		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
			RenderSystem.enableCull();
		}

		public String toString() {
			return "epicfight:PARTICLE_MODEL_NO_NORMAL";
		}
	};

	public static final ParticleRenderType LIGHTNING = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
		    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			RenderSystem.depthMask(false);
	        RenderSystem.setShader(GameRenderer::getRendertypeLightningShader);
	        
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		}
		
		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
		}
		
		@Override
		public String toString() {
			return "epicfight:LIGHTING";
		}
	};
	
	public static final Function<ResourceLocation, ParticleRenderType> TRAIL_EFFECT = Util.memoize(textureLocation -> {
		TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
		AbstractTexture abstracttexture = texturemanager.getTexture(textureLocation);
	    
		// Set texture parameter
		RenderSystem.bindTexture(abstracttexture.getId());
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
	    RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		return new ParticleRenderType() {
			@Override
			public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
				RenderSystem.disableCull();
				RenderSystem.enableBlend();
			    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				RenderSystem.depthMask(false);
				RenderSystem.setShaderTexture(0, textureLocation);
				
				bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
			}
			
			@Override
			public void end(Tesselator tesselator) {
				tesselator.end();
				
				RenderSystem.enableCull();
			}
			
			@Override
			public String toString() {
				return "epicfight:TRAIL_EFFECT";
			}
		};
	});
	
	public static final ParticleRenderType TRANSLUCENT_GLOWING = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.depthMask(true);
	        RenderSystem.setShader(GameRenderer::getPositionColorShader);
	        
			bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
		}
		
		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
		}

		@Override
		public String toString() {
			return "epicfight:TRANSLUCENT_GLOWING";
		}
	};
	
	public static final ParticleRenderType ENTITY_PARTICLE = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferbuilder, TextureManager texManager) {
			RenderSystem.depthMask(true);
			RenderSystem.setShader(GameRenderer::getParticleShader);
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}
		
		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
			Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		}
		
		@Override
		public String toString() {
			return "epicfight:ENTITY_PARTICLE";
		}
	};
}
