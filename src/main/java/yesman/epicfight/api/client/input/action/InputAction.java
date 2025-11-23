package yesman.epicfight.api.client.input.action;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

/// Represents a client-side input action in Epic Fight mod.
///
/// Each action is associated with a Minecraft vanilla [KeyMapping], which
/// only supports keyboard and mouse input.
///
/// Controller input is not directly supported to avoid depending on third-party controller mods.
@ApiStatus.Experimental
public interface InputAction extends ExtendableEnum {
    ExtendableEnumManager<InputAction> ENUM_MANAGER = new ExtendableEnumManager<>("input_action");

    /// Returns the Minecraft vanilla [KeyMapping] associated with this action.
    ///
    /// Note: This mapping only supports keyboard and mouse input and does not support controllers.
    /// Consumers should consider using a different API when possible to take advantage of
    /// the current supported controller mod implementation.
    ///
    /// **Important:** This method must be called **only on the client**.
    ///
    /// @return the vanilla [KeyMapping] for this action
    @NotNull
    KeyMapping keyMapping();
}
