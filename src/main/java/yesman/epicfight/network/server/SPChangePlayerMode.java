package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SPChangePlayerMode {
	private final int entityId;
	private final PlayerPatch.PlayerMode mode;

	public SPChangePlayerMode() {
		this.entityId = 0;
		this.mode = PlayerPatch.PlayerMode.VANILLA;
	}

	public SPChangePlayerMode(int entityId, PlayerPatch.PlayerMode battleMode) {
		this.entityId = entityId;
		this.mode = battleMode;
	}

	public static SPChangePlayerMode fromBytes(FriendlyByteBuf buf) {
		return new SPChangePlayerMode(buf.readInt(), PlayerPatch.PlayerMode.values()[buf.readInt()]);
	}

	public static void toBytes(SPChangePlayerMode msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeInt(msg.mode.ordinal());
	}
	
	public static void handle(SPChangePlayerMode msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.player.level().getEntity(msg.entityId);
			
			if (entity != null) {
				PlayerPatch<?> playerpatch = (PlayerPatch<?>)entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
				
				if (playerpatch != null) {
					playerpatch.toMode(msg.mode, false);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}