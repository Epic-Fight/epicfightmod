package yesman.epicfight.compat;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

public class IRISCompat implements ICompatModule {
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onGameEventBus(IEventBus eventBus) {
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		ComputeShaderProvider.initIris();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
	}
}
