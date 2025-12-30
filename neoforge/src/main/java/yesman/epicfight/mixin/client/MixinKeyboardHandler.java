package yesman.epicfight.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.ClientEngine;

@Mixin(value = KeyboardHandler.class)
public abstract class MixinKeyboardHandler {
	@Shadow
	private long debugCrashKeyTime = -1L;

	@Inject(at = @At(value = "HEAD"), method = "handleDebugKeys(I)Z", cancellable = true)
	private void epicfight$handleDebugKeys(int key, CallbackInfoReturnable<Boolean> info) {
		if (!(this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L)) {
            if (key == InputConstants.KEY_Y) {
                boolean flag = ClientEngine.getInstance().switchVanillaModelDebuggingMode();
                this.debugFeedbackTranslated(flag ? "debug.vanilla_model_debugging.on" : "debug.vanilla_model_debugging.off");
                info.cancel();
                info.setReturnValue(true);
            }
		}
	}
	
	@Shadow
    public void debugFeedbackTranslated(String p_90914_, Object... p_90915_) {}
}