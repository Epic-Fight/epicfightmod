package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.InputEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.InputAction;

/// Internal utility for simplified input checks.
/// Consumers should avoid calling these methods, as they may break in future versions.
///
/// @see InputManager
@ApiStatus.Internal
public final class InputUtils {
    private InputUtils() {
    }

    /// Handles firing the [InputEvent.InteractionKeyMappingTriggered] input event for keyboard/mouse actions
    /// and runs the callback only if the event is not canceled.
    public static void runKeyboardMouseEvent(@NotNull InputAction action, @NotNull Runnable handler) {
        final KeyMapping keyMapping = action.keyMapping();

        final InputConstants.Key key = keyMapping.getKey();
        final boolean isMouse = InputConstants.Type.MOUSE == key.getType();

        final int mouseButton = isMouse ? key.getValue() : -1;

        if (checkInteractionKeyUsable(mouseButton, keyMapping)) {
            handler.run();
        }
    }

    public static void sneakingTick(boolean isSneaking, float sneakingSpeedMultiplier) {
        final LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            sneakingTick(localPlayer, isSneaking, sneakingSpeedMultiplier);
        }
    }

    /// Checks if the given key mapping is interaction key (block or entity interaction) and triggers
    /// [InputEvent.InteractionKeyMappingTriggered] event
    public static boolean checkInteractionKeyUsable(int mouseButton, KeyMapping keyMapping) {
        Options option = Minecraft.getInstance().options;

        if (
            keyMapping == option.keyAttack ||
                keyMapping == option.keyUse ||
                keyMapping == option.keyPickItem
        ) {
            @SuppressWarnings("UnstableApiUsage")
            InputEvent.InteractionKeyMappingTriggered inputEvent = ClientHooks.onClickInput(
                mouseButton, keyMapping, InteractionHand.MAIN_HAND
            );

            return !inputEvent.isCanceled();
        }

        return true;
    }

    /// Currently, this calls [Input#tick] without performing any additional logic.
    /// This abstraction was introduced to allow calling it without depending on the vanilla Minecraft [Input],
    /// enabling Epic Fight to introduce changes in future updates if necessary to support controllers.
    public static void sneakingTick(@NotNull LocalPlayer player, boolean isSneaking, float sneakingSpeedMultiplier) {
        player.input.tick(isSneaking, sneakingSpeedMultiplier);
    }
}
