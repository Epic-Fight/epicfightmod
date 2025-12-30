package yesman.epicfight.api.client.input;

/// Represents the type of input mode a player can use in the game.
public enum InputMode {
    /// Input is limited to the keyboard and mouse.
    KEYBOARD_MOUSE,

    /// Input is limited to a controller or gamepad.
    CONTROLLER,

    /// Input from both the keyboard and mouse, and a controller or gamepad.
    MIXED;

    /// Returns whether this mode supports keyboard and mouse input.
    ///
    /// @return `true` if keyboard and mouse input is supported.
    public boolean supportsKeyboardAndMouse() {
        return switch (this) {
            case KEYBOARD_MOUSE, MIXED -> true;
            case CONTROLLER -> false;
        };
    }

    /// Returns whether this mode supports controller or gamepad input.
    ///
    /// @return `true` if controller input is supported.
    public boolean supportsController() {
        return switch (this) {
            case CONTROLLER, MIXED -> true;
            case KEYBOARD_MOUSE -> false;
        };
    }
}
