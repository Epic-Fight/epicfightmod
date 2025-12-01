package yesman.epicfight.client.input;

import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.generated.LangKeys;

@ApiStatus.Internal
public final class EpicFightInputCategories {
    private EpicFightInputCategories() {
    }

    public static final String COMBAT = LangKeys.KEY_COMBAT;
    public static final String GUI = LangKeys.KEY_GUI;
    public static final String SYSTEM = LangKeys.KEY_SYSTEM;
    public static final String CAMERA = LangKeys.KEY_CAMERA;
}
