package yesman.epicfight.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record CPModifyEntityModelYRot(float modelYRot, boolean disable) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, CPModifyEntityModelYRot> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.FLOAT,
			CPModifyEntityModelYRot::modelYRot,
			ByteBufCodecs.BOOL,
			CPModifyEntityModelYRot::disable,
			CPModifyEntityModelYRot::new
	    );
	
	public CPModifyEntityModelYRot(float degree) {
		this(degree, false);
	}
}