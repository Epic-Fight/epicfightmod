package yesman.epicfight.api.animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashMultimap;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.datastructure.ParameterizedHashMap;
import yesman.epicfight.api.utils.datastructure.ParameterizedMap.ParameterizedKey;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable.Action;

public class AnimationVariables {
	protected final Animator animator;
	protected final ParameterizedHashMap<AnimationVariableKey<?>> animationVariables = new ParameterizedHashMap<> ();
	protected final HashMultimap<AssetAccessor<? extends StaticAnimation>, PendingData<?>> pendingIndependentVariables = HashMultimap.create();
	
	public AnimationVariables(Animator animator) {
		this.animator = animator;
	}
	
	/**
	 * Return a value of shared variable key, or null
	 */
	public <T> Optional<T> getSharedVariable(SharedVariableKey<T> key) {
		return Optional.ofNullable(this.animationVariables.get(key));
	}
	
	/**
	 * Return a value of shared variable key, or return the default value specified in key declaration
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOrDefaultSharedVariable(SharedVariableKey<T> key) {
		return ParseUtil.orElse((T)this.animationVariables.get(key), () -> key.defaultValue(this.animator));
	}
	
	/**
	 * Return a value of independent variable key for an animation, or null
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(IndependentVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
		if (animation == null) {
			return Optional.empty();
		}
		
		Map<ResourceLocation, Object> subMap = this.animationVariables.get(key);
		
		if (subMap == null) {
			return Optional.empty();
		} else {
			return Optional.ofNullable((T)subMap.get(animation.registryName()));
		}
	}
	
	/**
	 * Return a value of independent variable key for an animation, or return the default value specified in key declaration
	 */
	@SuppressWarnings("unchecked")
	public <T> T getOrDefault(IndependentVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
		if (animation == null) {
			return Objects.requireNonNull(key.defaultValue(this.animator), "Null value returned by default provider.");
		}
		
		Map<ResourceLocation, Object> subMap = this.animationVariables.get(key);
		
		if (subMap == null) {
			return Objects.requireNonNull(key.defaultValue(this.animator), "Null value returned by default provider.");
		} else {
			return ParseUtil.orElse((T)subMap.get(animation.registryName()), () -> key.defaultValue(this.animator));
		}
	}
	
	/**
	 * Put a shared variable key and its default value
	 */
	public <T> void putSharedVariableWithDefault(SharedVariableKey<T> key) {
		T value = key.defaultValue(this.animator);
		Objects.requireNonNull(value, "Null value returned by default provider.");
		
		this.putSharedVariable(key, value);
	}
	
	/**
	 * Put a shared variable key and a value
	 */
	public <T> void putSharedVariable(SharedVariableKey<T> key, T value) {
		this.putSharedVariable(key, value, true);
	}
	
	@SuppressWarnings("unchecked")
	@ApiStatus.Internal // Avoid using directly
	public <T> void putSharedVariable(SharedVariableKey<T> key, T value, boolean synchronize) {
		if (this.animationVariables.containsKey(key) && !key.mutable()) {
			throw new UnsupportedOperationException("Can't modify a const variable");
		}
		
		this.animationVariables.put(key, value);
		
		if (synchronize && key.isSynched()) {
			SynchedAnimationVariableKey.synchronize((SynchedAnimationVariableKey<T>)key, this.animator.entitypatch, null, value, BiDirectionalAnimationVariable.Action.PUT);
		}
	}
	
	/**
	 * Put an independent variable key for an animation and its default value
	 */
	public <T> void putDefaultValue(IndependentVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
		T value = key.defaultValue(this.animator);
		Objects.requireNonNull(value, "Null value returned by default provider.");
		
		this.put(key, animation, value);
	}
	
	/**
	 * Put an independent variable key for an animation and a value
	 */
	public <T> void put(IndependentVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation, T value) {
		this.put(key, animation, value, true);
	}
	
	@SuppressWarnings("unchecked")
	@ApiStatus.Internal // Avoid using directly
	public <T> void put(IndependentVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation, T value, boolean synchronize) {
		if (animation == Animations.EMPTY_ANIMATION) {
			return;
		}
		
		this.animationVariables.computeIfPresent(key, (k, v) -> {
			Map<ResourceLocation, Object> variablesByAnimations = ((Map<ResourceLocation, Object>)v);
			
			if (!key.mutable() && variablesByAnimations.containsKey(animation.registryName())) {
				throw new UnsupportedOperationException("Can't modify a const variable");
			}
			
			variablesByAnimations.put(animation.registryName(), value);
			
			return v;
		});
		
		this.animationVariables.computeIfAbsent(key, (k) -> {
			return new HashMap<> (Map.of(animation.registryName(), value));
		});
		
		if (synchronize && key.isSynched()) {
			if (this.animator.isPlaying(animation)) {
				SynchedAnimationVariableKey.synchronize((SynchedAnimationVariableKey<T>)key, this.animator.entitypatch, animation, value, BiDirectionalAnimationVariable.Action.PUT);
			} else {
				this.pendingIndependentVariables.put(animation, new PendingData<> ((SynchedAnimationVariableKey<T>)key, value));
			}
		}
	}
	
	/**
	 * Remove the value of a shared variable key
	 */
	public <T> T removeSharedVariable(SharedVariableKey<T> key) {
		return this.removeSharedVariable(key, true);
	}
	
