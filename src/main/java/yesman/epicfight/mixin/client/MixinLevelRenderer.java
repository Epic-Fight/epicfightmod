package yesman.epicfight.mixin.client;

import java.util.Iterator;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = LevelRenderer.class)
public class MixinLevelRenderer {
	@Shadow
	private RenderBuffers renderBuffers;
	
	@Shadow
	private Minecraft minecraft;
	
	@Inject(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V",
			shift = Shift.AFTER
		),
		method = "renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void epicfight$renderLevel(
		DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo callbackInfo,
		// local variables
		TickRateManager local1,
		float local2,
		ProfilerFiller local3,
		Vec3 local4,
		double local5,
		double local6,
		double local7,
		boolean local8,
		Frustum local9,
		float local10,
		boolean local11,
		Matrix4fStack local12,
		boolean local13,
		PoseStack local14,
		MultiBufferSource.BufferSource local15,
		Iterator<?> local16,
		Entity local17,
		BlockPos local18,
		MultiBufferSource local19,
		OutlineBufferSource local20,
		int local21
	) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
			if (playerpatch.shouldHighlightTarget(local17)) this.renderBuffers.outlineBufferSource().setColor(255, 40, 40, 255);
		});
	}
}
