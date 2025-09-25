package yesman.epicfight.compat;

import java.lang.reflect.Constructor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.neoforge.common.NeoForge;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

public interface ICompatModule {
	public static void loadCompatModule(IEventBus modEventBus, Class<? extends ICompatModule> compatModule) {
		try {
			Constructor<? extends ICompatModule> constructor = compatModule.getConstructor();
			ICompatModule compatModuleInstance = constructor.newInstance();
			compatModuleInstance.onModEventBus(modEventBus);
			compatModuleInstance.onGameEventBus(NeoForge.EVENT_BUS);
			
			if (EpicFightSharedConstants.isPhysicalClient()) {
				compatModuleInstance.onModEventBusClient(modEventBus);
				compatModuleInstance.onGameEventBusClient(NeoForge.EVENT_BUS);
			}
			
			EpicFightMod.LOGGER.info("Loaded mod compat: " + compatModule.getSimpleName());
		} catch (ModLoadingException e) {
			throw e;
		} catch (Exception e) {
			EpicFightMod.LOGGER.error("Failed to load mod compat: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	void onModEventBus(IEventBus eventBus);
	
	void onGameEventBus(IEventBus eventBus);
	
	@OnlyIn(Dist.CLIENT)
	void onModEventBusClient(IEventBus eventBus);
	@OnlyIn(Dist.CLIENT)
	void onGameEventBusClient(IEventBus eventBus);
}