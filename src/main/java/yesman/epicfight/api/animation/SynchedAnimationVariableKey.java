package yesman.epicfight.api.animation;

import java.util.function.Function;

import javax.annotation.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.animation.AnimationVariables.AnimationVariableKey;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.callbacks.SynchedAnimationVariableKeyCallbacks;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public interface SynchedAnimationVariableKey<T> extends AnimationVariableKey<T>, StreamCodec<ByteBuf, T> {
	public static final Codec<Holder<SynchedAnimationVariableKey<?>>> CODEC = EpicFightRegistries.SYNCHED_ANIMATION_VARIABLE.holderByNameCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SynchedAnimationVariableKey<?>>> STREAM_CODEC = ByteBufCodecs.holderRegistry(EpicFightRegistries.Keys.SYNCHED_ANIMATION_VARIABLE_KEY);
	
	public static <T> SynchedSharedAnimationVariableKey<T> shared(Function<Animator, T> defaultValueSupplier, boolean mutable, StreamCodec<ByteBuf, T> codec) {
		return new SynchedSharedAnimationVariableKey<> (defaultValueSupplier, mutable, codec);
	}
	
	public static <T> SynchedIndependentAnimationVariableKey<T> independent(Function<Animator, T> defaultValueSupplier, boolean mutable, StreamCodec<ByteBuf, T> codec) {
		return new SynchedIndependentAnimationVariableKey<> (defaultValueSupplier, mutable, codec);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> SynchedAnimationVariableKey<T> byId(int id) {
		return (SynchedAnimationVariableKey<T>)SynchedAnimationVariableKeyCallbacks.getIdMap().byId(id);
	}
	
	public StreamCodec<ByteBuf, T> getCodec();
	
	@Override
	default void encode(ByteBuf buf, T val) {
		this.getCodec().encode(buf, val);
	}
	
	@Override
	default T decode(ByteBuf buf) {
		return this.getCodec().decode(buf);
	}
	
	default int getId() {
		return SynchedAnimationVariableKeyCallbacks.getIdMap().getId(this);
	}
	
	public static <T> void synchronize(
		  SynchedAnimationVariableKey<T> synchedanimationvariablekey
		, LivingEntityPatch<?> entitypatch
		, @Nullable AssetAccessor<? extends StaticAnimation> animation
		, T value
		, BiDirectionalAnimationVariable.Action action
	) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		synchedanimationvariablekey.encode(buf, value);
		Holder<SynchedAnimationVariableKey<?>> holder = EpicFightRegistries.SYNCHED_ANIMATION_VARIABLE.wrapAsHolder(synchedanimationvariablekey);
		
		BiDirectionalAnimationVariable payload = new BiDirectionalAnimationVariable(action, animation, entitypatch.getId(), holder, buf);
		
		if (entitypatch.isLogicalClient()) {
			EpicFightNetworkManager.sendToServer(payload);
		} else {
			entitypatch.sendToAllPlayersTrackingMe(payload);
		}
	}
	
	public static <T> BiDirectionalAnimationVariable createPayload(
		  SynchedAnimationVariableKey<T> synchedanimationvariablekey
		, LivingEntityPatch<?> entitypatch
		, @Nullable AssetAccessor<? extends StaticAnimation> animation
		, T value
		, BiDirectionalAnimationVariable.Action action
	) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		synchedanimationvariablekey.encode(buf, value);
		Holder<SynchedAnimationVariableKey<?>> holder = EpicFightRegistries.SYNCHED_ANIMATION_VARIABLE.wrapAsHolder(synchedanimationvariablekey);
		return new BiDirectionalAnimationVariable(action, animation, entitypatch.getId(), holder, buf);
	}
	
	public static class SynchedSharedAnimationVariableKey<T> implements SynchedAnimationVariableKey<T>, AnimationVariables.SharedVariableKey<T> {
		private final Function<Animator, T> initValueSupplier;
		private final StreamCodec<ByteBuf, T> codec;
		private final boolean mutable;
		
		protected SynchedSharedAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable, StreamCodec<ByteBuf, T> codec) {
			this.initValueSupplier = initValueSupplier;
			this.mutable = mutable;
			this.codec = codec;
		}
		
		@Override
		public StreamCodec<ByteBuf, T> getCodec() {
			return this.codec;
		}
		
		@Override
		public boolean mutable() {
			return this.mutable;
		}

		@Override
		public @NonNull T defaultValue(Animator animator) {
			return this.initValueSupplier.apply(animator);
		}
	}
	
	public static class SynchedIndependentAnimationVariableKey<T> implements SynchedAnimationVariableKey<T>, AnimationVariables.IndependentVariableKey<T> {
		private final Function<Animator, T> initValueSupplier;
		private final StreamCodec<ByteBuf, T> codec;
		private final boolean mutable;
		
		protected SynchedIndependentAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable, StreamCodec<ByteBuf, T> codec) {
			this.initValueSupplier = initValueSupplier;
			this.mutable = mutable;
			this.codec = codec;
		}
		
		@Override
		public StreamCodec<ByteBuf, T> getCodec() {
			return this.codec;
		}
		
		@Override
		public boolean mutable() {
			return this.mutable;
		}

		@Override
		public @NonNull T defaultValue(Animator animator) {
			return this.initValueSupplier.apply(animator);
		}
	}
}