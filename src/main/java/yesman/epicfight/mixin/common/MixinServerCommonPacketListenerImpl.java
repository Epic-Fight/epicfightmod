package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(value = ServerCommonPacketListenerImpl.class)
public abstract class MixinServerCommonPacketListenerImpl {
	@Shadow
	public MinecraftServer server;
	@Shadow
	public Connection connection;
	@Shadow
	protected abstract GameProfile playerProfile();
	
	@Inject(at = @At(value = "TAIL"), method = "handleResourcePackResponse(Lnet/minecraft/network/protocol/common/ServerboundResourcePackPacket;)V")
	public void epicfight$handleResourcePackResponse(ServerboundResourcePackPacket packet, CallbackInfo info) {
		if (packet.action() == ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED) {
			ServerPlayer serverPlayer = this.server.getPlayerList().getPlayer(this.playerProfile().getId());
			
			EpicFightCapabilities.getUnparameterizedEntityPatch(serverPlayer, ServerPlayerPatch.class).ifPresent(playerpatch -> {
				playerpatch.modifyLivingMotionByCurrentItem(false);
			});
		}
	}
}