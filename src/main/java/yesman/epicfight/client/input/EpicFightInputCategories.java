package yesman.epicfight.client.input;

import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.main.EpicFightMod;

@ApiStatus.Internal
public final class EpicFightInputCategories {
    private EpicFightInputCategories() {
    }

    public static final String COMBAT = EpicFightMod.format("key.%s.combat");
    public static final String GUI = EpicFightMod.format("key.%s.gui");
    public static final String SYSTEM = EpicFightMod.format("key.%s.system");
    public static final String CAMERA = EpicFightMod.format("key.%s.camera");
}
