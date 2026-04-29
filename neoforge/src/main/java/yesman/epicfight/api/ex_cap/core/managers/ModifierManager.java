package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.ex_cap.core.data.modifier.WeaponModifier;

import java.util.Map;

/**
 * Manager class responsible for the lifecycle of {@link WeaponModifier} instances.
 * <p>
 * This class handles the registration of static modifiers and manages their
 * application to items via the {@link ItemPresetManager}. It supports a
 * "hot-reload" style system where modifiers can be registered once, then
 * refreshed and re-applied when datapacks or game configurations update.
 * </p>
 * * @author EpicFight Contributor
 * @version 1.0
 */
public class ModifierManager
{
    /**
     * Internal cache of currently active modifiers.
     * Cleared and repopulated during {@link #refresh()}.
     */
    private static final Map<ResourceLocation, WeaponModifier> MODIFIERS = Maps.newHashMap();

    /**
     * The master registry of modifiers. Modifiers registered here persist
     * across refreshes.
     */
    private static final Map<ResourceLocation, WeaponModifier> REGISTERED_MODIFIERS = Maps.newHashMap();

    /**
     * Registers a new weapon modifier and builds it immediately.
     * <p><b>Note:</b> This is intended for internal initialization or
     * advanced addon registration.</p>
     * @param id The unique {@link ResourceLocation} for this modifier.
     * @param builder The builder containing the modifier's properties.
     * @return The constructed {@link WeaponModifier} instance.
     */
    public static WeaponModifier register(ResourceLocation id, WeaponModifier.Builder builder)
    {
        WeaponModifier modifier = build(id, builder);
        REGISTERED_MODIFIERS.put(id, modifier);
        return modifier;
    }

    /**
     * Synchronizes the active modifier cache with the master registry.
     * <p>
     * This method clears the current {@code MODIFIERS} map and clones all entries
     * from {@code REGISTERED_MODIFIERS}. This is typically called during
     * datapack reloading.
     * </p>
     */
    public static void refresh()
    {
        MODIFIERS.clear();
        MODIFIERS.putAll(REGISTERED_MODIFIERS);
    }

    /**
     * Executes the modification logic for all currently active modifiers.
     * <p>
     * Iterates through the active {@code MODIFIERS} and passes each to the
     * {@link ItemPresetManager#modify} method to apply changes to weapon presets.
     * </p>
     */
    public static void apply()
    {
        MODIFIERS.forEach((id, modifier) -> ItemPresetManager.modify(modifier));
    }

    /**
     * Internal helper to finalize a {@link WeaponModifier} from its builder.
     * <p>
     * Triggers {@link WeaponModifier.Builder#assemble()} to ensure
     * internal consistency (such as sorting conditionals by priority) before
     * instantiation.
     * </p>
     * * @param id The modifier ID.
     * @param builder The source builder.
     * @return A new immutable {@link WeaponModifier}.
     */
    private static WeaponModifier build(ResourceLocation id, WeaponModifier.Builder builder)
    {
        builder.assemble();
        return new WeaponModifier(id, builder.target, builder.conditionals, builder.moveSetModifier, builder.type);
    }
}