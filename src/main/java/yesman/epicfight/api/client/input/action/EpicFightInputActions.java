package yesman.epicfight.api.client.input.action;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.input.EpicFightKeyMappings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a default set of input actions used in the Epic Fight mod.
 * <p>
 * Each action is linked to a corresponding Minecraft vanilla {@link KeyMapping} or
 * an Epic Fight custom key mapping. These mappings only support keyboard and mouse input.
 * Controller input is not directly supported to avoid dependencies on third-party controller mods.
 * <p>
 * For implementation details, refer to {@link EpicFightInputActions#keyMapping()}.
 */
@ApiStatus.Experimental
public enum EpicFightInputActions implements InputAction {
    /**
     * Corresponds to Minecraft's default "Attack/Destroy" action.
     * <p>
     * This uses the vanilla {@link net.minecraft.client.Options#keyAttack} key mapping,
     * which is bound to the left mouse button by default. It handles both attacking entities
     * and breaking blocks in the base game.
     */
    VANILLA_ATTACK_DESTROY(true),
    USE(true),
    SWAP_OFF_HAND(true),
    DROP(true),
    TOGGLE_PERSPECTIVE(true),
    ATTACK(false),
    JUMP(true),
    MOBILITY(false),
    GUARD(false),
    DODGE(false),
    LOCK_ON(false),
    SWITCH_MODE(false),
    WEAPON_INNATE_SKILL(false),
    WEAPON_INNATE_SKILL_TOOLTIP(false),
    MOVE_FORWARD(true),
    MOVE_BACKWARD(true),
    MOVE_LEFT(true),
    MOVE_RIGHT(true),
    SPRINT(true),
    SNEAK(true),
    OPEN_SKILL_SCREEN(false),
    OPEN_CONFIG_SCREEN(false),
    SWITCH_VANILLA_MODEL_DEBUGGING(false);

    final private int id;

    /**
     * Indicates whether this input action corresponds to a standard
     * Minecraft vanilla key mapping.
     * <p>
     * Vanilla actions are those defined by the base game (e.g., attack, jump, move),
     * while non-vanilla actions are custom Epic Fight actions introduced by the mod.
     */
    final private boolean isVanilla;

    /**
     * Returns whether this input action corresponds to a vanilla
     * Minecraft key mapping.
     *
     * @return {@code true} if this action is linked to a vanilla key mapping,
     * {@code false} if it is defined by the Epic Fight mod.
     */
    public boolean isVanilla() {
        return isVanilla;
    }

    EpicFightInputActions(final boolean isVanilla) {
        this.id = InputAction.ENUM_MANAGER.assign(this);
        this.isVanilla = isVanilla;
    }

    @Override
    public int universalOrdinal() {
        return id;
    }

    @Override
    @NotNull
    public KeyMapping keyMapping() {
        final Options options = Minecraft.getInstance().options;
        return switch (this) {
            case VANILLA_ATTACK_DESTROY -> options.keyAttack;
            case USE -> options.keyUse;
            case SWAP_OFF_HAND -> options.keySwapOffhand;
            case DROP -> options.keyDrop;
            case TOGGLE_PERSPECTIVE -> options.keyTogglePerspective;
            case ATTACK -> EpicFightKeyMappings.ATTACK;
            case JUMP -> options.keyJump;
            case MOBILITY -> EpicFightKeyMappings.MOVER_SKILL;
            case GUARD -> EpicFightKeyMappings.GUARD;
            case DODGE -> EpicFightKeyMappings.DODGE;
            case LOCK_ON -> EpicFightKeyMappings.LOCK_ON;
            case SWITCH_MODE -> EpicFightKeyMappings.SWITCH_MODE;
            case WEAPON_INNATE_SKILL -> EpicFightKeyMappings.WEAPON_INNATE_SKILL;
            case WEAPON_INNATE_SKILL_TOOLTIP -> EpicFightKeyMappings.WEAPON_INNATE_SKILL_TOOLTIP;
            case MOVE_FORWARD -> options.keyUp;
            case MOVE_BACKWARD -> options.keyDown;
            case MOVE_LEFT -> options.keyLeft;
            case MOVE_RIGHT -> options.keyRight;
            case SPRINT -> options.keySprint;
            case SNEAK -> options.keyShift;
            case OPEN_SKILL_SCREEN -> EpicFightKeyMappings.SKILL_EDIT;
            case OPEN_CONFIG_SCREEN -> EpicFightKeyMappings.OPEN_CONFIG_SCREEN;
            case SWITCH_VANILLA_MODEL_DEBUGGING -> EpicFightKeyMappings.SWITCH_VANILLA_MODEL_DEBUGGING;
        };
    }

    private static final @NotNull Map<KeyMapping, EpicFightInputActions> BY_KEY_MAPPING;

    static {
        Map<KeyMapping, EpicFightInputActions> map = new HashMap<>();
        for (EpicFightInputActions action : values()) {
            map.put(action.keyMapping(), action);
        }
        BY_KEY_MAPPING = Collections.unmodifiableMap(map);
    }

    /**
     * Gets the input action corresponding to a {@link KeyMapping}.
     *
     * @param keyMapping the key mapping; must not be {@code null}
     * @return the corresponding action, or null if none matches
     */
    public static @Nullable EpicFightInputActions fromKeyMapping(@NotNull KeyMapping keyMapping) {
        return BY_KEY_MAPPING.get(keyMapping);
    }
}
