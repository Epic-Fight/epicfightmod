package yesman.epicfight.platform;

import org.jetbrains.annotations.NotNull;

/// Provides access to mod-loader specific functionalities that cannot be achieved using vanilla classes otherwise.
public interface ModPlatform {
    boolean isDevelopmentEnvironment();

    boolean isModLoaded(@NotNull final String id);
}
