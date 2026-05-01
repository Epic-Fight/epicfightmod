package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = Gui.class)
public abstract class MixinGui {
	
	@Final
    @Shadow
	private Minecraft minecraft;
	
	/**
	 * Render crosshair in third person TPS mode
	 */
	@Inject(
		at = @At(
			value = "TAIL"
		),
		method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
	)
	private void renderCrosshairINJECT(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callback) {
		// Draw crosshair in TPS mode, since vanilla crosshair aren't rendered in third person.
		if (EpicFightCameraAPI.getInstance().isTPSMode()) {
			this.epicfight$renderCrosshair(guiGraphics, true);
		}
	}
	
	/**
	 * Replace the crosshair into mining indicator
	 */
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V",
			ordinal = 0
		),
		method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
	)
	private void renderCrosshairREDIRECT(GuiGraphics guiGraphics, ResourceLocation sprite, int x, int y, int width, int height) {
		this.epicfight$renderCrosshair(guiGraphics, false);
	}
	
	@Unique
	private void epicfight$renderCrosshair(GuiGraphics guiGraphics, boolean setupBlend) {
		if (setupBlend) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        }
		
		MutableBoolean drawVanillaCrosshair = new MutableBoolean(true);
		
		if (ClientConfig.mineBlockGuideOption.switchCrosshair()) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
				if (playerpatch.isVanillaMode()) {
					drawVanillaCrosshair.setValue(RenderEngine.hitResultNotEquals(this.minecraft.hitResult, HitResult.Type.BLOCK));
				} else {
					drawVanillaCrosshair.setValue(playerpatch.canPlayAttackAnimation());
				}
			});
		}
		
		if (drawVanillaCrosshair.booleanValue()) {
            guiGraphics.blitSprite(Gui.CROSSHAIR_SPRITE, (guiGraphics.guiWidth() - 15) / 2, (guiGraphics.guiHeight() - 15) / 2, 15, 15);
		} else {
            guiGraphics.blit(EntityUI.BATTLE_ICON, (guiGraphics.guiWidth() - 15) / 2, (guiGraphics.guiHeight() - 15) / 2, 0, 240, 15, 15);
		}
		
		if (setupBlend) RenderSystem.defaultBlendFunc();
	}
}
