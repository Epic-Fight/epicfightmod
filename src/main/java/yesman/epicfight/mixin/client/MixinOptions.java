package yesman.epicfight.mixin.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.config.ClientConfig;

@Mixin(Options.class)
public class MixinOptions {

    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void onSetCameraType(CameraType requested, CallbackInfo ci) {
        final ClientConfig.CameraPerspectiveToggleMode preference = ClientConfig.CAMERA_PERSPECTIVE_TOGGLE_MODE.get();
        final Options options = Minecraft.getInstance().options;

        switch (preference) {
            case VANILLA -> {
                // No-op
            }
            case SKIP_THIRD_PERSON_FRONT -> {
                if (requested == CameraType.THIRD_PERSON_FRONT) {
                    final CameraType current = options.getCameraType();
                    final CameraType replacement = (current == CameraType.FIRST_PERSON)
                            ? CameraType.THIRD_PERSON_BACK
                            : CameraType.FIRST_PERSON;

                    options.setCameraType(replacement);
                    ci.cancel();
                }
            }
        }
    }
}
