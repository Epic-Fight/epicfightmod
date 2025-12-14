package yesman.epicfight.network.client;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPSetPlayerTarget {
	private final int entityId;

	public CPSetPlayerTarget() {
		this.entityId = 0;
	}

	public CPSetPlayerTarget(int entityId) {
		this.entityId = entityId;
	}

	public static CPSetPlayerTarget fromBytes(FriendlyByteBuf buf) {
		return new CPSetPlayerTarget(buf.readInt());
	}

	public static void toBytes(CPSetPlayerTarget msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
	}
	
	public static void handle(CPSetPlayerTarget msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(entitypatch -> {
				Entity entity = entitypatch.getOriginal().level().getEntity(msg.entityId);
				
				if (entity instanceof LivingEntity livingEntity) {
					entitypatch.setAttackTarget(livingEntity);
				} else if (entity == null) {
					entitypatch.setAttackTarget(null);
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}