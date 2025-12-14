package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.generated.LangKeys;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EpicFightKeyMappings {

    // GUI key-mappings
    public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP =
            new KeyMapping(
                    LangKeys.KEY_SHOW_TOOLTIP,
                    KeyConflictContext.GUI,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_LSHIFT,
                    EpicFightInputCategories.GUI
            );

    public static final KeyMapping SKILL_EDIT =
            new KeyMapping(
                    LangKeys.KEY_SKILL_GUI,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_K,
                    EpicFightInputCategories.GUI
            );

    public static final KeyMapping OPEN_CONFIG_SCREEN =
            new KeyMapping(
                    LangKeys.KEY_CONFIG,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    -1,
                    EpicFightInputCategories.GUI
            );

    // In-game keymappings
    public static final KeyMapping DODGE =
            new CombatKeyMapping(
                    LangKeys.KEY_DODGE,
                    InputConstants.KEY_LALT,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping GUARD =
            new CombatKeyMapping(
                    LangKeys.KEY_GUARD,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_RIGHT,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping ATTACK =
            new CombatKeyMapping(
                    LangKeys.KEY_ATTACK,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping WEAPON_INNATE_SKILL =
            new CombatKeyMapping(
                    LangKeys.KEY_WEAPON_INNATE_SKILL,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping MOVER_SKILL =
            new CombatKeyMapping(
                    LangKeys.KEY_MOVER_SKILL,
                    InputConstants.KEY_SPACE,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping SWITCH_MODE =
            new KeyMapping(
                    LangKeys.KEY_SWITCH_MODE,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_R,
                    EpicFightInputCategories.COMBAT
            );

    public static final KeyMapping LOCK_ON =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_G,
                    EpicFightInputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_LEFT =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_LEFT,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_LEFT,
                    EpicFightInputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_RIGHT =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_RIGHT,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_RIGHT,
                    EpicFightInputCategories.CAMERA
            );

    public static final KeyMapping LOCK_ON_SHIFT_FREELY =
            new KeyMapping(
                    LangKeys.KEY_LOCK_ON_SHIFT_FREELY,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.MOUSE,
                    InputConstants.MOUSE_BUTTON_RIGHT,
                    EpicFightInputCategories.CAMERA
            );

    // Systemical key mappings especially for debugging
    public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING =
            new KeyMapping(
                    LangKeys.KEY_SWITCH_VANILLA_MODEL_DEBUG,
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    -1,
                    EpicFightInputCategories.SYSTEM
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
