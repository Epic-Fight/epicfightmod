package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import yesman.epicfight.client.events.engine.RenderEngine;

@Mixin(value = GameRenderer.class)
public class MixinGameRenderer {
	@Inject(
		method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
			shift = At.Shift.AFTER
		)
	)
	private void epicfight$renderLevel(DeltaTracker deltaTracker, CallbackInfo callbackInfo, @Local Camera camera, @Local(ordinal = 1) float f1) {
		CameraType cameraType = Minecraft.getInstance().options.getCameraType();
		RenderEngine.getInstance().setRangedWeaponThirdPerson(camera, cameraType, f1);
	}
}
