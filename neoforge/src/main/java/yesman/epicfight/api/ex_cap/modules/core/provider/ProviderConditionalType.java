package yesman.epicfight.api.ex_cap.modules.core.provider;

public enum ProviderConditionalType
{
    WEAPON_CATEGORY(1),
    SPECIFIC_WEAPON(2),
    SKILL_EXISTENCE(3),
    SKILL_ACTIVATION(4),
    DATA_KEY(5),
    COMPOSITE(6),
    CUSTOM(7),
    DEFAULT(0);

    private final int priority;

    ProviderConditionalType(int priority)
    {
        this.priority = priority;
    }

    public int getPriority()
    {
        return priority;
    }
}
