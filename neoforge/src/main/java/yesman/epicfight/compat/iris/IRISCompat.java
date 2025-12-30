package yesman.epicfight.compat.iris;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.sodium.client.SodiumFakeBlockRenderer;

public class IRISCompat implements ICompatModule {
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onGameEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> {
			ComputeShaderProvider.initIris();
			event.enqueueWork(() -> RenderEngine.getInstance().reloadFakeBlockRenderer(new SodiumFakeBlockRenderer()));
		});
	}
	
	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
	}
}
