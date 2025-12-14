package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SPAbsorption {
	private int entityId;
	private float amount;
	
	public SPAbsorption() {
		this.entityId = -1;
	}
	
	public SPAbsorption(int entityId, float amount) {
		this.entityId = entityId;
		this.amount = amount;
	}
	
	public static SPAbsorption fromBytes(FriendlyByteBuf buf) {
		return new SPAbsorption(buf.readInt(), buf.readFloat());
	}
	
	public static void toBytes(SPAbsorption msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.amount);
	}
	
	public static void handle(SPAbsorption msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.level.getEntity(msg.entityId);
			
			if (entity instanceof LivingEntity livingentity && !(entity instanceof Player)) {
				livingentity.setAbsorptionAmount(msg.amount);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}