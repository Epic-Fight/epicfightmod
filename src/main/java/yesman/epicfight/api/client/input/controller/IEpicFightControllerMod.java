package yesman.epicfight.api.client.input.controller;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputActions;

/**
 * Represents an integration layer for third-party controller mods used by Epic Fight.
 * <p>
 * This interface must be implemented by any external controller mod to provide
 * controller input support for Epic Fight. It acts as a bridge between the
 * controller mod’s input system (e.g., Controlify, Controllable, MidnightControls) and Epic Fight’s
 * input handling logic.
 * <p>
 * Epic Fight relies on this interface to determine and manage the current
 * {@link InputMode}. Since input mode management is not part of the vanilla
 * Minecraft input system, controller mods must supply their own implementation.
 * <p>
 * <b>Note:</b> This interface exposes low-level controller integration. Most consumers should
 * use a higher-level abstraction unless direct access is necessary for functionality
 * that cannot be achieved otherwise.
 * <p>
 * <b>Warning:</b> This API is currently marked as experimental.
 * This designation does not imply that the implementation is of an 'experimental' quality,
 * but rather indicates that classes, methods, and fields may be subject to renaming, relocation, or removal.
 * The Epic Fight team reserves the right to modify or completely remove any components of the API at any time,
 * without prior notice.
 * <p>
 */
@ApiStatus.Experimental
public interface IEpicFightControllerMod {
    /**
     * Returns the controller mod’s display name (e.g., `Controlify`, `Controllable`).
     * Intended for logging or debugging only; should not influence gameplay logic or be used as a workaround.
     */
    String getModName();

    /**
     * Returns the current input mode (keyboard/mouse or controller).
     */
    @NotNull
    InputMode getInputMode();

    /**
     * Retrieves the universal controller binding for a given Epic Fight input action.
     * <p>
     * This method maps actions such as {@link EpicFightInputActions#JUMP},
     * {@link EpicFightInputActions#ATTACK}, and
     * {@link EpicFightInputActions#MOVE_FORWARD} to the corresponding controller
     * buttons or axes.
     * </p>
     *
     * @param action the Epic Fight input action to retrieve the binding for
     * @return the {@link ControllerBinding} associated with the specified action,
     * providing both state and metadata about the input
     * @see ControllerBinding
     */
    @NotNull
    ControllerBinding getBinding(EpicFightInputActions action);

    /**
     * Retrieves the current input state.
     * This is used internally by Epic Fight to perform actions such as {@link EpicFightInputActions#DODGE}.
     *
     * @return a {@link PlayerInputState} representing the current input state.
     */
    @NotNull
    PlayerInputState getInputState();

    /**
     * Checks whether the specified input actions are bound to the same controller button.
     * <p>
     * This comparison only applies to digital buttons (e.g., A, B, L3, R3) and does not
     * account for analogue inputs such as triggers or stick movements.
     * </p>
     *
     * @param action  the first input action
     * @param action2 the second input action
     * @return {@code true} if both actions are bound to the same controller button; {@code false} otherwise
     */
    boolean isBoundToSameButton(@NotNull EpicFightInputActions action, @NotNull EpicFightInputActions action2);
}
