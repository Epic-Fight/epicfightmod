package yesman.epicfight.api.client.event.types.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.Event;

public class RenderEnderDragonEvent extends Event implements CancelableEvent {
	private final EnderDragon entity;
    private final EnderDragonRenderer renderer;
    private final float partialRenderTick;
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int light;
	
	public RenderEnderDragonEvent(EnderDragon entity, EnderDragonRenderer renderer, float partialRenderTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
		this.entity = entity;
        this.renderer = renderer;
        this.partialRenderTick = partialRenderTick;
        this.poseStack = poseStack;
        this.buffers = buffers;
        this.light = light;
	}
	
	public EnderDragon getEntity() {
		return entity;
	}

	public EnderDragonRenderer getRenderer() {
		return renderer;
	}
	
	public float getPartialRenderTick() {
		return partialRenderTick;
	}

	public PoseStack getPoseStack() {
		return poseStack;
	}

	public MultiBufferSource getBuffers() {
		return buffers;
	}

	public int getLight() {
		return light;
	}
}