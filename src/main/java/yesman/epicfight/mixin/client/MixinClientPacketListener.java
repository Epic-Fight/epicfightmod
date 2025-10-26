package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import yesman.epicfight.client.events.ClientEvents;

@Mixin(value = ClientPacketListener.class)
public abstract class MixinClientPacketListener {
	@Inject(at = @At(value = "HEAD"), method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V", cancellable = false)
	private void epicfight_handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo info) {
		ClientEvents.packet = clientboundRespawnPacket;
	}
}