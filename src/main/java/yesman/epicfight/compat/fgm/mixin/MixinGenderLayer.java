package yesman.epicfight.compat.fgm.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wildfire.render.GenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(GenderLayer.class)
public class MixinGenderLayer<E extends LivingEntity>
{
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"), remap = false, cancellable = true)
    public void cancelIfEpicFight(@NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferSource, int light, @NotNull E e, float limbAngle, float limbDistance, float partialTicks, float animationProgress, float headYaw, float headPitch, CallbackInfo ci)
    {
        if (EpicFightCapabilities.getEntityPatch(e, EntityPatch.class) instanceof LivingEntityPatch<?> entityPatch)
        {
            if (entityPatch instanceof PlayerPatch<?> playerPatch && playerPatch.isEpicFightMode())
                ci.cancel();
        }
    }
}
