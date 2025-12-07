package yesman.epicfight.client.gui.screen.overlay;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class OverlayManager {
	private Map<String, OverlayManager.Overlay> overlays = Maps.newHashMap();
	private double modifiedGamma;
	private double originalGamma;
	private boolean isGammaChanged;

	public void renderTick(int xResolution, int yResolution) {
		this.isGammaChanged = false;
		this.overlays.entrySet().removeIf((entry) -> entry.getValue().render(xResolution, yResolution));
	}
	
	public void remove(String overlayId) {
		this.overlays.remove(overlayId);
	}
	
	public void setModifiedGamma(double originalGamma) {
		this.isGammaChanged = true;
		this.modifiedGamma = originalGamma;
	}

	public double getModifiedGamma(double originalGamma) {
		this.originalGamma = originalGamma;
		return this.modifiedGamma;
	}

	public double getOriginalGamma() {
		return this.originalGamma;
	}

	public boolean isGammaChanged() {
		return this.isGammaChanged;
	}

	public void blendingTexture(String overlayId, ResourceLocation texture) {
		if (Minecraft.renderNames()) {
			this.overlays.put(overlayId, new BlendingTextureOverlay(texture));
		}
	}
	
	public void flickering(String overlayId, float deltaTime, float strength) {
		if (Minecraft.renderNames()) {
			this.overlays.put(overlayId, new FlickeringOverlay(deltaTime, strength));
		}
	}
	
	public abstract static class Overlay {
		public abstract boolean render(int xResolution, int yResolution);
	}
}