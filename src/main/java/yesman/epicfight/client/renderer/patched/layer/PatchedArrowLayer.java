package yesman.epicfight.client.renderer.patched.layer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class PatchedArrowLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends PlayerModel<E>> extends PatchedStuckInBodyLayer<E, T, M, ArrowLayer<E, M>> {
	private final EntityRenderDispatcher dispatcher;
	
	public PatchedArrowLayer(EntityRendererProvider.Context context) {
		this.dispatcher = context.getEntityRenderDispatcher();
	}
	
	@Override
	protected void renderStuckItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Entity entity, float f1, float f2, float f3, float partialTick) {
		float f = Mth.sqrt(f1 * f1 + f3 * f3);
		Arrow arrow = new Arrow(entity.level(), entity.getX(), entity.getY(), entity.getZ(), ItemStack.EMPTY, null);
		arrow.setYRot((float) (Math.atan2((double) f1, (double) f3) * (double) (180F / (float) Math.PI)));
		arrow.setXRot((float) (Math.atan2((double) f2, (double) f) * (double) (180F / (float) Math.PI)));
		arrow.yRotO = arrow.getYRot();
		arrow.xRotO = arrow.getXRot();
		this.dispatcher.render(arrow, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	protected int numStuck(E entity) {
		return entity.getArrowCount();
	}
}
