package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = Minecraft.class)
public class MixinMinecraft {
	@Shadow
	private LocalPlayer player;
	
	@Inject(at = @At(value = "HEAD"), method = "handleKeybinds()V", cancellable = true)
	private void epicfight$handleKeybinds(CallbackInfo callbackInfo) {
		ClientEngine.getInstance().controlEngine.handleEpicFightKeyMappings();
	}
	
	@Inject(at = @At(value = "HEAD"), method = "shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
	private void epicfight$shouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> callbackInfo) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(this.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
			if (playerpatch.shouldHighlightTarget(entity)) {
				callbackInfo.setReturnValue(true);
				callbackInfo.cancel();
			}
		});
	}
}
