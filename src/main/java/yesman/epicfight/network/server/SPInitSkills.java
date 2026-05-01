package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.world.capabilities.skill.PlayerSkills;

public record SPInitSkills(CompoundTag serializedSkill) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPInitSkills> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG,
			SPInitSkills::serializedSkill,
			SPInitSkills::new
	    );
	
	public SPInitSkills(PlayerSkills skillCapability) {
		this(skillCapability.write(new CompoundTag()));
	}
}