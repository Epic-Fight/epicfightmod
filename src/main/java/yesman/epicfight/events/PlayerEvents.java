package yesman.epicfight.events;

import java.io.File;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AbstractAnimatorControl;
import yesman.epicfight.network.server.SPAbsorption;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public final class PlayerEvents {
	private PlayerEvents() {}
	
	@SubscribeEvent
	public static void arrowLoose(ArrowLooseEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), PlayerPatch.class).ifPresent(playerpatch -> {
			if (playerpatch.isLogicalClient()) {
				playerpatch.getAnimator().playShootingAnimation();
			} else {
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPAnimatorControl(AbstractAnimatorControl.Action.SHOT, Animations.EMPTY_ANIMATION, event.getEntity().getId(), 0.0F, false), event.getEntity());
			}
		});
	}
	
	@SubscribeEvent
	public static void startTrackingEvent(PlayerEvent.StartTracking event) {
		// Sync absorption attribute
		if (event.getTarget() instanceof LivingEntity livingEntity) {
			if (livingEntity.getAbsorptionAmount() > 0.0F) {
				EpicFightNetworkManager.sendToPlayer(new SPAbsorption(event.getTarget().getId(), livingEntity.getAbsorptionAmount()), (ServerPlayer)event.getEntity());
			}
		}
		
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
	
	public static final PlayerPatch.PlayerMode[] PLAYER_MODE_ENUM = PlayerPatch.PlayerMode.values();
	
	@SubscribeEvent
	public static void playerLoadEvent(PlayerEvent.LoadFromFile event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			File file = new File(event.getPlayerDirectory(), event.getPlayerUUID() + ".dat");
			
			if (!file.exists()) {
				int initialMode = Math.min(EpicFightGameRules.INITIAL_PLAYER_MODE.getRuleValue(event.getEntity().level()), PLAYER_MODE_ENUM.length - 1);
				playerpatch.toMode(PLAYER_MODE_ENUM[initialMode], true);
			}
		});
	}
	
	@SubscribeEvent
	public static void cloneEvent(PlayerEvent.Clone event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getOriginal(), ServerPlayerPatch.class).ifPresent(oldCap -> {
			EpicFightCapabilities.<ServerPlayer, ServerPlayerPatch>getParameterizedEntityPatch(event.getEntity(), ServerPlayer.class, ServerPlayerPatch.class).ifPresent(newCap -> {
				if ((!event.isWasDeath() || EpicFightGameRules.KEEP_SKILLS.getRuleValue(event.getOriginal().level()))) {
					newCap.copyOldData(oldCap, event.isWasDeath());
				}
				
				newCap.toMode(oldCap.getPlayerMode(), false);
			});
		});
	}
	
	@SubscribeEvent
	public static void changeDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.getAnimator().resetLivingAnimations();
			playerpatch.modifyLivingMotionByCurrentItem(true);
			
			EpicFightNetworkManager.PayloadBundleBuilder packetBundleBuilder = EpicFightNetworkManager.PayloadBundleBuilder.create();
			
			playerpatch.getPlayerSkills().listSkillContainers().filter(SkillContainer::hasSkill).forEach(skillContainer -> {
				skillContainer.getSkill().onTracked(skillContainer, packetBundleBuilder);
			});
			
			packetBundleBuilder.send((start, others) -> {
				EpicFightNetworkManager.sendToPlayer(start, playerpatch.getOriginal(), others);
			});
		});
	}
	
	@SubscribeEvent
	public static void rightClickItemServerEvent(RightClickItem.RightClickItem event) {
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
				boolean canceled = playerpatch.getPlayerSkills().fireSkillEvents(EpicFightMod.MODID, event).isCanceled();
				
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
			
			if (!playerpatch.getEntityState().canUseItem()) {
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
			boolean canceled = playerpatch.getPlayerSkills().fireSkillEvents(EpicFightMod.MODID, event).isCanceled();
			event.setCanceled(canceled);
		});
	}
	
	// Fixed by Saithe6(github)
	public static boolean fakePlayerCheck(Player source) {
		return source instanceof FakePlayer;
	}
}
