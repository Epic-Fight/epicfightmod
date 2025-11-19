package yesman.epicfight.client.camera;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

import java.util.Objects;

public final class EpicFightTpsCameraDisableState {
    private EpicFightTpsCameraDisableState() {
    }

    private static @Nullable EpicFightTpsCameraDisabledReason reason = null;

    public static void disable(@NotNull EpicFightTpsCameraDisabledReason reason) {
        Objects.requireNonNull(reason, "reason must not be null");
        EpicFightTpsCameraDisableState.reason = reason;
        EpicFightMod.LOGGER.info("Epic Fight TPS mode has been disabled due to a mod conflict with {}", reason.getModName());
        ClientConfig.cameraMode = ClientConfig.TPSType.ALWAYS_BACK;
    }

    public static @Nullable EpicFightTpsCameraDisabledReason getReason() {
        return reason;
    }
}
