package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.screen.overlay.OverlayManager;

@Mixin(value = LightTexture.class)
public abstract class MixinLightTexture {
	@Inject(at = @At(value = "HEAD"), method = "updateLightTexture(F)V", cancellable = true)
	private void epicfight$head_updateLightTexture(CallbackInfo info) {
		OverlayManager overlayManager = RenderEngine.getInstance().getOverlayManager();
		
		if (overlayManager.isGammaChanged()) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.options.gamma().set(overlayManager.getModifiedGamma(minecraft.options.gamma().get()));
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "updateLightTexture(F)V", cancellable = true)
	private void epicfight$tail_updateLightTexture(CallbackInfo info) {
		OverlayManager overlayManager = RenderEngine.getInstance().getOverlayManager();
		
		if (overlayManager.isGammaChanged()) {
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.options.gamma().set(overlayManager.getOriginalGamma());
		}
	}
}