package yesman.epicfight.api.client.input.action;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.compat.controlify.EpicFightControlifyControllerMod;

import java.util.Optional;

public enum MinecraftInputAction implements InputAction {
    JUMP,
    /// Corresponds to Minecraft's default "Attack/Destroy" action.
    ///
    /// This uses the vanilla [net.minecraft.client.Options#keyAttack] key mapping,
    /// which is bound to the left mouse button by default.
    /// It handles both attacking entities and breaking blocks in the base game.
    ATTACK_DESTROY,
    USE,
    SWAP_OFF_HAND,
    DROP,
    TOGGLE_PERSPECTIVE,
    MOVE_FORWARD,
    MOVE_BACKWARD,
    MOVE_LEFT,
    MOVE_RIGHT,
    SPRINT,
    SNEAK;

    final private int id;

    MinecraftInputAction() {
        this.id = InputAction.ENUM_MANAGER.assign(this);
    }

    @Override
    @NotNull
    public KeyMapping keyMapping() {
        final Options options = Minecraft.getInstance().options;
        return switch (this) {
            case USE -> options.keyUse;
            case ATTACK_DESTROY -> options.keyAttack;
            case SWAP_OFF_HAND -> options.keySwapOffhand;
            case DROP -> options.keyDrop;
            case TOGGLE_PERSPECTIVE -> options.keyTogglePerspective;
            case JUMP -> options.keyJump;
            case MOVE_FORWARD -> options.keyUp;
            case MOVE_BACKWARD -> options.keyDown;
            case MOVE_LEFT -> options.keyLeft;
            case MOVE_RIGHT -> options.keyRight;
            case SPRINT -> options.keySprint;
            case SNEAK -> options.keyShift;
        };
    }

    @Override
    @NotNull
    public Optional<@NotNull ControllerBinding> controllerBinding() {
        if (EpicFightControllerModProvider.get() == null) {
            throw new IllegalStateException("controllerBinding() must not be called when the controller mod is not installed");
        }
        return Optional.of(EpicFightControlifyControllerMod.getBinding(this));
    }

    @Override
    public boolean isVanilla() {
        return true;
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }
}
