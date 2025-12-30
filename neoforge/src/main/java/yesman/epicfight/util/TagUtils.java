package yesman.epicfight.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.function.Supplier;

public final class TagUtils {
    public static <T extends Tag> T getOrCreateTag(CompoundTag root, String key, Supplier<T> tagProvider) {
        if (root.contains(key)) {
            return (T)root.get(key);
        }

        T tag = tagProvider.get();
        root.put(key, tag);
        return tag;
    }

    private TagUtils() {
    }
}
