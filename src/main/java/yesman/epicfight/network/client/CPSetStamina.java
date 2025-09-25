package yesman.epicfight.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record CPSetStamina(float consumption, boolean resetActionTick) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, CPSetStamina> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.FLOAT,
			CPSetStamina::consumption,
			ByteBufCodecs.BOOL,
			CPSetStamina::resetActionTick,
			CPSetStamina::new
	    );
}