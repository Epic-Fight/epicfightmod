package yesman.epicfight.api.ex_cap.modules.core.data;

import yesman.epicfight.api.ex_cap.data.Moveset;

/**
 * @deprecated left for legacy compatibility and to give time to migrate.
 */
@Deprecated(forRemoval = true)
public class MoveSet
{
    public static Moveset.Builder builder() {
        return new MoveSetBuilder();
    }

    public static class MoveSetBuilder extends Moveset.Builder
    {
    }
}
