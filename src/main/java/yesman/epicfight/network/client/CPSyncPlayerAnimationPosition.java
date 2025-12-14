package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.SyncAnimationPositionPacket;
import yesman.epicfight.network.server.SPSyncAnimationPosition;

public class CPSyncPlayerAnimationPosition extends SyncAnimationPositionPacket {
	public CPSyncPlayerAnimationPosition(int entityId, float elapsedTime, Vec3 position, int lerpSteps) {
		super(entityId, elapsedTime, position, lerpSteps);
	}
	
	public static CPSyncPlayerAnimationPosition fromBytes(FriendlyByteBuf buf) {
		return new CPSyncPlayerAnimationPosition(buf.readInt(), buf.readFloat(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readInt());
	}
	
	public static void toBytes(CPSyncPlayerAnimationPosition msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.elapsedTime);
		buf.writeDouble(msg.position.x);
		buf.writeDouble(msg.position.y);
		buf.writeDouble(msg.position.z);
		buf.writeInt(msg.lerpSteps);
	}
	
	public static void handle(CPSyncPlayerAnimationPosition msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity entity = ctx.get().getSender().level().getEntity(msg.entityId);
			
			if (entity != null && entity instanceof Player player) {
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPSyncAnimationPosition(entity.getId(), msg.elapsedTime, msg.position, msg.lerpSteps), player);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
