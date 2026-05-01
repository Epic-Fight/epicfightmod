package yesman.epicfight.client.gui.screen.overlay;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class BlendingTextureOverlay extends OverlayManager.Overlay {
	public ResourceLocation texture;
	private boolean isAlive = true;
	
	public BlendingTextureOverlay(ResourceLocation texture) {
		this.texture = texture;
	}
	
	public void remove() {
		this.isAlive = false;
	}
	
	@Override
	public boolean render(int xResolution, int yResolution) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, this.texture);
		GlStateManager._enableBlend();
		GlStateManager._disableDepthTest();
		GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tesselator tessellator = Tesselator.getInstance();
	    BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
	    bufferbuilder.addVertex(0, 0, 1).setUv(0, 0);
	    bufferbuilder.addVertex(0, yResolution, 1).setUv(0, 1);
	    bufferbuilder.addVertex(xResolution, yResolution, 1).setUv(1, 1);
	    bufferbuilder.addVertex(xResolution, 0, 1).setUv(1, 0);
	    BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
	    
	    return !this.isAlive;
	}
}