package yesman.epicfight;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.platform.ModPlatform;
import yesman.epicfight.platform.ModPlatformProvider;

/// Common functionalities shared between the platform entrypoints.
public final class EpicFight {
    private EpicFight() {
    }

    public static final String MODID = "epicfight";
    public static final String EPICSKINS_MODID = "epicskins";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    /// Creates an identifier that points to an Epic Fight resource.
    ///
    /// This was called `identifier` and not `resourceLocation` since [Mojang renamed `ResourceLocation` to `Identifier` in 1.21.11](https://neoforged.net/news/21.11release/#renaming-of-resourcelocation-to-identifier).
    public static @NotNull ResourceLocation identifier(@NotNull final String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    /// Initializes common functionalities.
    ///
    /// Provides the global [ModPlatform] instance given by the mod platform (e.g., NeoForge, Fabric)
    /// to common vanilla code for sharing.
    public static void initialize(@NotNull final ModPlatform platform) {
        ModPlatformProvider.initialize(platform);
    }
}
