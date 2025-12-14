package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ServerboundResourcePackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(value = ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
	@Shadow
	public ServerPlayer player;
	
	@Inject(at = @At(value = "TAIL"), method = "handleResourcePackResponse(Lnet/minecraft/network/protocol/game/ServerboundResourcePackPacket;)V")
	public void epicfight_handleResourcePackResponse(ServerboundResourcePackPacket packet, CallbackInfo info) {
		if (packet.getAction() == ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(this.player, ServerPlayerPatch.class);
			
			if (playerpatch != null) {
				playerpatch.modifyLivingMotionByCurrentItem(false);
			}
		}
	}
}