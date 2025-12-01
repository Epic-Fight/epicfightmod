package yesman.epicfight.api.client.input;

import org.jetbrains.annotations.NotNull;

/// Functional interface for discrete input action callbacks.
@FunctionalInterface
public interface DiscreteActionHandler {
    /// Called when a discrete input action triggers.
    ///
    /// @param context The arguments of this handler
    void onAction(@NotNull Context context);

    /// @param triggeredByController true if the action was triggered by a controller, false if by keyboard/mouse
    record Context(boolean triggeredByController) {
    }
}
