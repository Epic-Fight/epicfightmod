package yesman.epicfight.api.client.input.action;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;
import yesman.epicfight.api.client.input.controller.ControllerBinding;

import java.util.Optional;

/// Represents a client-side input action in Epic Fight mod.
///
/// Each action is associated with:
///
/// - a Minecraft vanilla [KeyMapping] (supports keyboard and mouse only).
/// - a [ControllerBinding], which is an abstraction around the input binding from
///  third-party controller mods (supports controller only).
///
/// **Important:** This class must be called **only on the client**.
@ApiStatus.Experimental
public interface InputAction extends ExtendableEnum {
    ExtendableEnumManager<InputAction> ENUM_MANAGER = new ExtendableEnumManager<>("input_action");

    /// Returns the Minecraft vanilla [KeyMapping] associated with this action.
    ///
    /// **Note:** This only supports keyboard and mouse input and does not support controllers.
    ///
    /// @return the vanilla [KeyMapping] for this action
    /// @see #controllerBinding
    @NotNull
    KeyMapping keyMapping();

    /// Returns the universal controller binding associated with this action, if available.
    ///
    /// This method may return [Optional#empty()] if the action does not support controller input.
    ///
    /// **Important:** Consumers must **not** call this method if the controller mod is not installed,
    /// since the creation of a [ControllerBinding] requires depending on APIs from the controller mod.
    ///
    /// If this was called and the controller mod was not installed, the behavior is undefined and depends
    /// on the implementation details.
    /// Usually a [ClassNotFoundException] or [IllegalStateException] is thrown.
    ///
    /// @return the [ControllerBinding] for this action, or [Optional#empty()] if not supported
    /// @see ControllerBinding
    @NotNull Optional<@NotNull ControllerBinding> controllerBinding();
}
