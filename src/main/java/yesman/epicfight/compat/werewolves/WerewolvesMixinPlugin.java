package yesman.epicfight.compat.werewolves;

import org.jetbrains.annotations.NotNull;
import yesman.epicfight.compat.ModMixinPlugin;

public final class WerewolvesMixinPlugin extends ModMixinPlugin {
    @Override
    public @NotNull String getModId() {
        return "werewolves";
    }
}
