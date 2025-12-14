package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPModifyPlayerData;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPModifyEntityModelYRot {
	private final float modelYRot;
	private final boolean disable;
	
	public CPModifyEntityModelYRot() {
		this.modelYRot = 0F;
		this.disable = true;
	}
	
	public CPModifyEntityModelYRot(float degree) {
		this.modelYRot = degree;
		this.disable = false;
	}
	
	private CPModifyEntityModelYRot(float degree, boolean disable) {
		this.modelYRot = degree;
		this.disable = disable;
	}
	
	public static CPModifyEntityModelYRot fromBytes(FriendlyByteBuf buf) {
		return new CPModifyEntityModelYRot(buf.readFloat(), buf.readBoolean());
	}
	
	public static void toBytes(CPModifyEntityModelYRot msg, FriendlyByteBuf buf) {
		buf.writeFloat(msg.modelYRot);
		buf.writeBoolean(msg.disable);
	}
	
	public static void handle(CPModifyEntityModelYRot msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(( )-> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				if (msg.disable) {
					playerpatch.disableModelYRot(false);
					EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPModifyPlayerData.disablePlayerYRot(playerpatch.getOriginal().getId()), playerpatch.getOriginal());
				} else {
					playerpatch.setModelYRot(msg.modelYRot, false);
					EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPModifyPlayerData.setPlayerYRot(playerpatch.getOriginal().getId(), msg.modelYRot), playerpatch.getOriginal());
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}