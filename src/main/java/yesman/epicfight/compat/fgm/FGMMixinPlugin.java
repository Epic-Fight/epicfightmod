package yesman.epicfight.compat.fgm;

import org.jetbrains.annotations.NotNull;
import yesman.epicfight.compat.ModMixinPlugin;

public class FGMMixinPlugin extends ModMixinPlugin {
    @Override
    public @NotNull String getModId() {
        return "wildfire_gender";
    }
}
