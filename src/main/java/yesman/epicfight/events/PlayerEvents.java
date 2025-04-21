package yesman.epicfight.events;

import java.io.File;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.ItemUseEndEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.RightClickItemEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID)
public class PlayerEvents {
	@SubscribeEvent
	public static void startTrackingEvent(PlayerEvent.StartTracking event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getTarget(), EntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.onStartTracking((ServerPlayer)event.getEntity());
		});
	}
	
	@SubscribeEvent
	public static void stopTrackingEvent(PlayerEvent.StopTracking event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getTarget(), EntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.onStopTracking((ServerPlayer)event.getEntity());
		});
	}
	
	@SubscribeEvent
	public static void playerLoadEvent(PlayerEvent.LoadFromFile event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			File file = new File(event.getPlayerDirectory(), event.getPlayerUUID() + ".dat");
			
			if (!file.exists()) {
				int initialMode = Math.min(EpicFightGameRules.INITIAL_PLAYER_MODE.getRuleValue(event.getEntity().level()), PlayerPatch.PlayerMode.values().length - 1);
				playerpatch.toMode(PlayerPatch.PlayerMode.values()[initialMode], true);
			}
		});
	}
	
	@SubscribeEvent
	public static void cloneEvent(PlayerEvent.Clone event) {
		event.getOriginal().reviveCaps();
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getOriginal(), ServerPlayerPatch.class).ifPresent(oldCap -> {
			EpicFightCapabilities.<ServerPlayer, ServerPlayerPatch>getParameterizedEntityPatch(event.getEntity(), ServerPlayer.class, ServerPlayerPatch.class).ifPresent(newCap -> {
				if ((!event.isWasDeath() || EpicFightGameRules.KEEP_SKILLS.getRuleValue(event.getOriginal().level()))) {
					newCap.copySkillsFrom(oldCap);
				}
				
				newCap.toMode(oldCap.getPlayerMode(), false);
			});
		});
		
		event.getOriginal().invalidateCaps();
	}
	
	@SubscribeEvent
	public static void changeDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.getAnimator().resetLivingAnimations();
			playerpatch.modifyLivingMotionByCurrentItem(true);
			
			EpicFightGameRules.WEIGHT_PENALTY.synchronizeTo(playerpatch.getOriginal());
			EpicFightGameRules.DISABLE_ENTITY_UI.synchronizeTo(playerpatch.getOriginal());
		});
	}
	
	@SubscribeEvent
	public static void rightClickItemServerEvent(RightClickItem event) {
		/**
		 * Client item use event is fired in {@link ClientEvents#rightClickItemClient}
		 */
		if (event.getSide() == LogicalSide.CLIENT) {
			return;
		}
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			ItemStack itemstack = playerpatch.getOriginal().getOffhandItem();
			
			if (!playerpatch.getEntityState().canUseItem()) {
				event.setCanceled(true);
			} else if (itemstack.getUseAnimation() == UseAnim.NONE || !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand()) {
				boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.SERVER_ITEM_USE_EVENT, new RightClickItemEvent<>(playerpatch));
				
				if (playerpatch.getEntityState().movementLocked()) {
					canceled = true;
				}
				
				event.setCanceled(canceled);
			}
		});
	}
	
	@SubscribeEvent
	public static void itemUseStartEvent(LivingEntityUseItemEvent.Start event) {
		EpicFightCapabilities.<Player, PlayerPatch<Player>>getParameterizedEntityPatch(event.getEntity(), Player.class, PlayerPatch.class).ifPresent(playerpatch -> {
			InteractionHand hand = playerpatch.getOriginal().getItemInHand(InteractionHand.MAIN_HAND).equals(event.getItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			CapabilityItem itemCap = playerpatch.getHoldingItemCapability(hand);
			
			if (!playerpatch.getEntityState().canUseSkill() || !playerpatch.getEntityState().canUseItem()) {
				event.setCanceled(true);
			} else if (event.getItem() == playerpatch.getOriginal().getOffhandItem() && !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand()) {
				event.setCanceled(true);
			}
			
			if (itemCap.getUseAnimation(playerpatch) == UseAnim.BLOCK) {
				event.setDuration(Integer.MAX_VALUE);
			}
		});
	}
	
	@SubscribeEvent
	public static void itemUseStopEvent(LivingEntityUseItemEvent.Stop event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.SERVER_ITEM_STOP_EVENT, new ItemUseEndEvent(playerpatch, event));
			event.setCanceled(canceled);
		});
	}
	
	@SubscribeEvent
	public static void attackEntityEvent(AttackEntityEvent event) {
		EpicFightCapabilities.<Player, PlayerPatch<Player>>getParameterizedEntityPatch(event.getEntity(), Player.class, PlayerPatch.class).ifPresent(playerpatch -> {
			boolean isLivingTarget = event.getTarget() instanceof LivingEntity livingEntity && livingEntity.attackable();
			
			if (!EpicFightGameRules.DO_VANILLA_ATTACK.getRuleValue(event.getEntity().level()) && isLivingTarget && playerpatch.getEpicFightDamageSource() == null && !fakePlayerCheck(event.getEntity())) {
				event.setCanceled(true);
			}
		});
	}
	
	// Fixed by Saithe6(github)
	public static boolean fakePlayerCheck(Player source) {
		return source instanceof FakePlayer;
	}
}
