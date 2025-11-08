package yesman.epicfight.compat.azurelib;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.compat.azurelib.client.AzureArmorTransformer;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.compat.ICompatModule;

public class AzureLibArmorCompat implements ICompatModule {
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		HumanoidModelBaker.registerNewTransformer(new AzureArmorTransformer());
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
		eventBus.addListener(AzureArmorTransformer::getGeoArmorTexturePath);
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onGameEventBus(IEventBus eventBus) {
	}
}