package yesman.epicfight.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = MouseHandler.class)
public abstract class MixinMouseHandler {
    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
        ),
        method = "turnPlayer(D)V"
    )
    private void epicfight$turnPlayer(LocalPlayer instance, double yRot, double xRot, Operation<Void> original) {
        if (!EpicFightCameraAPI.getInstance().turnCamera(yRot, xRot)) original.call(instance, yRot, xRot);
    }
}
