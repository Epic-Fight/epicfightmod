package yesman.epicfight.platform.neoforge.client;

import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.platform.client.ClientModPlatform;
import yesman.epicfight.platform.client.KeyMappingRegistrar;

public final class NeoForgeClientModPlatform implements ClientModPlatform {

    public NeoForgeClientModPlatform(IEventBus modEventBus) {
        modEventBus.addListener(keyMappingRegistrar::onRegisterKeys);
    }

    private final NeoForgeKeyMappingRegistrar keyMappingRegistrar = new NeoForgeKeyMappingRegistrar();

    @Override
    public @NotNull KeyMappingRegistrar keyMappingRegistrar() {
        return keyMappingRegistrar;
    }
}
