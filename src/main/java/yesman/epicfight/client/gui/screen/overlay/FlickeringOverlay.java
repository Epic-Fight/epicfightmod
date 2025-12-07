package yesman.epicfight.client.gui.screen.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import yesman.epicfight.client.events.engine.RenderEngine;

public class FlickeringOverlay extends OverlayManager.Overlay {
	private float time = (float)-Math.PI;
	private final float deltaTime;
	private final float strength;
	private final double initialGamma;
	
	public FlickeringOverlay(float deltaTime, float strength) {
		this.deltaTime = deltaTime;
		this.strength = strength;
		Minecraft minecraft = Minecraft.getInstance();
		this.initialGamma = minecraft.options.gamma().get();
	}
	
	@Override
	public boolean render(int xResolution, int yResolution) {
		this.time += this.deltaTime;
		float darkenAmount = Mth.clamp((float)Math.sin(this.time), -1.0F, 0.0F);
		
		OverlayManager overlayManager = RenderEngine.getInstance().getOverlayManager();
		float gamma = (float)Math.max(this.initialGamma + darkenAmount * this.strength, 0.0F);
		overlayManager.setModifiedGamma(gamma);

		if (this.time >= 0) {
			return true;
		}

		return false;
	}
}