package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Inventory;
import yesman.epicfight.client.events.engine.ControlEngine;

@Mixin(Inventory.class)
public class MixinInventory {
    @Inject(
            // Note for maintainers: when porting to 1.21.2 or newer, target setSelectedHotbarSlot instead.
            // Some adjustments may be required. Please see: https://github.com/isXander/Controlify/blob/e90c94a9dfe45bc071e4ad01c4db039a0dd2492d/src/main/java/dev/isxander/controlify/ingame/InGameInputHandler.java#L96-L102
            // And test with Controlify to avoid regressions. Remove this comment as well.
            method = "swapPaint",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onCycleHotbarSlot(double direction, CallbackInfo ci) {
        // Called whenever the player changes their selected hotbar item via the mouse wheel or other input systems.
        if (ControlEngine.isHotbarCyclingDisabled()) {
            // InputEvent.MouseScrollingEvent is already cancelled in ControlEngine.Events#mouseScrollEvent to block hotbar cycling for mouse input.
            // Controller inputs are unaffected, so we also cancel it here to enforce the restriction
            // for all input methods.
            ci.cancel();
        }
    }
}
