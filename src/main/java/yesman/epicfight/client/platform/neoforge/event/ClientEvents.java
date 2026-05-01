package yesman.epicfight.client.platform.neoforge.event;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yesman.epicfight.api.client.event.impl.VanillaGUIEventHooks;
import yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public final class ClientEvents {
	@SubscribeEvent
	public static void epicfight$mouseButtonPressedInScreenPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (VanillaGUIEventHooks.onMouseButtonPressedInScreen(event.getScreen())) {
            event.setCanceled(true);
        }
	}
	
	@SubscribeEvent
	public static void epicfight$mouseButtonReleasedInScreenPre(ScreenEvent.MouseButtonReleased.Pre event) {
        if (VanillaGUIEventHooks.onMouseButtonReleasedInScreen(event.getScreen())) {
            event.setCanceled(true);
        }
	}
	
	@SubscribeEvent
	public static void epicfight$keyPressedInScreenPre(ScreenEvent.KeyPressed.Pre event) {
        if (VanillaGUIEventHooks.onKeyboardPressedInScreen(event.getScreen(), event.getKeyCode())) {
            event.setCanceled(true);
        }
	}
	
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