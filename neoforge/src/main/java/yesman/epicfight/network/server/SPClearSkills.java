package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record SPClearSkills(int entityId) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPClearSkills> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT,
			SPClearSkills::entityId,
			SPClearSkills::new
	    );
}
