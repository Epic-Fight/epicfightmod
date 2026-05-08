package yesman.epicfight.api.ex_cap.provider;

/**
 * Defines the logic type and evaluation priority for weapon style providers.
 * <p>
 * The {@code priority} value determines which condition "wins" when multiple predicates 
 * return true. Higher values indicate higher priority, ensuring that specific overrides 
 * (like a specific item) take precedence over general defaults.
 * </p>
 */
public enum ProviderConditionalType {
    /** Checks if the item belongs to a specific {@link yesman.epicfight.world.capabilities.item.WeaponCategory}. */
    WEAPON_CATEGORY(1),

    /** Checks for a match against a specific {@link net.minecraft.world.item.Item}. High priority override. */
    SPECIFIC_WEAPON(2),

    /** Checks if a specific skill is currently present in the player's skill book. */
    SKILL_EXISTENCE(3),

    /** Checks if a specific skill is currently being actively used or held. */
    SKILL_ACTIVATION(4),

    /** Checks a boolean value within a skill's data map (e.g., toggles like "Sheathed"). */
    DATA_KEY(5),

    /** A container for multiple sub-conditionals; logic is handled by the children. */
    COMPOSITE(6),

    /** Allows for a custom {@link java.util.function.Predicate} based on the {@link yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch}. */
    CUSTOM(7),

    /** The baseline priority. Used when no other specialized conditions are met. */
    DEFAULT(0);

    private final int priority;

    ProviderConditionalType(int priority) {
        this.priority = priority;
    }

    /**
     * Returns the priority level of this condition type.
     * @return An integer where higher values represent higher evaluation precedence.
     */
    public int getPriority() {
        return priority;
    }
}