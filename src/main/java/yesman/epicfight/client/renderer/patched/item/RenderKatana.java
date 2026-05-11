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
import yesman.epicfight.api.ex_cap.data.Moveset;
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
        // The blade renders at the joint of whichever hand actually holds it; the sheath rides on
        // the opposite hand. The original implementation hardcoded MAIN_HAND for the blade and
        // OFF_HAND for the sheath, which left the katana stuck on the right side and the sheath
        // invisible whenever the player swapped to mirror mode. Following the live `hand` argument
        // makes both pieces follow the X-flip applied at ClientAnimator.getPose, so they appear on
        // the correct sides without authoring a mirrored variant. The display context stays
        // THIRD_PERSON_RIGHT_HAND for both pieces -- the underlying data is always authored
        // right-handed, and the pose mirror plus the side-aware parent joint already handle the
        // visual flip; switching the display context on top would double-mirror and offset the
        // model from the hand.
        InteractionHand sheathHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

        OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, hand, poses);
		poseStack.pushPose();
		MathUtils.mulStack(poseStack, modelMatrix);
        itemRenderer.renderStatic(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();
        if (entitypatch.getHoldingItemCapability(hand) instanceof WeaponCapability wCap && wCap.getCurrentSet(entitypatch) instanceof Moveset set && set.shouldRenderSheath().test(entitypatch))
        {
            modelMatrix = this.getCorrectionMatrix(entitypatch, sheathHand, poses);
            poseStack.pushPose();
            MathUtils.mulStack(poseStack, modelMatrix);
            itemRenderer.renderStatic(this.sheathStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
            poseStack.popPose();
        }

    }
}