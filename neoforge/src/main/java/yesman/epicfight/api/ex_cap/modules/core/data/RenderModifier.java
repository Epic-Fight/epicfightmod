package yesman.epicfight.api.ex_cap.modules.core.data;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@FunctionalInterface
public interface RenderModifier {
    /// Returns true if the original item renderer needs to be canceled, otherwise continues with the render chain.
    boolean modify(RenderItemBase itemBaseRenderer, ItemStack stack, LivingEntityPatch<?> livingEntityPatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource source, PoseStack poseStack, int packedLight, float partialTicks, ItemRenderer renderer, ItemInHandRenderer handRenderer);
}
