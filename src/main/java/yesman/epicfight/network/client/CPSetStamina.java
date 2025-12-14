package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPSetStamina {
	private final float consumption;
	private final boolean resetActionTick;
	
	public CPSetStamina(float consumption, boolean resetActionTick) {
		this.consumption = consumption;
		this.resetActionTick = resetActionTick;
	}

	public static CPSetStamina fromBytes(FriendlyByteBuf buf) {
		return new CPSetStamina(buf.readFloat(), buf.readBoolean());
	}

	public static void toBytes(CPSetStamina msg, FriendlyByteBuf buf) {
		buf.writeFloat(msg.consumption);
		buf.writeBoolean(msg.resetActionTick);
	}
	
	public static void handle(CPSetStamina msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				playerpatch.setStamina(msg.consumption);
				
				if (msg.resetActionTick) {
					playerpatch.resetActionTick();
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}