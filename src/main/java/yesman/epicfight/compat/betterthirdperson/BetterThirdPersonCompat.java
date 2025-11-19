package yesman.epicfight.compat.betterthirdperson;

import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisableState;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisabledReason;
import yesman.epicfight.compat.ICompatModule;

// Disables the Epic Fight's TPS perspective when this mod is installed,
// otherwise, both mods will make modifications to the vanilla third-person back perspective,
// which results in buggy behavior.
// Note: This does not support the "Better Third Person" mod,
// features like dodge, attack, and lock-on may not work with Epic Fight.
public final class BetterThirdPersonCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {

    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {
        EpicFightTpsCameraDisableState.disable(EpicFightTpsCameraDisabledReason.BetterThirdPerson);
    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {

    }
}
