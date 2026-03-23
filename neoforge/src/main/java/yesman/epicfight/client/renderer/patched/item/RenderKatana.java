package yesman.epicfight.client.renderer.patched.item;

import java.util.Objects;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSet;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

public class RenderKatana extends RenderItemBase {
	private final ItemStack sheathStack;

    public ItemStack getSheathStack() {
        return sheathStack.copy();
    }
	
	public RenderKatana(JsonElement jsonElement) {
		super(jsonElement);
		
		if (jsonElement.getAsJsonObject().has("sheath")) {
			this.sheathStack = new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse(jsonElement.getAsJsonObject().get("sheath").getAsString()))));
		} else {
			this.sheathStack = new ItemStack(EpicFightItems.UCHIGATANA_SHEATH.get());
		}
	}
	
	@Override
	public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
        OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, InteractionHand.MAIN_HAND, poses);
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
        itemRenderer.renderStatic(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();
        if (entitypatch.getHoldingItemCapability(InteractionHand.MAIN_HAND) instanceof WeaponCapability wCap && wCap.getCurrentSet(entitypatch) instanceof MoveSet set && set.shouldRenderSheath().test(entitypatch))
        {
            modelMatrix = this.getCorrectionMatrix(entitypatch, InteractionHand.OFF_HAND, poses);
            poseStack.pushPose();
            MathUtils.mulStack(poseStack, modelMatrix);
            itemRenderer.renderStatic(this.sheathStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
            poseStack.popPose();
        }

    }
}