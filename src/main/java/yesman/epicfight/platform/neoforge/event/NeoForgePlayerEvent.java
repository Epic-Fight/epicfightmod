package yesman.epicfight.platform.neoforge.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.UseAnim;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public final class NeoForgePlayerEvent {
    @SubscribeEvent
    public static void arrowLoose(ArrowLooseEvent event) {
        VanillaPlayerEventHooks.onLooseArrow(event.getEntity());
    }

    @SubscribeEvent
    public static void startTrackingEvent(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) VanillaPlayerEventHooks.onStartTracking(event.getTarget(), serverPlayer);
    }

    @SubscribeEvent
    public static void stopTrackingEvent(PlayerEvent.StopTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) VanillaPlayerEventHooks.onStopTracking(event.getTarget(), serverPlayer);
    }

    @SubscribeEvent
    public static void playerLoadEvent(PlayerEvent.LoadFromFile event) {
        VanillaPlayerEventHooks.onLoad(event.getEntity(), event.getPlayerDirectory(), event.getPlayerUUID());
    }

    @SubscribeEvent
    public static void cloneEvent(PlayerEvent.Clone event) {
        VanillaPlayerEventHooks.onCloned(event.getOriginal(), event.getEntity(), event.isWasDeath());
    }

    @SubscribeEvent
    public static void changeDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event) {
        VanillaPlayerEventHooks.onChagneDimension(event.getEntity());
    }

    @SubscribeEvent
    public static void rightClickItemServerEvent(RightClickItem.RightClickItem event) {
        if (VanillaPlayerEventHooks.onUseItemInServerSide(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void itemUseStartEvent(LivingEntityUseItemEvent.Start event) {
        EpicFightCapabilities.getPlayerPatchAsOptional(event.getEntity()).ifPresent(playerpatch -> {
            InteractionHand hand = playerpatch.getOriginal().getItemInHand(InteractionHand.MAIN_HAND).equals(event.getItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            CapabilityItem itemCap = playerpatch.getHoldingItemCapability(hand);

            if (!playerpatch.getEntityState().canUseItem()) {
                event.setCanceled(true);
            } else if (event.getItem() == playerpatch.getOriginal().getOffhandItem() && !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand()) {
                event.setCanceled(true);
            }

            if (itemCap.getUseAnimation(playerpatch) == UseAnim.BLOCK) {
                event.setDuration(event.getItem().getUseDuration(event.getEntity()));
            }
        });
    }

    private NeoForgePlayerEvent() {}
}
