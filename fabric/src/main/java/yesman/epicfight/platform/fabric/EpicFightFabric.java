package yesman.epicfight.platform.fabric;

import net.fabricmc.api.ModInitializer;
import yesman.epicfight.EpicFight;

/// Epic Fight doesn't support Fabric for Minecraft 1.21.1.
/// This sample project is only for demonstrating the multi-loader structure
/// and to prepare for future-proofing Epic Fight project to support Fabric.
public final class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EpicFight.initialize(new FabricModPlatform());
    }
}
