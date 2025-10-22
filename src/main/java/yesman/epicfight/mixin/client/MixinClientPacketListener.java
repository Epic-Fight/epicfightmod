package yesman.epicfight.mixin.client;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import yesman.epicfight.client.events.ClientEvents;
import yesman.epicfight.client.events.engine.ControlEngine;

@Mixin(value = ClientPacketListener.class)
public abstract class MixinClientPacketListener {
	@Inject(at = @At(value = "HEAD"), method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", cancellable = false)
	private void epicfight_handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo info) {
		ClientEvents.packet = clientboundRespawnPacket;
	}

    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onBeforeSendPacket(Packet<?> packet, CallbackInfo ci) {
        final boolean isSwapItemWithOffhand = packet instanceof ServerboundPlayerActionPacket actionPacket &&
                actionPacket.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND; 
        if (isSwapItemWithOffhand && ControlEngine.shouldDisableSwapHandItems()) {
            // Disables the swap offhand items while in action (e.g., attacking in Epic Fight mode).
            ci.cancel();
        }
    }
}