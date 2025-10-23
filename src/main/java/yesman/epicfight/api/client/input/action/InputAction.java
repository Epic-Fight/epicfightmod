package yesman.epicfight.api.client.input.action;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

/**
 * Represents a client-side input action in Epic Fight mod.
 * <p>
 * Each action is associated with a Minecraft vanilla {@link KeyMapping}, which
 * only supports keyboard and mouse input. Controller input is not directly
 * supported to avoid depending on third-party controller mods.
 */
@OnlyIn(Dist.CLIENT)
@ApiStatus.Experimental
public interface InputAction extends ExtendableEnum {
    ExtendableEnumManager<InputAction> ENUM_MANAGER = new ExtendableEnumManager<>("input_action");

    /**
     * Returns the Minecraft vanilla {@link KeyMapping} associated with this action.
     * <p>
     * Note: This mapping only supports keyboard and mouse input and does not support controllers.
     * Consumers should consider using a different API when possible to take advantage of
     * the current supported controller mod implementation.
     * <p>
     * <b>Important:</b> This method must be called <b>only on the client</b>.
     *
     * @return the vanilla {@link KeyMapping} for this action
     */
    @NotNull
    @OnlyIn(Dist.CLIENT)
    KeyMapping keyMapping();
}
