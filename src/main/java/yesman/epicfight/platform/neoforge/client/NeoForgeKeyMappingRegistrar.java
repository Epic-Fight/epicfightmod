package yesman.epicfight.platform.neoforge.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.platform.client.KeyMappingRegistrar;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeKeyMappingRegistrar implements KeyMappingRegistrar {
    private final List<@NotNull KeyMapping> pending = new ArrayList<>();
    private boolean locked = false;

    @Override
    public void registerKeyMapping(@NotNull final KeyMapping keyMapping) {
        if (locked) {
            throw new IllegalStateException("Cannot register key mappings after the 'RegisterKeyMappingsEvent' has fired. " +
                    "All key mappings must be registered during mod setup, before the event is processed.");
        }
        pending.add(keyMapping);
    }

    @ApiStatus.Internal
    public void onRegisterKeys(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyMapping : pending) {
            event.register(keyMapping);
        }
        locked = true;
        pending.clear();
    }
}
