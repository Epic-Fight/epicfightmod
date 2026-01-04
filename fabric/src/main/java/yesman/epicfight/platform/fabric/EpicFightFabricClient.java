package yesman.epicfight.platform.fabric;

import net.fabricmc.api.ClientModInitializer;
import yesman.epicfight.EpicFightClient;
import yesman.epicfight.platform.fabric.client.FabricClientModPlatform;

public final class EpicFightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());
    }
}
