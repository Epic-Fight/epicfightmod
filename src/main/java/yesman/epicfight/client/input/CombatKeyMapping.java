package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.client.ClientEngine;

/**
 * A specialized {@link KeyMapping} used by Epic Fight to represent combat-related key bindings.
 * <p>
 * This enforces that all {@link #isDown()} or related checks to return {@code false}
 * whenever the player is <strong>not</strong> in Epic Fight mode.
 * <p>
 * <strong>Important:</strong> Other mods or consumers should <em>not</em> rely on this behavior.
 * They should explicitly check whether the player is in Epic Fight mode through
 * {@link yesman.epicfight.client.ClientEngine#isEpicFightMode()} instead of depending on
 * this key mapping’s conditional logic.
 * <p>
 * This also force setting {@link KeyConflictContext#IN_GAME},
 * since a {@link CombatKeyMapping} is usually used for player moves
 * (e.g, dodge, guard, mover skill, epic fight attack).
 * <p>
 * Note: The author of this code is not entirely certain about the exact purpose of
 * {@link KeyConflictContext#IN_GAME}.
 * It appears mainly relevant to key modifiers
 * (e.g., distinguishing Shift + E from E) and does not affect the red conflict highlighting
 * in the key bindings menu, since vanilla key mappings do not assign any {@link KeyConflictContext}.
 * <p>
 * This class is primarily used as a fallback or metadata reference for compatibility with
 * other mods (hopefully!).
 * Otherwise, it has no meaningful function beyond normal {@link KeyMapping} behavior.
 * <p>
 * Future maintainers should consider refactoring or removing this class
 * if it becomes problematic or a maintenance burden.
 * <p>
 */
@OnlyIn(Dist.CLIENT)
public class CombatKeyMapping extends KeyMapping {
    public CombatKeyMapping(String description, int code, String category) {
        this(description, InputConstants.Type.KEYSYM, code, category);
    }

    /**
     * This key mapping only applies {@link KeyConflictContext#IN_GAME} since it represents player moves
     */
	public CombatKeyMapping(String description, InputConstants.Type type, int code, String category) {
		super(description, KeyConflictContext.IN_GAME, type, code, category);
	}
	
	@Override
	public boolean isActiveAndMatches(@NotNull InputConstants.Key keyCode) {
        return super.isActiveAndMatches(keyCode) && ClientEngine.getInstance().isEpicFightMode();
    }
}