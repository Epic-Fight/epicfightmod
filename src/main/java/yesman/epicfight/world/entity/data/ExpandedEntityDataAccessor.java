package yesman.epicfight.world.entity.data;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.datastructure.ParameterizedMap.ParameterizedKey;
import yesman.epicfight.registry.EpicFightRegistries;

public record ExpandedEntityDataAccessor<T> (@Nullable MapCodec<T> persistent, StreamCodec<ByteBuf, T> streamCodec, T defaultValue) implements ParameterizedKey<T> {
	public static final Codec<Holder<ExpandedEntityDataAccessor<?>>> CODEC = EpicFightRegistries.EXPANDED_ENTITY_DATA_ACCESSOR.holderByNameCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ExpandedEntityDataAccessor<?>>> STREAM_CODEC = ByteBufCodecs.holderRegistry(EpicFightRegistries.Keys.EXPANDED_ENTITY_DATA_ACCESSOR);
	
	public void saveData(T value, RecordBuilder<Tag> recordBuilder) {
		if (this.persistent != null) {
			this.persistent.encode(value, NbtOps.INSTANCE, recordBuilder);
		}
	}
	
	public T readFromTag(MapLike<Tag> compound) {
		if (this.persistent != null) {
			return this.persistent.decode(NbtOps.INSTANCE, compound).result().orElse(null);
		}
		
		return null;
	}
	
	public static <T> Builder<T> builder() {
		return new Builder<> ();
	}
	
	public static final class Builder<T> {
		private MapCodec<T> codec;
		private StreamCodec<ByteBuf, T> streamCodec;
		private T defaultVal;
		
		public Builder<T> persistent(MapCodec<T> codec) {
			this.codec = codec;
			return this;
		}
		
		public Builder<T> dataSerializer(StreamCodec<ByteBuf, T> streamCodec) {
			this.streamCodec = streamCodec;
			return this;
		}
		
		public Builder<T> defaultValue(T defaultValue) {
			this.defaultVal = defaultValue;
			return this;
		}
		
		public ExpandedEntityDataAccessor<T> build() {
			if (this.streamCodec == null) {
				throw new IllegalStateException("No stream codec info provided for Expanded entity data accessor");
			}
			
			if (this.defaultVal == null) {
				throw new IllegalStateException("No default value provided for Expanded entity data accessor");
			}
			
			return new ExpandedEntityDataAccessor<> (this.codec, this.streamCodec, this.defaultVal);
		}
	}
}
