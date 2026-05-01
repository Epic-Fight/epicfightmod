package yesman.epicfight.platform;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Provides access to the universal [ModPlatform] instance.
///
/// Mod platforms (e.g., NeoForge or Fabric) are expected to call [#initialize()].
public final class ModPlatformProvider {
    private ModPlatformProvider() {
    }

    private static @Nullable ModPlatform INSTANCE = null;

    @ApiStatus.Internal
    public static void initialize(@NotNull final ModPlatform platform) {
        Objects.requireNonNull(platform, "The platform argument cannot be null.");
        if (INSTANCE != null) {
            throw new IllegalStateException("A mod platform implementation can be provided only once.");
        }
        INSTANCE = platform;
    }

    public static @NotNull ModPlatform get() {
        return Objects.requireNonNull(INSTANCE, "A mod platform implementation has not been provided.");
    }
}
