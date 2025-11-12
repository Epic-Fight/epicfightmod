package yesman.epicfight.compat.kubejs;

import dev.latvian.mods.kubejs.util.ConsoleJS;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CallbackUtils {
    public static <T> boolean safeCallback(Consumer<T> consumer, T value, String errorMessage) {
        try {
            consumer.accept(value);
        } catch (Throwable e) {
            ConsoleJS.STARTUP.error(errorMessage, e);
            return false;
        }
        return true;
    }

    public static <T, U> boolean biSafeCallback(BiConsumer<T, U> consumer, T value, U otherValue, String errorMessage) {
        try {
            consumer.accept(value, otherValue);
        } catch (Throwable e) {
            ConsoleJS.STARTUP.error(errorMessage, e);
            return false;
        }
        return true;
    }
}
