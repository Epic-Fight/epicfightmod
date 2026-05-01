package yesman.epicfight.client.camera;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.main.EpicFightMod;

import java.util.Objects;

public final class EpicFightTpsCameraDisableState {
    private EpicFightTpsCameraDisableState() {
    }

    private static @Nullable EpicFightTpsCameraDisabledReason reason = null;
    private static boolean eventRegistered = false;

    public static void disable(@NotNull EpicFightTpsCameraDisabledReason reason) {
        Objects.requireNonNull(reason, "reason must not be null");

        EpicFightTpsCameraDisableState.reason = reason;
        EpicFightMod.LOGGER.info("Epic Fight TPS mode has been disabled due to a mod conflict with {}", reason.getModName());

        if (!eventRegistered) {
            EpicFightClientEventHooks.Camera.ACTIVATE_TPS_CAMERA.registerEvent(e -> {
                if (EpicFightTpsCameraDisableState.reason != null) {
                    e.cancel();
                }
            });
            eventRegistered = true;
        }
    }

    public static @Nullable EpicFightTpsCameraDisabledReason getReason() {
        return reason;
    }
}
