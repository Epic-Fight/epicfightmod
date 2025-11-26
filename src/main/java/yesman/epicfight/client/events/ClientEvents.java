package yesman.epicfight.client.events;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.UseAnim;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.neoevent.playerpatch.StartActionEvent;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.gamerule.EpicFightGameRules.ConfigurableGameRule;
import yesman.epicfight.world.level.block.FractureBlockState;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public final class ClientEvents {
	private ClientEvents() {}
	
	private static final Pair<ResourceLocation, ResourceLocation> OFFHAND_TEXTURE = Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	
	@SubscribeEvent
	public static void epicfight$mouseButtonPressedInScreenPre(ScreenEvent.MouseButtonPressed.Pre event) {
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
	public static void epicfight$mouseButtonReleasedInScreenPre(ScreenEvent.MouseButtonReleased.Pre event) {
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
	public static void epicfight$keyPressedInScreenPre(ScreenEvent.KeyPressed.Pre event) {
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
	public static void epicfight$rightClickItem(PlayerInteractEvent.RightClickItem event) {
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
				boolean canceled = playerpatch.getPlayerSkills().fireSkillEvents(EpicFightMod.MODID, event).isCanceled();
				
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
	public static void epicfight$loggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getPlayer(), LocalPlayerPatch.class).ifPresent(entitypatch -> {
			ControlEngine.getInstance().reloadPlayerPatch(entitypatch);
			RenderEngine.getInstance().initHUD(entitypatch);
		});
	}
	
	/**
	 * Bad code: should be fixed after Forge provides any parameters that can figure out if respawning caused by dimension changes
	 */
	@Deprecated
	public static ClientboundRespawnPacket packet;
	
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void epicfight$clonePlayer(ClientPlayerNetworkEvent.Clone event) {
		LocalPlayerPatch oldCap = EpicFightCapabilities.getEntityPatch(event.getOldPlayer(), LocalPlayerPatch.class);
		LocalPlayerPatch newCap = EpicFightCapabilities.getEntityPatch(event.getNewPlayer(), LocalPlayerPatch.class);
		
		/**
		 * oldCap == null when a player revives after it disappears
		 */
		if (oldCap != null && newCap != null) {
			if (packet != null && packet.shouldKeep((byte)3)) {
				event.getNewPlayer().tickCount = event.getOldPlayer().tickCount;
				newCap.copyOldData(oldCap, false);
			}
			
			packet = null;
			newCap.onRespawnLocalPlayer(event);
			newCap.toMode(oldCap.getPlayerMode(), false);
		}
		
		EpicFightGameRules.GAME_RULES.values().forEach(gamerule -> {
			Object val = gamerule.getRuleValue(event.getOldPlayer().level());
			((ConfigurableGameRule<Object, ?, ?>)gamerule).setRuleValue(event.getNewPlayer().level(), val);
		});
		
		ControlEngine.getInstance().reloadPlayerPatch(newCap);
		RenderEngine.getInstance().initHUD(newCap);
	}
	
	@SubscribeEvent
	public static void epicfight$loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
		if (event.getPlayer() != null) {
			ItemCapabilityReloadListener.reset();
			EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.clear();
			EpicFightCapabilities.ENTITY_PATCH_PROVIDER.clearDatapackEntities();
			WeaponTypeReloadListener.clear();
			RenderEngine.getInstance().clear();
			FractureBlockState.reset();
		}
	}
	
	@SubscribeEvent
	public static void epicfight$startActionEvent(StartActionEvent event) {
		event.runOnLocalClient(playerpatch -> {
			ControlEngine.getInstance().unlockHotkeys();
		});
	}
}