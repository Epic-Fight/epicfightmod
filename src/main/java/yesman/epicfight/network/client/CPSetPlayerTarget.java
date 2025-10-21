package yesman.epicfight.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record CPSetPlayerTarget(int targetEntityId) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, CPSetPlayerTarget> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT,
			CPSetPlayerTarget::targetEntityId,
			CPSetPlayerTarget::new
	    );
}