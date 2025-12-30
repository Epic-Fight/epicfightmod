package yesman.epicfight.network.server;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record SPPlayUISound(Holder<SoundEvent> sound, float pitch, float volume) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SPPlayUISound> STREAM_CODEC =
		StreamCodec.composite(
			SoundEvent.STREAM_CODEC,
			SPPlayUISound::sound,
			ByteBufCodecs.FLOAT,
			SPPlayUISound::pitch,
			ByteBufCodecs.FLOAT,
			SPPlayUISound::volume,
			SPPlayUISound::new
	    );
	
	public SPPlayUISound(Holder<SoundEvent> sound) {
		this(sound, 1.0F, 1.0F);
	}
}
