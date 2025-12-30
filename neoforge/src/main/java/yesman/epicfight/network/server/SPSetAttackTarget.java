package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record SPSetAttackTarget(int entityId, int targetEntityId) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPSetAttackTarget> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT,
			SPSetAttackTarget::entityId,
			ByteBufCodecs.INT,
			SPSetAttackTarget::targetEntityId,
			SPSetAttackTarget::new
	    );
}