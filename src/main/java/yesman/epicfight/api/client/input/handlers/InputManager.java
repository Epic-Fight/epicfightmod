package yesman.epicfight.api.client.input.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputActions;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.api.client.input.controller.ControllerBinding.InputType;

/**
 * High-level input API that abstracts direct interactions with {@link KeyMapping}
 * and supports controllers if an Epic Fight controller mod implementation is present
 * (see {@link EpicFightControllerModProvider}).
 * <p>
 * Use this class whenever possible to ensure input works consistently across
 * keyboard/mouse and supported controllers.
 */
@ApiStatus.Experimental
public final class InputManager {
    private InputManager() {
    }

    @Nullable
    private static IEpicFightControllerMod getControllerModApi() {
        return EpicFightControllerModProvider.get();
    }

    /**
     * Checks if controller or gamepad input is currently supported.
     *
     * <p><b>Note:</b> The {@link InputMode#MIXED} mode supports both controller and keyboard/mouse input at the same time.
     * Returning {@code true} here does not necessarily mean the input mode is exclusively {@link InputMode#CONTROLLER}.
     *
     * @return {@code true} if a controller mod is present and the current input mode allows controller input.
     * @see InputMode
     */
    public static boolean supportsControllerInput() {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        if (controllerMod == null) {
            return false;
        }
        return controllerMod.getInputMode().supportsController();
    }

    /**
     * Returns whether the given input action is currently active this tick.
     * <p>
     * Checks the action state depending on the current input mode:
     * <ul>
     *     <li>{@link InputMode#KEYBOARD_MOUSE}: whether the key mapping is down.</li>
     *     <li>{@link InputMode#CONTROLLER}: whether the controller binding digital state is active.</li>
     *     <li>{@link InputMode#MIXED}: true if either the key mapping is down or the controller binding state is active.</li>
     * </ul>
     * If no controller mod is present, only the key mapping is checked.
     * This is usually useful for continuous actions.
     *
     * @param action the input action to check
     * @return true if the action is currently active in this tick; false otherwise
     * @see InputType
     */
    public static boolean isActionActive(@NotNull EpicFightInputActions action) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        if (controllerMod == null) {
            return isKeyDown(action.keyMapping());
        }

