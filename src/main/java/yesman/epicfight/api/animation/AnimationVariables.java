package yesman.epicfight.api.animation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.checkerframework.checker.nullness.qual.NonNull;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap.TypeKey;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.common.AnimationVariablePacket;

public class AnimationVariables {
	protected final Animator animator;
	protected final TypeFlexibleHashMap<AnimationVariableKey<?>> animationVariables = new TypeFlexibleHashMap<> (false);
	
	public AnimationVariables(Animator animator) {
		this.animator = animator;
	}
	
	public <T> Optional<T> getSharedVariable(SharedAnimationVariableKey<T> key) {
		return Optional.ofNullable(this.animationVariables.get(key));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOrDefaultSharedVariable(SharedAnimationVariableKey<T> key) {
		return ParseUtil.orElse((T)this.animationVariables.get(key), () -> key.defaultValue(this.animator));
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
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
	
	@SuppressWarnings("unchecked")
	public <T> T getOrDefault(IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
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
	
	public <T> void putDefaultSharedVariable(SharedAnimationVariableKey<T> key) {
		T value = key.defaultValue(this.animator);
		Objects.requireNonNull(value, "Null value returned by default provider.");
		
		this.putSharedVariable(key, value);
	}
	
	public <T> void putSharedVariable(SharedAnimationVariableKey<T> key, T value) {
		this.putSharedVariable(key, value, true);
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated // Avoid direct use
	public <T> void putSharedVariable(SharedAnimationVariableKey<T> key, T value, boolean synchronize) {
		if (this.animationVariables.containsKey(key) && !key.mutable()) {
			throw new UnsupportedOperationException("Can't modify a const variable");
		}
		
		this.animationVariables.put((AnimationVariableKey<?>)key, value);
		
		if (synchronize && key instanceof SynchedAnimationVariableKey) {
			SynchedAnimationVariableKey<T> synchedanimationvariablekey = (SynchedAnimationVariableKey<T>)key;
			synchedanimationvariablekey.sync(this.animator.entitypatch, (AssetAccessor<? extends StaticAnimation>)null, value, AnimationVariablePacket.Action.PUT);
		}
	}
	
	public <T> void putDefaultValue(IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation) {
		T value = key.defaultValue(this.animator);
		Objects.requireNonNull(value, "Null value returned by default provider.");
		
		this.put(key, animation, value);
	}
	
	public <T> void put(IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation, T value) {
		this.put(key, animation, value, true);
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated // Avoid direct use
	public <T> void put(IndependentAnimationVariableKey<T> key, AssetAccessor<? extends StaticAnimation> animation, T value, boolean synchronize) {
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
		
		if (synchronize && key instanceof SynchedAnimationVariableKey) {
			SynchedAnimationVariableKey<T> synchedanimationvariablekey = (SynchedAnimationVariableKey<T>)key;
			synchedanimationvariablekey.sync(this.animator.entitypatch, animation, value, AnimationVariablePacket.Action.PUT);
		}
	}
	
	public <T> T removeSharedVariable(SharedAnimationVariableKey<T> key) {
		return this.removeSharedVariable(key, true);
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated // Avoid direct use
	public <T> T removeSharedVariable(SharedAnimationVariableKey<T> key, boolean synchronize) {
		if (!key.mutable()) {
			throw new UnsupportedOperationException("Can't remove a const variable");
		}
		
		if (synchronize && key instanceof SynchedAnimationVariableKey) {
			SynchedAnimationVariableKey<T> synchedanimationvariablekey = (SynchedAnimationVariableKey<T>)key;
			synchedanimationvariablekey.sync(this.animator.entitypatch, null, null, AnimationVariablePacket.Action.REMOVE);
		}
		
		return (T)this.animationVariables.remove(key);
	}
	
	@SuppressWarnings("unchecked")
	public void removeAll(AnimationAccessor<? extends StaticAnimation> animation) {
		if (animation == Animations.EMPTY_ANIMATION) {
			return;
		}
		
		for (Map.Entry<AnimationVariableKey<?>, Object> entry : this.animationVariables.entrySet()) {
			if (entry.getKey().isSharedKey()) {
				continue;
			}
			
			Map<ResourceLocation, Object> map = (Map<ResourceLocation, Object>)entry.getValue();
			
			if (map != null) {
				map.remove(animation.registryName());
			}
		}
	}
	
	public void remove(IndependentAnimationVariableKey<?> key, AssetAccessor<? extends StaticAnimation> animation) {
		this.remove(key, animation, true);
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated // Avoid direct use
	public void remove(IndependentAnimationVariableKey<?> key, AssetAccessor<? extends StaticAnimation> animation, boolean synchronize) {
		if (animation == Animations.EMPTY_ANIMATION) {
			return;
		}
		
		Map<ResourceLocation, Object> map = (Map<ResourceLocation, Object>)this.animationVariables.get(key);
		
		if (map != null) {
			map.remove(animation.registryName());
		}
		
		if (synchronize && key instanceof SynchedAnimationVariableKey) {
			SynchedAnimationVariableKey<?> synchedanimationvariablekey = (SynchedAnimationVariableKey<?>)key;
			synchedanimationvariablekey.sync(this.animator.entitypatch, null, null, AnimationVariablePacket.Action.REMOVE);
		}
	}
	
	public static <T> SharedAnimationVariableKey<T> shared(Function<Animator, T> defaultValueSupplier, boolean mutable) {
		return new SharedAnimationVariableKey<> (defaultValueSupplier, mutable);
	}
	
	public static <T> IndependentAnimationVariableKey<T> independent(Function<Animator, T> defaultValueSupplier, boolean mutable) {
		return new IndependentAnimationVariableKey<> (defaultValueSupplier, mutable);
	}
	
	protected abstract static class AnimationVariableKey<T> implements TypeKey<T> {
		protected final Function<Animator, T> defaultValueSupplier;
		protected final boolean mutable;
		
		protected AnimationVariableKey(Function<Animator, T> defaultValueSupplier, boolean mutable) {
			this.defaultValueSupplier = defaultValueSupplier;
			this.mutable = mutable;
		}
		
		@NonNull
		public T defaultValue(Animator animator) {
			return this.defaultValueSupplier.apply(animator);
		}
		
		public boolean mutable() {
			return this.mutable;
		}
		
		@Override
		public T defaultValue() {
			throw new UnsupportedOperationException("Use defaultValue(Animator animator) to get default value of animation variable key");
		}
		
		public abstract boolean isSharedKey();
		public abstract boolean isSynched(); 
	}
	
	public static class SharedAnimationVariableKey<T> extends AnimationVariableKey<T> {
		protected SharedAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable) {
			super(initValueSupplier, mutable);
		}
		
		@Override
		public boolean isSharedKey() {
			return true;
		}
		
		@Override
		public boolean isSynched() {
			return false;
		}
	}
	
	public static class IndependentAnimationVariableKey<T> extends AnimationVariableKey<T> {
		protected IndependentAnimationVariableKey(Function<Animator, T> initValueSupplier, boolean mutable) {
			super(initValueSupplier, mutable);
		}
		
		@Override
		public boolean isSharedKey() {
			return false;
		}
		
		@Override
		public boolean isSynched() {
			return false;
		}
	}
}
