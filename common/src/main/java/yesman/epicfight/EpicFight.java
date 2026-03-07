package yesman.epicfight;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.platform.ModPlatform;
import yesman.epicfight.platform.ModPlatformProvider;

/// 21.16.x
/// Introduced ExCap, with fallbacks to legacy methods
/// ExCap changes:
/// Introduces the ExCapData which houses all providers and movesets, essentially a little data packet.
/// Removed weapon default registration and now Epic Fight hooks into their own event to handle registering weapons
/// Builders are now static and are copied to form a new weapon, Innate skills are improved to use a BiFunction to avoid hardcoding innates.
/// RangedWeaponCapability now extends WeaponCapability and have full access to ExCap's things.
/// Added a new hook that registers ExCapData into a weapon which will then be applied.
/// Enhanced the tier system to allow bases and scaling multipliers.
///
/// Programmer's Note:
/// This was the greatest undertaking I could've done under the hood wth Epic Fight, if you thought Holdable Skills were cool,
/// ExCap was one of my first projects and was the first one of how I made Battle Arts. Completing this for ExCap would mean that ExCap would no longer be available on 1.21.1
///
/// Cheers to you, and everyone here.
///
/// Common functionalities shared between the platform entrypoints.
/// @author Forixaim, Ellet
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
