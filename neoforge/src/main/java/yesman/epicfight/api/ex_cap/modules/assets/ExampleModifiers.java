package yesman.epicfight.api.ex_cap.modules.assets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class ExampleModifiers
{
    public static boolean uchigatanaRender(RenderItemBase baseRenderer, ItemStack stack, LivingEntityPatch<?> livingEntityPatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource source, PoseStack poseStack, int packedLight, float partialTicks, ItemRenderer renderer)
    {
        OpenMatrix4f modelMatrix = baseRenderer.getCorrectionMatrix(livingEntityPatch, InteractionHand.OFF_HAND, poses);
        poseStack.pushPose();
        MathUtils.mulStack(poseStack, modelMatrix);
        renderer.renderStatic(new ItemStack(EpicFightItems.UCHIGATANA_SHEATH), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, source, null, 0);
        poseStack.popPose();
        //Let the render process complete, true if the render needs to be completely overridden.
        return false;
    }

}
