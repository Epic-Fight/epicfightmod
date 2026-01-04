package yesman.epicfight.platform.client;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;

public interface KeyMappingRegistrar {
    void registerKeyMapping(@NotNull final KeyMapping keyMapping);
}
