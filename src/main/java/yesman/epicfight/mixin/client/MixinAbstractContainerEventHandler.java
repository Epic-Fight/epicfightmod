package yesman.epicfight.mixin.client;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Mixin(value = AbstractContainerEventHandler.class)
public abstract class MixinAbstractContainerEventHandler {
	@Shadow
	private GuiEventListener focused;
	
	@Inject(at = @At(value = "HEAD"), method = "setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V", cancellable = true)
	private void epicfight_setFocused(@Nullable GuiEventListener widget, CallbackInfo info) {
		if (this.focused == widget) {
			info.cancel();
		}
	}
}