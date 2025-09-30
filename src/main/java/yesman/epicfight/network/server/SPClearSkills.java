package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record SPClearSkills(int entityId) {
	public static SPClearSkills fromBytes(FriendlyByteBuf buf) {
		return new SPClearSkills(buf.readInt());
	}
	
	public static void toBytes(SPClearSkills msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId());
	}
	
	public static void handle(SPClearSkills msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId());
			
			EpicFightCapabilities.getUnparameterizedEntityPatch(entity, PlayerPatch.class).ifPresent(playerpatch -> {
				playerpatch.getSkillCapability().clearContainersAndLearnedSkills();
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
