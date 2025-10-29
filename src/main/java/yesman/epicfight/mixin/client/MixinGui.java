package yesman.epicfight.mixin.client;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.client.gui.EntityUI;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = Gui.class)
public abstract class MixinGui {
	@Shadow
	@Final
	private static ResourceLocation GUI_ICONS_LOCATION;
	@Shadow
	private Minecraft minecraft;
	@Shadow
	private int screenWidth;
	@Shadow
	private int screenHeight;
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
			ordinal = 0
		),
		method = "renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;)V"
	)
	public void epicfight$renderCrosshair(GuiGraphics pGuiGraphics, ResourceLocation atlasLocation, int x, int y, int uOffset, int vOffset, int uWidth, int vHeight) {
		MutableBoolean itemAction = new MutableBoolean(true); // true: combat, false: mine
		
		if (ClientConfig.mineBlockGuideOption.switchCrosshair()) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
				itemAction.setValue(playerpatch.canPlayAttackAnimation());
			});
		}
		
		if (itemAction.booleanValue()) {
			pGuiGraphics.blit(GUI_ICONS_LOCATION, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
		} else {
			pGuiGraphics.blit(EntityUI.BATTLE_ICON, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 240, 15, 15);
		}
	}
}