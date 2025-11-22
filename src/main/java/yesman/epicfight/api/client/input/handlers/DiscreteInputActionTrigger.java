package yesman.epicfight.api.client.input.handlers;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;

/**
 * Handles triggering of a discrete (one-time) {@link EpicFightInputAction}
 * based on the current input state.
 * <p>
 * Consumers of this API provide only the "what to do" for each action;
 * this class determines the "when" to trigger it.
 * <p>
 * Internally, it supports both vanilla keyboard/mouse input and third-party controllers.
 * <p>
 * <b>Note:</b> This is an internal API. Consumers should prefer using higher-level components
 * such as {@link InputManager} unless direct access is truly required.
 */
@ApiStatus.Internal
public final class DiscreteInputActionTrigger {
    private DiscreteInputActionTrigger() {
    }

    @Nullable
    private static IEpicFightControllerMod getControllerModApi() {
        return EpicFightControllerModProvider.get();
    }

    /**
     * Called on every client tick to potentially trigger the provided callback for a given input action.
     * <p>
     * Determines *when* to trigger the action; consumers define *how* it executes.
     * For example, for {@link EpicFightInputAction#OPEN_SKILL_SCREEN}, this method decides when to call
     * the callback that opens the screen, but not how the screen is opened.
     * <p>
     * Consumers do not need to know any keyboard/mouse or controller input internals.
     *
     * @param action  The input action to monitor.
     * @param handler The callback to run when the action triggers.
     */
    public static void triggerOnPress(EpicFightInputAction action, DiscreteActionHandler handler) {
        final IEpicFightControllerMod controllerMod = getControllerModApi();
        final KeyMapping keyMapping = action.keyMapping();
        if (controllerMod == null) {
            handleKeyboardAndMouse(keyMapping, handler);
            return;
        }

        switch (controllerMod.getInputMode()) {
            case MIXED -> {
                final boolean handled = handleController(controllerMod.getBinding(action), handler);
                if (handled) {
                    return;
                }
                handleKeyboardAndMouse(keyMapping, handler);
            }
            case CONTROLLER -> handleController(controllerMod.getBinding(action), handler);
            case KEYBOARD_MOUSE -> handleKeyboardAndMouse(keyMapping, handler);
        }
    }

    private static void handleKeyboardAndMouse(KeyMapping keyMapping, DiscreteActionHandler handler) {
        while (keyMapping.consumeClick()) {
            handler.onAction(createContext(false));
        }
    }

    private static boolean handleController(ControllerBinding controllerBinding, DiscreteActionHandler handler) {
        if (controllerBinding.isDigitalJustPressed()) {
            handler.onAction(createContext(true));
            return true;
        }
        return false;
    }
    
    @NotNull
    private static DiscreteActionHandler.Context createContext(boolean triggeredByController) {
        return new DiscreteActionHandler.Context(triggeredByController);
    }
}
