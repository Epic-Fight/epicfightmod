package yesman.epicfight.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record CPUpdatePlayerInput(int entityId, float forward, float strafe) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, CPUpdatePlayerInput> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT,
			CPUpdatePlayerInput::entityId,
			ByteBufCodecs.FLOAT,
			CPUpdatePlayerInput::forward,
			ByteBufCodecs.FLOAT,
			CPUpdatePlayerInput::strafe,
			CPUpdatePlayerInput::new
	    );
}