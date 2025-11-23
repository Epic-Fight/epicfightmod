package yesman.epicfight.api.client.input.controller;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/// Represents a controller button or analogue stick axis.
/// Provides current and previous state, analogue/digital values, and metadata.
///
/// Serves a similar role to Minecraft's vanilla [net.minecraft.client.KeyMapping],
/// but for controller input instead of keyboard or mouse.
@ApiStatus.Experimental
public interface ControllerBinding {
    /// The ID of the binding (e.g., `epicfight:attack`).
    ///
    /// @return the ID
    @NotNull
    ResourceLocation id();

    /// Defines the type of input represented by a [ControllerBinding].
    ///
    /// Inputs can be either analogue or digital.
    ///
    /// Note: A single physical control can produce multiple input types.
    /// For example:
    ///
    /// - moving a left stick generates analogue signals (X/Y axes)
    /// - pressing the stick down generates a separate digital input
    enum InputType {
        /// Inputs with a continuous range of values, such as the movement axes
        /// of a stick or triggers (e.g., 0.0 to 1.0).
        ANALOGUE,
        /// Inputs that are binary,
        /// such as buttons or stick presses (e.g., L3/R3), which are either pressed or released.
        DIGITAL
    }

    /// Returns the type of this controller binding.
    ///
    /// @return the [InputType] of this binding
    @NotNull
    InputType getInputType();

    /// Returns whether this binding represents an analogue input.
    ///
    /// @return true if analogue, false if digital.
    /// @see InputType
    default boolean isAnalogueType() {
        return switch (getInputType()) {
            case ANALOGUE -> true;
            case DIGITAL -> false;
        };
    }

    /// Returns whether the digital state is currently active in this tick.
    ///
    /// This method is only applicable to digital inputs (e.g., buttons, pressing the stick down).
    ///
    /// @return the current digital state, this tick.
    /// @see InputType#DIGITAL
    boolean isDigitalActiveNow();

    /// Returns whether the digital state was active in the previous tick.
    ///
    /// This method is only applicable to digital inputs (e.g., buttons, pressing the stick down).
    ///
    /// @return the previous digital state, 1 tick ago.
    /// @see InputType#DIGITAL
    boolean wasDigitalActivePreviously();

    /// Returns whether the digital state was just pressed.
    ///
    /// This method is only applicable to digital inputs (e.g., buttons, pressing the stick down).
    ///
    /// @return true if the binding is pressed this tick and not pressed the previous tick.
    /// @see InputType#DIGITAL
    boolean isDigitalJustPressed();

    /// Returns whether the digital state was just released.
    ///
    /// This method is only applicable to digital inputs (e.g., buttons, pressing the stick down).
    ///
    /// @return true if the binding is not pressed this tick and pressed the previous tick.
    /// @see InputType#DIGITAL
    boolean isDigitalJustReleased();

    /// Returns the current analogue input value.
    ///
    /// This method is only applicable to analogue inputs (e.g., left stick, right trigger).
    /// For more details, refer to [InputType#ANALOGUE].
    ///
    /// @return the current analogue value in the range `0.0`–`1.0`, representing this tick's state.
    /// @see InputType#ANALOGUE
    float getAnalogueNow();

    /// Simulates a press of this binding.
    ///
    /// Can be used for GUI interactions or synthetic input from other systems.
    void emulatePress();
}