	@SuppressWarnings("unchecked")
	@ApiStatus.Internal // Avoid using directly
	public <T> T removeSharedVariable(SharedVariableKey<T> key, boolean synchronize) {
		if (!key.mutable()) {
			throw new UnsupportedOperationException("Can't remove a const variable");
		}
		
		if (synchronize && key.isSynched()) {
			SynchedAnimationVariableKey.synchronize((SynchedAnimationVariableKey<T>)key, this.animator.entitypatch, null, null, BiDirectionalAnimationVariable.Action.REMOVE);
		}
		
		return (T)this.animationVariables.remove(key);
	}
	
	/**
	 * Remove all animation variables belong to an animation
	 */
	@SuppressWarnings("unchecked")
	@ApiStatus.Internal // Avoid using directly
	public void removeAll(AnimationAccessor<? extends StaticAnimation> animation) {
		if (animation == Animations.EMPTY_ANIMATION) {
			return;
		}
		
		for (Map.Entry<? extends AnimationVariableKey<?>, Object> entry : this.animationVariables.entrySet()) {
			if (entry.getKey().isSharedKey()) {
				continue;
			}
			
			Map<ResourceLocation, Object> map = (Map<ResourceLocation, Object>)entry.getValue();
			
			if (map != null) {
				map.remove(animation.registryName());
			}
		}
		
		this.pendingIndependentVariables.removeAll(animation);
	}
	
	/**
	 * Remove an independent variable for an animation
	 */
	public void remove(IndependentVariableKey<?> key, AssetAccessor<? extends StaticAnimation> animation) {
		this.remove(key, animation, true);
	}
	
	@SuppressWarnings("unchecked")
	@ApiStatus.Internal // Avoid using directly
	public void remove(IndependentVariableKey<?> key, AssetAccessor<? extends StaticAnimation> animation, boolean synchronize) {
		if (animation == Animations.EMPTY_ANIMATION) {
			return;
		}
		
		if (!key.mutable()) {
			throw new UnsupportedOperationException("Can't remove a const variable");
		}
		
		Map<ResourceLocation, Object> map = (Map<ResourceLocation, Object>)this.animationVariables.get(key);
		
		if (map != null) {
			map.remove(animation.registryName());
		}
		
		if (synchronize && key.isSynched()) {
			SynchedAnimationVariableKey.synchronize((SynchedAnimationVariableKey<?>)key, this.animator.entitypatch, null, null, BiDirectionalAnimationVariable.Action.REMOVE);
			this.pendingIndependentVariables.remove(animation, key);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<BiDirectionalAnimationVariable> createPendingVariablesPayloads(AssetAccessor<? extends StaticAnimation> animation) {
		Set<PendingData<?>> pendingdata = this.pendingIndependentVariables.removeAll(animation);
		return pendingdata.stream().map(pair -> SynchedAnimationVariableKey.createPayload((SynchedAnimationVariableKey<Object>)pair.key(), this.animator.getEntityPatch(), animation, pair.value(), Action.PUT)).toList();
	}
	
	// Created unsynched shared key
	public static <T> SharedVariableKey<T> unsynchShared(Function<Animator, T> defaultValueSupplier, boolean mutable) {
		return new UnsynchedSharedAnimationVariableKey<> (defaultValueSupplier, mutable);
	}
	
	// Created unsynched independent key
	public static <T> IndependentVariableKey<T> unsyncIndependent(Function<Animator, T> defaultValueSupplier, boolean mutable) {
		return new UnsynchedIndependentAnimationVariableKey<> (defaultValueSupplier, mutable);
	}
	
	public interface AnimationVariableKey<T> extends ParameterizedKey<T> {
		public boolean mutable();
		
		@Override
		default T defaultValue() {
			throw new UnsupportedOperationException("Use defaultValue(Animator) to get default value of animation variable key");
		}
		
		@NonNull
		public T defaultValue(Animator animator);
		
		default boolean isSharedKey() {
			return this instanceof SharedVariableKey;
		}
		
		default boolean isSynched() {
			return this instanceof SynchedAnimationVariableKey;
		}
	}
	
	/**
	 * Shared variables will alive until you remove the value explicitly.
	 */
	public interface SharedVariableKey<T> extends AnimationVariableKey<T> {
	}
	
	/**
	 * Independent variables will alive until the specified animation ends. And can't access from other animations
	 */
	public interface IndependentVariableKey<T> extends AnimationVariableKey<T> {
	}
	
	/**
	 * Unsynchronized between server and client
	 */
	public static class UnsynchedSharedAnimationVariableKey<T> implements SharedVariableKey<T> {
		private final Function<Animator, T> initValueSupplier;
		private final boolean mutable;
		
		protected UnsynchedSharedAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable) {
			this.initValueSupplier = initValueSupplier;
			this.mutable = mutable;
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
	
	public static class UnsynchedIndependentAnimationVariableKey<T> implements IndependentVariableKey<T> {
		private final Function<Animator, T> initValueSupplier;
		private final boolean mutable;
		
		protected UnsynchedIndependentAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable) {
			this.initValueSupplier = initValueSupplier;
			this.mutable = mutable;
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
	
	public static record PendingData<T>(SynchedAnimationVariableKey<T> key, T value) {
		@Override
		public boolean equals(Object object) {
			if (object instanceof SynchedAnimationVariableKey<?> synchedDataKey) {
				return this.key.equals(synchedDataKey);
			}
			
			if (object instanceof PendingData<?> pendingData) {
				return this.key.equals(pendingData.key());
			}
			
			return false;
		}
	}
}
