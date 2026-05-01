package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public record SPChangeGamerule(EpicFightGameRules.KeyValuePair keyValuePair) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPChangeGamerule> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.GAMERULE,
			SPChangeGamerule::keyValuePair,
			SPChangeGamerule::new
	    );
}