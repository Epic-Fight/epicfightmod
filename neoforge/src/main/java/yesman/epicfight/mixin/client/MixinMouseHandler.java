package yesman.epicfight.mixin.client;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@Mixin(value = MouseHandler.class)
public abstract class MixinMouseHandler {
    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
        ),
        method = "turnPlayer(D)V"
    )
    private void epicfight$turnPlayer(LocalPlayer player, double yRot, double xRot) {
        if (!EpicFightCameraAPI.getInstance().turnCamera(yRot, xRot)) player.turn(yRot, xRot);
    }
}
