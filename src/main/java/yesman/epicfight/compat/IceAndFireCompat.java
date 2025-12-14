package yesman.epicfight.compat;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;

import net.minecraftforge.eventbus.api.IEventBus;
import yesman.epicfight.api.forgeevent.BattleModeSustainableEvent;

public class IceAndFireCompat implements ICompatModule {
	@Override
	public void onModEventBus(IEventBus eventBus) {
		
	}

	@Override
	public void onForgeEventBus(IEventBus eventBus) {
		eventBus.<BattleModeSustainableEvent>addListener((event) -> {
			if (event.getPlayerPatch().getOriginal().getVehicle() instanceof EntityDragonBase) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		
	}

	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
		
	}
}
