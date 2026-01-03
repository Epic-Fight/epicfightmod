package yesman.epicfight.platform.fabric;

import net.fabricmc.api.ModInitializer;
import yesman.epicfight.EpicFight;

public final class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EpicFight.initialize(new FabricModPlatform());
    }
}
