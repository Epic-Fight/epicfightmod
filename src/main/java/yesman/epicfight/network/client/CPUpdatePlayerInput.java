package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPUpdatePlayerInput;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class CPUpdatePlayerInput {
	private int entityId;
	private float forward;
	private float strafe;

	public CPUpdatePlayerInput() {
	}

	public CPUpdatePlayerInput(int entityId, float forward, float strafe) {
		this.entityId = entityId;
		this.forward = forward;
		this.strafe = strafe;
	}

	public static CPUpdatePlayerInput fromBytes(FriendlyByteBuf buf) {
		return new CPUpdatePlayerInput(buf.readInt(), buf.readFloat(), buf.readFloat());
	}

	public static void toBytes(CPUpdatePlayerInput msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.forward);
		buf.writeFloat(msg.strafe);
	}
	
	public static void handle(CPUpdatePlayerInput msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			
			player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent((entitypatch) -> {
				if (entitypatch instanceof PlayerPatch<?> plyaerpatch) {
					plyaerpatch.dx = msg.strafe;
					plyaerpatch.dz = msg.forward;
					EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPUpdatePlayerInput(msg.entityId, msg.forward, msg.strafe), player);
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}