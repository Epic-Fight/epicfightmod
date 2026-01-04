package yesman.epicfight.main;

import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.EpicFight;
import yesman.epicfight.generated.LangKeys;

/// @deprecated Use [EpicFight] or [EpicFightNeoForge] instead. Exists for backward compatibility and serves as documentation.
@Deprecated(forRemoval = true)
public class EpicFightMod {
    /// @deprecated Use [yesman.epicfight.EpicFight#identifier(String)] instead
    @Deprecated(forRemoval = true)
    public static @NotNull ResourceLocation identifier(@NotNull String path) {
        return EpicFight.identifier(path);
    }

    /// @deprecated Use [#identifier(String)] instead. [Mojang renamed `ResourceLocation` to `Identifier` in 1.21.11](https://neoforged.net/news/21.11release/#renaming-of-resourcelocation-to-identifier).
    @Deprecated(forRemoval = true)
    public static @NotNull ResourceLocation rl(@NotNull String path) {
        return EpicFight.identifier(path);
    }

    /// @deprecated Use [yesman.epicfight.EpicFight#MODID] instead
    @Deprecated(forRemoval = true)
    public static final String MODID = EpicFight.MODID;

    /// @deprecated Use [yesman.epicfight.EpicFight#EPICSKINS_MODID] instead
    @Deprecated(forRemoval = true)
    public static final String EPICSKINS_MODID = EpicFight.EPICSKINS_MODID;

    /// @deprecated Use [yesman.epicfight.EpicFight#LOGGER] instead
    @Deprecated(forRemoval = true)
    public static final Logger LOGGER = EpicFight.LOGGER;

    /// @deprecated Consider using the generated object [LangKeys],
    /// which is type-safe and not error-prone to runtime bugs or crashes.
    @Deprecated(forRemoval = true)
    public static String format(String s) {
        return String.format(s, EpicFight.MODID);
    }
}
