package yesman.epicfight.compat.azurelib;

import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.azurelib.client.AzureModelTransformer;

public class AzureLibCompat implements ICompatModule {
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}

	@Override
	public void onGameEventBus(IEventBus eventBus) {
	}

	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		HumanoidModelBaker.registerNewTransformer(new AzureModelTransformer());
	}

	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
		EpicFightClientEventHooks.Render.ANIMATED_ARMOR_TEXTURE.registerEvent(AzureModelTransformer::getGeoArmorTexturePath);
	}
}
