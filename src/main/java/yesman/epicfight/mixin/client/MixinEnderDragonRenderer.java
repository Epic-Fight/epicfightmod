package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.common.MinecraftForge;
import yesman.epicfight.api.client.forgeevent.RenderEnderDragonEvent;

@Mixin(value = EnderDragonRenderer.class)
public abstract class MixinEnderDragonRenderer {
	@Inject(at = @At(value = "HEAD"), method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", cancellable = true)
	private void epicfight_render(EnderDragon enderdragon, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource multiSourceBuffer, int packedLight, CallbackInfo info) {
		RenderEnderDragonEvent renderDragonEvent = new RenderEnderDragonEvent(enderdragon, (EnderDragonRenderer)((Object)this), partialTicks, poseStack, multiSourceBuffer, packedLight);
		
		if (MinecraftForge.EVENT_BUS.post(renderDragonEvent)) {
			info.cancel();
		}
	}
}