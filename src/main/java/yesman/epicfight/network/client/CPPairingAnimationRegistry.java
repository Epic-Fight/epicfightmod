package yesman.epicfight.network.client;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record CPPairingAnimationRegistry(List<String> registryNames) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, CPPairingAnimationRegistry> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.listOf(ByteBufCodecs.STRING_UTF8),
			CPPairingAnimationRegistry::registryNames,
			CPPairingAnimationRegistry::new
	    );
}
