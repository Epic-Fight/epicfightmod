package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

@Mixin(value = EntityRenderer.class)
public interface MixinEntityRenderer {
	@Invoker("shouldShowName")
	public boolean invokeShouldShowName(Entity entity);
	
	@Invoker("renderNameTag")
	public void invokeRenderNameTag(Entity entity, Component name, PoseStack poseStack, MultiBufferSource bufferSource, int i1, float partialTick);
}
