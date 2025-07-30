package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import yesman.epicfight.client.ClientEngine;

@Mixin(value = Minecraft.class)
public class MixinMinecraft {
	@Inject(at = @At(value = "HEAD"), method = "handleKeybinds()V", cancellable = true)
	private void epicfight_handleKeybinds(CallbackInfo info) {
		ClientEngine.getInstance().controlEngine.handleEpicFightKeyMappings();
	}
}
