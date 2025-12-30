package yesman.epicfight.api.utils.side;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

/// A utility enum that checks entities' and levels' logical side
public enum LogicalSide {
    CLIENT(Level::isClientSide, entity -> entity.level().isClientSide()),
    SERVER(level -> !level.isClientSide(), entity -> !entity.level().isClientSide()),
    BOTH(level -> true, entity -> true);

    final Predicate<Level> isLevelValidSide;
    final Predicate<Entity> isEntityValidSide;

    LogicalSide(Predicate<Level> isLevelValidSide, Predicate<Entity> isEntityValidSide) {
        this.isLevelValidSide = isLevelValidSide;
        this.isEntityValidSide = isEntityValidSide;
    }

    /// Returns if the given level is on the right side
    public boolean isLevelOnValidSide(Level level) {
        return this.isLevelValidSide.test(level);
    }

    /// Returns if the given entity is on the right side
    public boolean isEntityOnValidSide(Entity entity) {
        return this.isEntityValidSide.test(entity);
    }
}
