package yesman.epicfight.client.events;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.RightClickItemEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.gamerule.EpicFightGameRules.ConfigurableGameRule;
import yesman.epicfight.world.level.block.FractureBlockState;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
	private static final Pair<ResourceLocation, ResourceLocation> OFFHAND_TEXTURE = Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	
	@SubscribeEvent
	public static void mouseClickEvent(ScreenEvent.MouseButtonPressed.Pre event) {
		if (event.getScreen() instanceof AbstractContainerScreen) {
			Slot slot = ((AbstractContainerScreen<?>)event.getScreen()).getSlotUnderMouse();
			
			if (slot != null) {
				CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(MINECRAFT.player.containerMenu.getCarried());
				
				if (!cap.canBePlacedOffhand()) {
					if (slot.getNoItemIcon() != null && slot.getNoItemIcon().equals(OFFHAND_TEXTURE)) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void mouseReleaseEvent(ScreenEvent.MouseButtonReleased.Pre event) {
		if (event.getScreen() instanceof AbstractContainerScreen) {
			Slot slot = ((AbstractContainerScreen<?>)event.getScreen()).getSlotUnderMouse();
			
			if (slot != null) {
				CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(MINECRAFT.player.containerMenu.getCarried());
				
				if (!cap.canBePlacedOffhand()) {
					if (slot.getNoItemIcon() != null && slot.getNoItemIcon().equals(OFFHAND_TEXTURE)) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void presssKeyInGui(ScreenEvent.KeyPressed.Pre event) {
		CapabilityItem itemCapability = CapabilityItem.EMPTY;

        // TODO: (INPUT_SYSTEM_REFACTOR) This only disables putting the item to offhand inventory slot for key inputs (defaults to F).
        //  Explore a universal solution that also supports controllers and other input systems.
        //  https://github.com/Epic-Fight/epicfight/issues/2135
		if (event.getKeyCode() == MINECRAFT.options.keySwapOffhand.getKey().getValue()) {
			if (event.getScreen() instanceof AbstractContainerScreen) {
				Slot slot = ((AbstractContainerScreen<?>)event.getScreen()).getSlotUnderMouse();
				
				if (slot != null && slot.hasItem()) {
					itemCapability = EpicFightCapabilities.getItemStackCapability(slot.getItem());
					
					if (!itemCapability.canBePlacedOffhand()) {
						event.setCanceled(true);
					}
				}
			}
		} else if (event.getKeyCode() >= 49 && event.getKeyCode() <= 57) {
			if (event.getScreen() instanceof AbstractContainerScreen) {
				Slot slot = ((AbstractContainerScreen<?>)event.getScreen()).getSlotUnderMouse();
				
				if (slot != null && slot.getNoItemIcon() != null && slot.getNoItemIcon().equals(OFFHAND_TEXTURE)) {
					itemCapability = EpicFightCapabilities.getItemStackCapability(MINECRAFT.player.getInventory().getItem(event.getKeyCode() - 49));
					
					if (!itemCapability.canBePlacedOffhand()) {
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void rightClickItemClient(PlayerInteractEvent.RightClickItem event) {
		/**
		 * Server item use event is fired in {@link PlayerEvents#rightClickItemServerEvent}
		 */
		if (event.getSide() == LogicalSide.SERVER) {
			return;
		}
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LocalPlayerPatch.class).ifPresent(playerpatch -> {
			if (!playerpatch.getEntityState().canUseItem()) {
				event.setCanceled(true);
			} else if (playerpatch.getOriginal().getOffhandItem().getUseAnimation() == UseAnim.NONE) {
				boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.CLIENT_ITEM_USE_EVENT, new RightClickItemEvent<> (playerpatch));
				
				if (playerpatch.getEntityState().movementLocked()) {
					canceled = true;
				}
				
				event.setCanceled(canceled);
			}
			
			if (!event.isCanceled()) {
				EpicFightCameraAPI.getInstance().onItemUseEvent(event.getEntity(), playerpatch, event.getItemStack(), event.getHand());
			}
		});
	}
	
	@SubscribeEvent
	public static void clientLoggingInEvent(ClientPlayerNetworkEvent.LoggingIn event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getPlayer(), LocalPlayerPatch.class).ifPresent(ClientEngine.getInstance().controlEngine::setPlayerPatch);
		ClientEngine.getInstance().renderEngine.initHUD();
	}
	
	/**
	 * Bad code: should be fixed after Forge provides any parameters that can figure out if respawning caused by dimension changes
	 */
	@Deprecated
	public static ClientboundRespawnPacket packet;
	
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void clientRespawnEvent(ClientPlayerNetworkEvent.Clone event) {
		LocalPlayerPatch oldCap = EpicFightCapabilities.getEntityPatch(event.getOldPlayer(), LocalPlayerPatch.class);
		LocalPlayerPatch newCap = EpicFightCapabilities.getEntityPatch(event.getNewPlayer(), LocalPlayerPatch.class);
		
		/**
		 * oldCap == null when a player revives after it disappears
		 */
		if (oldCap != null && newCap != null) {
			if (packet != null && packet.shouldKeep((byte)3)) {
				event.getNewPlayer().tickCount = event.getOldPlayer().tickCount;
				newCap.copySkillsFrom(oldCap, false);
			}
			
			packet = null;
			newCap.onRespawnLocalPlayer(event);
			newCap.toMode(oldCap.getPlayerMode(), false);
		}
		
		EpicFightGameRules.GAME_RULES.values().forEach(gamerule -> {
			Object val = gamerule.getRuleValue(event.getOldPlayer().level());
			((ConfigurableGameRule<Object, ?, ?>)gamerule).setRuleValue(event.getNewPlayer().level(), val);
		});
		
		ClientEngine.getInstance().controlEngine.setPlayerPatch(newCap);
		ClientEngine.getInstance().renderEngine.initHUD();
	}
	
	@SubscribeEvent
	public static void clientLogoutEvent(ClientPlayerNetworkEvent.LoggingOut event) {
		if (event.getPlayer() != null) {
			ItemCapabilityReloadListener.reset();
			ItemCapabilityProvider.clear();
			EntityPatchProvider.clear();
			WeaponTypeReloadListener.clear();
			ClientEngine.getInstance().renderEngine.clear();
			FractureBlockState.reset();
		}
	}
}