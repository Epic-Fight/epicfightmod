package yesman.epicfight.client.camera;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates why Epic Fight's built-in third-person camera was disabled.
 * <p>
 * Epic Fight automatically turns off its own third-person mode when a
 * compatible third-party camera mod is detected.
 */
public enum EpicFightTpsCameraDisabledReason {
    ShoulderSurfing("Shoulder Surfing"),
    BetterThirdPerson("Better Third Person");

    final private @NotNull String modName;

    EpicFightTpsCameraDisabledReason(@NotNull String modName) {
        this.modName = modName;
    }

    public @NotNull String getModName() {
        return modName;
    }
}
