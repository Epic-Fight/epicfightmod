package yesman.epicfight.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record CPChangePlayerMode(PlayerPatch.PlayerMode mode) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, CPChangePlayerMode> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.enumCodec(PlayerPatch.PlayerMode.class),
			CPChangePlayerMode::mode,
			CPChangePlayerMode::new
	    );
}