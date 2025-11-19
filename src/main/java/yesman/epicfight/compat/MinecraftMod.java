package yesman.epicfight.compat;

import org.jetbrains.annotations.NotNull;
import yesman.epicfight.compat.azurelib.AzureLibArmorCompat;
import yesman.epicfight.compat.azurelib.AzureLibCompat;
import yesman.epicfight.compat.betterthirdperson.BetterThirdPersonCompat;
import yesman.epicfight.compat.curiosapi.CuriosCompat;
import yesman.epicfight.compat.firstperson.FirstPersonCompat;
import yesman.epicfight.compat.geckolib.GeckolibCompat;
import yesman.epicfight.compat.iris.IRISCompat;
import yesman.epicfight.compat.playeranimator.PlayerAnimatorCompat;
import yesman.epicfight.compat.skinlayer3d.SkinLayer3DCompat;
import yesman.epicfight.compat.vampirism.VampirismCompat;
import yesman.epicfight.compat.werewolves.WerewolvesCompat;

// List of mods with custom compatibility modules.
// Only includes mods requiring manual registration via ICompatModule.
// Mods with official API entry-points (e.g., Shoulder Surfing, Controlify, JEI, KubeJS) are excluded.
public enum MinecraftMod {
    VAMPIRISM("vampirism", false, VampirismCompat.class),
    WEREWOLVES("werewolves", false, WerewolvesCompat.class),
    CURIOS_API("curios", false, CuriosCompat.class),
    GECKO_LIB("geckolib", true, GeckolibCompat.class),
    AZURE_LIB("azurelib", true, AzureLibCompat.class),
    AZURE_LIB_ARMOR("azurelibarmor", true, AzureLibArmorCompat.class),
    FIRST_PERSON("firstperson", true, FirstPersonCompat.class),
    SKIN_LAYERS_3D("skinlayers3d", true, SkinLayer3DCompat.class),
    IRIS("iris", true, IRISCompat.class),
    PLAYER_ANIMATOR("playeranimator", true, PlayerAnimatorCompat.class),
    BETTER_THIRD_PERSON("betterthirdperson", true, BetterThirdPersonCompat.class),
    ;

    private final @NotNull String modId;
    private final boolean isClientOnly;
    private final @NotNull Class<? extends ICompatModule> compatibilityModule;

    MinecraftMod(@NotNull String modId, boolean isClientOnly, @NotNull Class<? extends ICompatModule> compatibilityModule) {
        this.modId = modId;
        this.isClientOnly = isClientOnly;
        this.compatibilityModule = compatibilityModule;
    }

    public @NotNull String getModId() {
        return modId;
    }

    public boolean isClientOnly() {
        return isClientOnly;
    }

    public @NotNull Class<? extends ICompatModule> getCompatibilityModule() {
        return compatibilityModule;
    }
}
