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

	/// Skips the redundant unfocus + refocus dance when the container's currently-focused widget
	/// is being re-set to itself. Was previously unconditional on `this.focused == widget`, but
	/// that broke text fields like vanilla EditBox / Controlling's search box: an EditBox can
	/// internally drop its own `isFocused` flag (when the mouse clicks outside it) without the
	/// container's `focused` reference changing. On the next click that lands back on the EditBox
	/// the call would be cancelled, and the widget's internal `setFocused(true)` would never run.
	/// Now we additionally require the widget to *actually* still be focused; if it's gone
	/// out-of-sync with the container, we let the vanilla code re-focus it properly.
	@Inject(at = @At(value = "HEAD"), method = "setFocused(Lnet/minecraft/client/gui/components/events/GuiEventListener;)V", cancellable = true)
	private void epicfight_setFocused(@Nullable GuiEventListener widget, CallbackInfo info) {
		if (this.focused == widget && (widget == null || widget.isFocused())) {
			info.cancel();
		}
	}
}