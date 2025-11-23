package yesman.epicfight.world.entity.eventlistener;

import net.minecraft.client.player.Input;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

public class MovementInputEvent extends AbstractPlayerEvent<LocalPlayerPatch> {
    /**
     * <b>DEPRECATED:</b>: This field is kept for backward compatibility with existing Epic Fight addons.
     * <p>
     * Consumers should migrate to using {@link #getInputState()} and
     * {@link InputManager#setInputState} instead, which fully support controller input.
     * </p>
     *
     * @see MovementInputEvent#getInputState
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @NotNull
    @Deprecated
    private final Input movementInput;

    // This was set as @Nullable to introduce PlayerInputState in a backward compatible way.
    // As soon as we introduce a breaking change by removing the existing constructor that sets movementInput, we
    // should set this to @NotNull.
    @Nullable
    private final PlayerInputState inputState;

    /**
     * <b>DEPRECATED:</b>: Please use the new constructor that accepts {@link PlayerInputState} to support controllers.
     * <p>
     * This constructor is only kept for backward compatibility with addons that rely on vanilla {@link Input}.
     * </p>
     *
     * @see InputManager
     */
    @Deprecated
    @ApiStatus.Internal
    public MovementInputEvent(LocalPlayerPatch playerPatch, @NotNull Input input) {
        super(playerPatch, false);
        this.movementInput = input;
        this.inputState = null;
    }

    /**
     * Creates a new {@link MovementInputEvent} with an immutable {@link PlayerInputState}.
     * <p>
     * Use this constructor to fully support controllers.
     * </p>
     *
     * @param playerPatch the patched local player
     * @param inputState  the current input state
     */
    @ApiStatus.Internal
    public MovementInputEvent(LocalPlayerPatch playerPatch, @NotNull PlayerInputState inputState) {
        super(playerPatch, false);
        this.inputState = inputState;
        // DEPRECATED: Still set the vanilla Input for backward compatibility to avoid Epic Fight addons breakage.
        // Not setting this, may break any consumers that depend on the deprecated MovementInputEvent#getMovementInput() method.
        this.movementInput = playerPatch.getOriginal().input;
    }

    /**
     * <b>DEPRECATED:</b>: Use {@link #getInputState()} instead to support controllers.
     * <p>
     * Note that {@link PlayerInputState} is immutable. You cannot directly modify the fields
     * like in vanilla {@link Input}. To apply changes, use {@link InputManager#setInputState}.
     * </p>
     *
     * @return the vanilla input instance (deprecated)
     * @see InputManager#setInputState 
     */
    @Deprecated
    public @NotNull Input getMovementInput() {
        return this.movementInput;
    }

    /**
     * Returns the current input state for the player.
     * <p>
     * This method abstracts over vanilla {@link Input} and controller input, providing a unified,
     * immutable {@link PlayerInputState}.
     * </p>
     *
     * @return the current player input state
     */
    @NotNull
    public PlayerInputState getInputState() {
        if (inputState == null) {
            return InputManager.getInputState(movementInput);
        }
        return inputState;
    }

    /**
     * Currently, this calls {@link Input#tick} without performing any additional logic.
     * This abstraction was introduced to allow calling it without depending on the vanilla Minecraft {@link Input},
     * enabling Epic Fight to introduce changes in future updates if necessary to support controllers.
     */
    @ApiStatus.Experimental
    public void sneakingTick(boolean isSneaking, float sneakingSpeedMultiplier) {
        InputUtils.sneakingTick(isSneaking, sneakingSpeedMultiplier);
    }
}