package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.generated.LangKeys;

@EventBusSubscriber(value = Dist.CLIENT)
public class EpicFightKeyMappings {

    @ApiStatus.Internal
    public static class InputCategories {
        public static final String COMBAT = LangKeys.KEY_COMBAT;
        public static final String GUI = LangKeys.KEY_GUI;
        public static final String SYSTEM = LangKeys.KEY_SYSTEM;
        public static final String CAMERA = LangKeys.KEY_CAMERA;
    }

    // GUI key-mappings
    public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP =
            new KeyMapping(
                    LangKeys.KEY_SHOW_TOOLTIP,
                    KeyConflictContext.GUI,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_LSHIFT,
                    InputCategories.GUI
            );

    public static final KeyMapping SKILL_EDIT =
            new KeyMapping(
                    LangKeys.KEY_SKILL_GUI,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_K,
                    InputCategories.GUI
            );

    public static final KeyMapping OPEN_CONFIG_SCREEN =
            new KeyMapping(
                    LangKeys.KEY_CONFIG,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    -1,
                    InputCategories.GUI
            );

    // In-game keymappings
    public static final KeyMapping DODGE =
            new CombatKeyMapping(
                    LangKeys.KEY_DODGE,
                    InputConstants.KEY_LALT,
                    InputCategories.COMBAT
            );

    public static final KeyMapping GUARD =
            new CombatKeyMapping(
                    LangKeys.KEY_GUARD,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_RIGHT,
                    InputCategories.COMBAT
            );

    public static final KeyMapping ATTACK =
            new CombatKeyMapping(
                    LangKeys.KEY_ATTACK,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    InputCategories.COMBAT
            );

    public static final KeyMapping WEAPON_INNATE_SKILL =
            new CombatKeyMapping(
                    LangKeys.KEY_WEAPON_INNATE_SKILL,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    InputCategories.COMBAT
            );

    public static final KeyMapping MOVER_SKILL =
            new CombatKeyMapping(
                    LangKeys.KEY_MOVER_SKILL,
                    InputConstants.KEY_SPACE,
                    InputCategories.COMBAT
            );

    public static final KeyMapping SWITCH_MODE =
            new KeyMapping(
                    LangKeys.KEY_SWITCH_MODE,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_R,
                    InputCategories.COMBAT
            );

    public static final KeyMapping LOCK_ON =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_G,
                    InputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_LEFT =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_LEFT,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_LEFT,
                    InputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_RIGHT =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_RIGHT,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_RIGHT,
                    InputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_FREELY =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_FREELY,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_RIGHT,
                    InputCategories.CAMERA
            );

    // Systemical key mappings especially for debugging
    public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING =
            new KeyMapping(
                    LangKeys.KEY_SWITCH_VANILLA_MODEL_DEBUG,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    -1,
                    InputCategories.SYSTEM
            );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(WEAPON_INNATE_SKILL_TOOLTIP);
        event.register(SWITCH_MODE);
        event.register(DODGE);
        event.register(GUARD);
        event.register(ATTACK);
        event.register(WEAPON_INNATE_SKILL);
        event.register(MOVER_SKILL);
        event.register(SKILL_EDIT);
        event.register(LOCK_ON);
        event.register(LOCK_ON_SHIFT_LEFT);
        event.register(LOCK_ON_SHIFT_RIGHT);
        event.register(LOCK_ON_SHIFT_FREELY);
        event.register(OPEN_CONFIG_SCREEN);
        event.register(SWITCH_VANILLA_MODEL_DEBUGGING);
    }
}