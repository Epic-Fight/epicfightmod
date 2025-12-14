package yesman.epicfight.compat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.SodiumFakeBlockRenderer;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

public class IRISCompat implements ICompatModule {
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> {
			ComputeShaderProvider.initIris();
			event.enqueueWork(() -> ClientEngine.getInstance().renderEngine.reloadFakeBlockRenderer(new SodiumFakeBlockRenderer()));
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
	}
}
