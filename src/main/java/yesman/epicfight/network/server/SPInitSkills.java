package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

public record SPInitSkills(CompoundTag serializedSkill) {
	public SPInitSkills(CapabilitySkill skillCapability) {
		this(skillCapability.serialize());
	}
	
	public static SPInitSkills fromBytes(FriendlyByteBuf buf) {
		return new SPInitSkills(buf.readNbt());
	}
	
	public static void toBytes(SPInitSkills msg, FriendlyByteBuf buf) {
		buf.writeNbt(msg.serializedSkill());
	}
	
	public static void handle(SPInitSkills msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
			
			if (playerpatch != null) {
				playerpatch.getSkillCapability().deserialize(msg.serializedSkill());
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
