package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record SPChangePlayerMode(int entityId, PlayerPatch.PlayerMode mode) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPChangePlayerMode> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT,
			SPChangePlayerMode::entityId,
			ByteBufCodecsExtends.enumCodec(PlayerPatch.PlayerMode.class),
			SPChangePlayerMode::mode,
			SPChangePlayerMode::new
	    );
}