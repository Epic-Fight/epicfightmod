package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPAnimatorControl extends AnimatorControlPacket {
	private boolean isClientOnly;
	private boolean responseToSender;
	
	public CPAnimatorControl(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float transitionTime, boolean pause, boolean clientOnly, boolean resendToSender) {
		this(action, animation.get().getId(), transitionTime, pause, clientOnly, resendToSender);
	}
	
	public CPAnimatorControl(AnimatorControlPacket.Action action, int animationId, float transitionTimeModifier, boolean pause, boolean clientOnly, boolean resendToSender) {
		super(action, animationId, transitionTimeModifier, pause);
		
		this.isClientOnly = clientOnly;
		this.responseToSender = resendToSender;
	}
	
	public static CPAnimatorControl fromBytes(FriendlyByteBuf buf) {
		return new CPAnimatorControl(buf.readEnum(Action.class), buf.readInt(), buf.readFloat(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
	}
	
	public static void toBytes(CPAnimatorControl msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.action);
		buf.writeInt(msg.animationId);
		buf.writeFloat(msg.transitionTimeModifier);
		buf.writeBoolean(msg.pause);
		buf.writeBoolean(msg.isClientOnly);
		buf.writeBoolean(msg.responseToSender);
	}	
	
	public static void handle(CPAnimatorControl msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()-> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				if (!msg.isClientOnly) {
					msg.process(playerpatch);
				}
				
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPAnimatorControl(msg.action, msg.animationId, playerpatch.getOriginal().getId(), msg.transitionTimeModifier, msg.pause), playerpatch.getOriginal());
				
				if (msg.responseToSender) {
					EpicFightNetworkManager.sendToPlayer(new SPAnimatorControl(msg.action, msg.animationId, playerpatch.getOriginal().getId(), msg.transitionTimeModifier, msg.pause), playerpatch.getOriginal());
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}