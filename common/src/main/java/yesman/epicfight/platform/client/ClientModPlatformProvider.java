package yesman.epicfight.platform.client;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides access to the universal [ClientModPlatform] instance.
///
/// Mod platforms (e.g., NeoForge or Fabric) are expected to call [#initialize()].
public final class ClientModPlatformProvider {
    private ClientModPlatformProvider() {
    }

    private static @Nullable ClientModPlatform INSTANCE = null;

    @ApiStatus.Internal
    public static void initialize(@NotNull final ClientModPlatform platform) {
        Objects.requireNonNull(platform, "The platform argument cannot be null.");
        if (INSTANCE != null) {
            throw new IllegalStateException("A client mod platform implementation can be provided only once.");
        }
        INSTANCE = platform;
    }

    public static @NotNull ClientModPlatform get() {
        return Objects.requireNonNull(INSTANCE, "A client mod platform implementation has not been provided.");
    }
}
