package yesman.epicfight.api.client.input.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.handlers.InputManager;

/**
 * Internal utility for simplified input checks.
 * Clients should use {@link InputManager} directly.
 */
@ApiStatus.Internal
public final class InputUtils {
    private InputUtils() {
    }

    public static void sneakingTick(boolean isSneaking, float sneakingSpeedMultiplier) {
        final LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null) {
            sneakingTick(localPlayer, isSneaking, sneakingSpeedMultiplier);
        }
    }

    /**
     * Currently, this calls {@link Input#tick} without performing any additional logic.
     * This abstraction was introduced to allow calling it without depending on the vanilla Minecraft {@link Input},
     * enabling Epic Fight to introduce changes in future updates if necessary to support controllers.
     */
    public static void sneakingTick(@NotNull LocalPlayer player, boolean isSneaking, float sneakingSpeedMultiplier) {
        player.input.tick(isSneaking, sneakingSpeedMultiplier);
    }
}
