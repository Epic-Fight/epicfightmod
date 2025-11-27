package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.ControllerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;

/// Allows Epic Fight to communicate with Controlify APIs without depending on their classes directly.
@ApiStatus.Internal
public class EpicFightControlifyControllerMod implements IEpicFightControllerMod {
    @Override
    public String getModName() {
        return "Controlify";
    }

    @Override
    public @NotNull InputMode getInputMode() {
        return switch (EpicFightControlifyEntrypoint.getApi().currentInputMode()) {
            case KEYBOARD_MOUSE -> InputMode.KEYBOARD_MOUSE;
            case CONTROLLER -> InputMode.CONTROLLER;
            case MIXED -> InputMode.MIXED;
        };
    }

    public static @NotNull ControllerBinding getBinding(@NotNull EpicFightInputAction action) {
        return new ControlifyControllerBinding(getControlifyBinding(action));
    }

    public static @NotNull InputBinding getControlifyBinding(@NotNull EpicFightInputAction action) {
        return EpicFightControlifyEntrypoint.getControlifyBinding(action);
    }

    @Override
    public @NotNull PlayerInputState getInputState() {
        ControllerEntity controller = EpicFightControlifyEntrypoint.requireControllerEntity();

        InputBinding forwardBind = ControlifyBindings.WALK_FORWARD.on(controller);
        InputBinding backwardBind = ControlifyBindings.WALK_BACKWARD.on(controller);
        InputBinding leftBind = ControlifyBindings.WALK_LEFT.on(controller);
        InputBinding rightBind = ControlifyBindings.WALK_RIGHT.on(controller);
        InputBinding jumpBind = ControlifyBindings.JUMP.on(controller);
        InputBinding sneakBind = ControlifyBindings.SNEAK.on(controller);

        float forwardImpulse = forwardBind.analogueNow() - backwardBind.analogueNow();
        float leftImpulse = leftBind.analogueNow() - rightBind.analogueNow();

        return new PlayerInputState(
                leftImpulse, forwardImpulse,
                forwardBind.digitalNow(), backwardBind.digitalNow(),
                leftBind.digitalNow(), rightBind.digitalNow(),
                jumpBind.digitalNow(), sneakBind.digitalNow()
        );
    }

    @Override
    public boolean isBoundToSamePhysicalInput(@NotNull EpicFightInputAction action, @NotNull EpicFightInputAction action2) {
        final Input input1 = getControlifyBinding(action).boundInput();
        final Input input2 = getControlifyBinding(action2).boundInput();
        return input1.getRelevantInputs().equals(input2.getRelevantInputs());
    }
}