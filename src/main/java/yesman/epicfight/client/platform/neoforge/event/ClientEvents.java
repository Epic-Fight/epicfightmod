package yesman.epicfight.client.platform.neoforge.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public final class ClientEvents {
	@SubscribeEvent
	public static void epicfight$rightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (VanillaGeneralClientEventHooks.onUseItemInClientSide(event.getEntity(), event.getItemStack(), event.getHand())) {
            event.setCanceled(true);
        }
	}
	
	@SubscribeEvent
	public static void epicfight$loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        VanillaGeneralClientEventHooks.onPlayerLoggedIn(event.getPlayer());
	}

	@SubscribeEvent
	public static void epicfight$clonePlayer(ClientPlayerNetworkEvent.Clone event) {
        VanillaGeneralClientEventHooks.onClonedInClient(event.getOldPlayer(), event.getNewPlayer());
	}
	
	@SubscribeEvent
	public static void epicfight$loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		if (event.getPlayer() != null) {
            VanillaGeneralClientEventHooks.onPlayerLoggedOut(event.getPlayer());
		}
	}

    private ClientEvents() {}
}
