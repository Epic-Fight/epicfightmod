package yesman.epicfight.config;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.DEDICATED_SERVER)
public class ServerConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec.BooleanValue ALLOW_CUSTOM_ANIMATIONS = BUILDER.define("allow_custom_animations", false);
	public static final ModConfigSpec SPEC = BUILDER.build();
	
	public static boolean allowCustomAnimations;
	
	@SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
		if (event.getConfig().getType() != ModConfig.Type.SERVER) {
			return;
		}
		
		allowCustomAnimations = ALLOW_CUSTOM_ANIMATIONS.get();
	}
}
