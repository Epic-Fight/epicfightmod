package yesman.epicfight.skill;

import java.util.Set;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.registry.EpicFightRegistries;

public record SkillDataKey<T> (StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec, T defaultValue, boolean syncronizeToRemotePlayers, Set<Class<? extends Skill>> referencingSkillClasses) implements StreamCodec<ByteBuf, T> {
	public static final Codec<Holder<SkillDataKey<?>>> CODEC = EpicFightRegistries.SKILL_DATA_KEY.holderByNameCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SkillDataKey<?>>> STREAM_CODEC = ByteBufCodecs.holderRegistry(EpicFightRegistries.Keys.SKILL_DATA_KEY);
	
	@SafeVarargs
	public static <T> SkillDataKey<T> createSkillDataKey(StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers, Class<? extends Skill>... skillClass) {
		return new SkillDataKey<T> (packetCodec, defaultValue, syncronizeTrackingPlayers, skillClass);
	}
	
	@SafeVarargs
	public SkillDataKey(StreamCodec<? super RegistryFriendlyByteBuf, T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers, Class<? extends Skill>... skillClass) {
		this(packetCodec, defaultValue, syncronizeTrackingPlayers, Set.of(skillClass));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T decode(ByteBuf buffer) {
		return ((StreamCodec<ByteBuf, T>)this.packetCodec).decode(buffer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void encode(ByteBuf buffer, T value) {
		((StreamCodec<ByteBuf, T>)this.packetCodec).encode(buffer, value);
	}
}
