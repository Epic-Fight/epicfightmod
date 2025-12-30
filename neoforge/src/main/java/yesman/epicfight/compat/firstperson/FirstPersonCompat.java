package yesman.epicfight.compat.firstperson;

import dev.tr7zw.firstperson.api.ActivationHandler;
import dev.tr7zw.firstperson.api.FirstPersonAPI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class FirstPersonCompat implements ICompatModule {
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> event.enqueueWork(() -> {
			FirstPersonAPI.getActivationHandlers().add(new ActivationHandler() {
				public boolean preventFirstperson() {
					PlayerPatch<?> playerpatch = EpicFightCapabilities.getCachedLocalPlayerPatch();;
					
					if (playerpatch != null && (playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT || !ClientConfig.enableOriginalModel) && ClientConfig.enableAnimatedFirstPersonModel) {
						return true;
					}
					
					return false;
				}
			});
		}));
	}
	
	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onGameEventBus(IEventBus eventBus) {
	}
}