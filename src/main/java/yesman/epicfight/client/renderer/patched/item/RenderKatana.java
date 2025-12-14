package yesman.epicfight.client.renderer.patched.item;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.item.EpicFightItems;

import java.util.Objects;

public class RenderKatana extends RenderItemBase {
	private final ItemStack sheathStack;
	
	public RenderKatana(JsonElement jsonElement) {
		super(jsonElement);
		
		if (jsonElement.getAsJsonObject().has("sheath")) {
			this.sheathStack = new ItemStack(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(jsonElement.getAsJsonObject().get("sheath").getAsString()))));
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
        
		modelMatrix = this.getCorrectionMatrix(entitypatch, InteractionHand.OFF_HAND, poses);
		
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
		itemRenderer.renderStatic(this.sheathStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();
    }
}