        return switch (controllerMod.getInputMode()) {
            case KEYBOARD_MOUSE -> isKeyDown(action.keyMapping());
            case CONTROLLER -> controllerMod.getBinding(action).isDigitalActiveNow();
            case MIXED -> isKeyDown(action.keyMapping()) || controllerMod.getBinding(action).isDigitalActiveNow();
        };
    }

    /**
     * Called on every client tick to potentially trigger the provided callback for a given input action.
     * <p>
     * This calls the internal {@link DiscreteInputActionTrigger#triggerOnPress} API, and additionally fires
     * the {@link InputEvent.InteractionKeyMappingTriggered} input event for keyboard/mouse if {@code interactionKeyEventCheck} is {@code true}.
     *
     * @param action     The input action to monitor and trigger.
     * @param interactionKeyEventCheck If {@code true}, fires the {@link InputEvent.InteractionKeyMappingTriggered} event for non-controller actions.
     *                   This event is cancellable.
     * @param handler    The callback to invoke when the action triggers.
     * @see DiscreteInputActionTrigger#triggerOnPress Internal implementation details.
     */
    public static void triggerOnPress(@NotNull EpicFightInputActions action, boolean interactionKeyEventCheck, @NotNull DiscreteActionHandler handler) {
        DiscreteInputActionTrigger.triggerOnPress(action, (context) -> {
            if (context.triggeredByController() || !interactionKeyEventCheck) {
                handler.onAction(context);
                return;
            }

            runKeyboardMouseEvent(action, handler);
        });
    }

    /**
     * Convenience overload of {@link #triggerOnPress(EpicFightInputActions, boolean, DiscreteActionHandler)}
     * for callbacks that do not require the {@link DiscreteActionHandler.Context}.
     *
     * @see #triggerOnPress(EpicFightInputActions, boolean, DiscreteActionHandler)
     */
    public static void triggerOnPress(@NotNull EpicFightInputActions action, boolean interactionKeyEventCheck, @NotNull Runnable runnable) {
        triggerOnPress(action, interactionKeyEventCheck, (context) -> runnable.run());
    }

    /**
     * Checks whether the given input action is assigned to the same key / button as another action.
     * <p>
     * For keyboard/mouse, this compares the key codes; for controllers, it compares the digital button.
     * <p><b>Note:</b> {@link InputMode#MIXED} is currently unsupported and its behavior is undefined.</p>
     *
     * @param action  the first input action
     * @param action2 the second input action
     * @return true if both actions are triggered by the same key or controller button; false otherwise
     */
    public static boolean isBoundToSamePhysicalInput(@NotNull EpicFightInputActions action, @NotNull EpicFightInputActions action2) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        if (controllerMod != null && controllerMod.getInputMode() == InputMode.CONTROLLER) {
            return controllerMod.isBoundToSameButton(action, action2);
        }

        final KeyMapping keyMapping1 = action.keyMapping();
        final KeyMapping keyMapping2 = action2.keyMapping();
        return keyMapping1.getKey() == keyMapping2.getKey();
    }

    /**
     * Retrieves the current input state for the current player (client-side).
     * <p><b>Note:</b> {@link InputMode#MIXED} is currently unsupported and its behavior is undefined.</p>
     * You should use this method instead of depending on the vanilla {@link Input} directly
     * to support controllers.
     * <p>
     * The {@link PlayerInputState} is immutable, so properties cannot be updated directly, for that,
     * use {@link InputManager#setInputState}.
     *
     * @param vanillaInput the Minecraft vanilla {@link Input} which will be mapped to a {@link PlayerInputState};
     *                     ignored if using a controller.
     * @return an immutable {@link PlayerInputState} representing the current input state.
     * @see InputManager#setInputState
     */
    @NotNull
    public static PlayerInputState getInputState(@NotNull Input vanillaInput) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        if (controllerMod != null && controllerMod.getInputMode() == InputMode.CONTROLLER) {
            return controllerMod.getInputState();
        }

        return PlayerInputState.fromVanillaInput(vanillaInput);
    }

    /**
     * Convenience overload of {@link #getInputState(Input)} that requires the full {@link LocalPlayer},
     * which is needed to read the vanilla {@link Input} used for non-controller inputs.
     *
     * @param localPlayer the player whose vanilla {@link Input} will be read; ignored when using a controller.
     * @return an immutable {@link PlayerInputState} representing the current input state.
     */
    @NotNull
    public static PlayerInputState getInputState(@NotNull LocalPlayer localPlayer) {
        return getInputState(localPlayer.input);
    }

    /**
     * Updates the current input state for the current player (client-side).
     * <p>
     * You should use this instead of modifying fields in the vanilla {@link Input} directly
     * to ensure controller input is properly supported.
     * </p>
     *
     * @param inputState the updated input state.
     * @see InputManager#getInputState
     */
    public static void setInputState(@NotNull PlayerInputState inputState) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Input input = player.input;
            PlayerInputState.applyToVanillaInput(inputState, input);
        }
    }

    /**
     * Handles firing the {@link InputEvent.InteractionKeyMappingTriggered} input event for keyboard/mouse actions
     * and runs the callback only if the event is not canceled.
     * This method replaces the legacy internal {@link ControlEngine#isKeyPressed}.
     */
    @SuppressWarnings("JavadocReference")
    private static void runKeyboardMouseEvent(@NotNull EpicFightInputActions action, @NotNull DiscreteActionHandler handler) {
        final KeyMapping keyMapping = action.keyMapping();

        final InputConstants.Key key = keyMapping.getKey();
        final boolean isMouse = InputConstants.Type.MOUSE == key.getType();

        final int mouseButton = isMouse ? key.getValue() : -1;

        @SuppressWarnings("UnstableApiUsage")
        InputEvent.InteractionKeyMappingTriggered inputEvent = ForgeHooksClient.onClickInput(
                mouseButton, keyMapping, InteractionHand.MAIN_HAND
        );

        if (!inputEvent.isCanceled()) {
            handler.onAction(new DiscreteActionHandler.Context(false));
        }
    }

    /**
     * Checks whether the vanilla {@link KeyMapping} is down.
     * This internal method replaces the legacy {@link ControlEngine#isKeyDown}.
     */
    private static boolean isKeyDown(@NotNull KeyMapping keyMapping) {
        if (keyMapping.isDown()) {
            return true;
        }
        final InputConstants.Key key = keyMapping.getKey();
        final int keyValue = key.getValue();
        final long windowPointer = Minecraft.getInstance().getWindow().getWindow();

        if (key.getType() == InputConstants.Type.KEYSYM) {
            return GLFW.glfwGetKey(windowPointer, keyValue) > 0;
        } else if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(windowPointer, keyValue) > 0;
        }
        return false;
    }
}
