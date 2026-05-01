package yesman.epicfight.api.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;

import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.Utf8String;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.gamerule.EpicFightGameRules.ConfigurableGameRule;

public interface ByteBufCodecsExtends {
	public static final StreamCodec<ByteBuf, Void> EMPTY = new StreamCodec<> () {
		@Override
		public void encode(ByteBuf buffer, Void obj) {
		}
		
		@Override
		public Void decode(ByteBuf buffer) {
			return (Void)null;
		}
	};
	
	public static final StreamCodec<ByteBuf, Vec3> VEC3 = new StreamCodec<> () {
		@Override
		public void encode(ByteBuf buffer, Vec3 obj) {
			buffer.writeDouble(obj.x);
			buffer.writeDouble(obj.y);
			buffer.writeDouble(obj.z);
		}
		
		@Override
		public Vec3 decode(ByteBuf buffer) {
			return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		}
	};
	
	public static final StreamCodec<ByteBuf, Vec3f> VEC3F = new StreamCodec<> () {
		@Override
		public void encode(ByteBuf buffer, Vec3f obj) {
			buffer.writeFloat(obj.x);
			buffer.writeFloat(obj.y);
			buffer.writeFloat(obj.z);
		}
		
		@Override
		public Vec3f decode(ByteBuf buffer) {
			return new Vec3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
		}
	};
	
	public static <B extends ByteBuf, T> StreamCodec<B, List<T>> listOf(StreamCodec<B, T> elementCodec) {
		return new StreamCodec<> () {
			@Override
			public void encode(B buffer, List<T> obj) {
				buffer.writeInt(obj.size());
				obj.forEach(val -> elementCodec.encode(buffer, val));
			}
			
			@Override
			public List<T> decode(B buffer) {
				int size = buffer.readInt();
				List<T> list = new ArrayList<> (size);
				
				for (int i = 0; i < size; i++) {
					T obj = elementCodec.decode(buffer);
					list.add(obj);
				}
				
				return list;
			}
		};
	}
	
	public static <B extends ByteBuf, T> StreamCodec<B, List<Holder<T>>> listOfHolder(StreamCodec<B, Holder<T>> elementCodec) {
		return new StreamCodec<> () {
			@Override
			public void encode(B buffer, List<Holder<T>> obj) {
				buffer.writeInt(obj.size());
				obj.forEach(val -> elementCodec.encode(buffer, val));
			}
			
			@Override
			public List<Holder<T>> decode(B buffer) {
				int size = buffer.readInt();
				List<Holder<T>> list = new ArrayList<> (size);
				
				for (int i = 0; i < size; i++) {
					Holder<T> obj = elementCodec.decode(buffer);
					list.add(obj);
				}
				
				return list;
			}
		};
	}
	
	public static final StreamCodec<ByteBuf, AssetAccessor<? extends StaticAnimation>> ANIMATION = new StreamCodec<> () {
		@Override
		public void encode(ByteBuf buffer, AssetAccessor<? extends StaticAnimation> obj) {
			buffer.writeInt(obj.get().getId());
		}
		
		@Override
		public AssetAccessor<? extends StaticAnimation> decode(ByteBuf buffer) {
			int animationId = buffer.readInt();
			return animationId < 0 ? Animations.EMPTY_ANIMATION : AnimationManager.byId(animationId);
		}
	};
	
	public static final StreamCodec<ByteBuf, EpicFightGameRules.KeyValuePair> GAMERULE = new StreamCodec<> () {
		@Override
		public void encode(ByteBuf buffer, EpicFightGameRules.KeyValuePair obj) {
			Utf8String.write(buffer, obj.gamerule().getRuleName(), 32767);
			obj.gamerule().getRuleType().codec().encode(buffer, obj.value());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public EpicFightGameRules.KeyValuePair decode(ByteBuf buffer) {
			String ruleName = Utf8String.read(buffer, 32767);
			ConfigurableGameRule<Object, ?, ?> gamerule = (ConfigurableGameRule<Object, ?, ?>)EpicFightGameRules.GAME_RULES.get(ruleName);
			Object value = gamerule.getRuleType().codec().decode(buffer);
			
			return new EpicFightGameRules.KeyValuePair(gamerule, value);
		}
	};
	
	static final Function<Class<? extends Enum<?>>, StreamCodec<? super RegistryFriendlyByteBuf, ? extends Enum<?>>> ENUM = Util.memoize(enumType -> {
		try {
			return new EnumCodec<> (enumType);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
			throw new IllegalArgumentException(enumType.getName() + " is not a valid type for enum codec", e);
		}
	});
	
	public static <T> StreamCodec<RegistryFriendlyByteBuf, ResourceKey<T>> getResourceKey(ResourceKey<? extends Registry<T>> registryKey) {
		return new StreamCodec<> () {
			@Override
			public void encode(RegistryFriendlyByteBuf buffer, ResourceKey<T> obj) {
				buffer.writeResourceKey(obj);
			}
			
			@Override
			public ResourceKey<T> decode(RegistryFriendlyByteBuf buffer) {
				return buffer.readResourceKey(registryKey);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public static <B extends ByteBuf, T extends Enum<T>> StreamCodec<B, T> enumCodec(Class<T> enumType) {
		return (StreamCodec<B, T>)ENUM.apply(enumType);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> StreamCodec<ByteBuf, T> ofNullable(StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        return new StreamCodec<ByteBuf, T> () {
			@Override
            public T decode(ByteBuf buffer) {
                boolean isNull = buffer.readBoolean();
                
                if (isNull) {
                	return null;
                } else {
                	return ((StreamCodec<ByteBuf, T>)codec).decode(buffer);
                }
            }
            
            @Override
            public void encode(ByteBuf buffer, T object) {
            	if (object == null) {
            		buffer.writeBoolean(true);
            	} else {
            		buffer.writeBoolean(false);
            		((StreamCodec<ByteBuf, T>)codec).encode(buffer, object);
            	}
            }
        };
	}
	
	public static <T> StreamCodec<FriendlyByteBuf, TagKey<T>> tagKey(ResourceKey<Registry<T>> registry) {
		return new StreamCodec<FriendlyByteBuf, TagKey<T>> () {
			@Override
			public TagKey<T> decode(FriendlyByteBuf buffer) {
				ResourceLocation registry = buffer.readResourceLocation();
				ResourceLocation tagName = buffer.readResourceLocation();
				return TagKey.create(ResourceKey.createRegistryKey(registry), tagName);
			}
			
			@Override
			public void encode(FriendlyByteBuf buffer, TagKey<T> tagKey) {
				buffer.writeResourceLocation(tagKey.registry().location());
				buffer.writeResourceLocation(tagKey.location());
			}
		};
	}
	
	static final Function<ExtensibleEnumManager<? extends ExtensibleEnum>, StreamCodec<? super RegistryFriendlyByteBuf, ? extends ExtensibleEnum>> EXTENSIBLE_ENUM = Util.memoize(enumManager -> {
		return new StreamCodec<> () {
			@Override
			public void encode(RegistryFriendlyByteBuf buffer, ExtensibleEnum obj) {
				buffer.writeInt(obj.universalOrdinal());
			}
			
			@Override
			public ExtensibleEnum decode(RegistryFriendlyByteBuf buffer) {
				return enumManager.getOrThrow(buffer.readInt());
			}
		};
	});
	
	@SuppressWarnings("unchecked")
	public static <B extends ByteBuf, T extends ExtensibleEnum> StreamCodec<B, T> extendableEnumCodec(ExtensibleEnumManager<T> enumManager) {
		return (StreamCodec<B, T>)EXTENSIBLE_ENUM.apply(enumManager);
	}
	
	static class EnumCodec<B extends ByteBuf, T extends Enum<T>> implements StreamCodec<B, T> {
		private final T[] enumValues;
		
		@SuppressWarnings("unchecked")
		private EnumCodec(Class<? extends Enum<?>> enumType) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
			Method valuesInvoker = enumType.getMethod("values");
			this.enumValues = (T[]) valuesInvoker.invoke(null);
		}
		
		@Override
		public T decode(B buffer) {
			return this.enumValues[buffer.readInt()];
		}
		
		@Override
		public void encode(B buffer, T value) {
			buffer.writeInt(value.ordinal());
		}
	}
	
	static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite7(
        final StreamCodec<? super B, T1> codec1,
        final Function<C, T1> getter1,
        final StreamCodec<? super B, T2> codec2,
        final Function<C, T2> getter2,
        final StreamCodec<? super B, T3> codec3,
        final Function<C, T3> getter3,
        final StreamCodec<? super B, T4> codec4,
        final Function<C, T4> getter4,
        final StreamCodec<? super B, T5> codec5,
        final Function<C, T5> getter5,
        final StreamCodec<? super B, T6> codec6,
        final Function<C, T6> getter6,
        final StreamCodec<? super B, T7> codec7,
        final Function<C, T7> getter7,
        final Function7<T1, T2, T3, T4, T5, T6, T7, C> factory
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_330310_) {
                T1 t1 = codec1.decode(p_330310_);
                T2 t2 = codec2.decode(p_330310_);
                T3 t3 = codec3.decode(p_330310_);
                T4 t4 = codec4.decode(p_330310_);
                T5 t5 = codec5.decode(p_330310_);
                T6 t6 = codec6.decode(p_330310_);
                T7 t7 = codec7.decode(p_330310_);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7);
            }

            @Override
            public void encode(B p_332052_, C p_331912_) {
                codec1.encode(p_332052_, getter1.apply(p_331912_));
                codec2.encode(p_332052_, getter2.apply(p_331912_));
                codec3.encode(p_332052_, getter3.apply(p_331912_));
                codec4.encode(p_332052_, getter4.apply(p_331912_));
                codec5.encode(p_332052_, getter5.apply(p_331912_));
                codec6.encode(p_332052_, getter6.apply(p_331912_));
                codec7.encode(p_332052_, getter7.apply(p_331912_));
            }
        };
    }
	
	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite8(
        final StreamCodec<? super B, T1> codec1,
        final Function<C, T1> getter1,
        final StreamCodec<? super B, T2> codec2,
        final Function<C, T2> getter2,
        final StreamCodec<? super B, T3> codec3,
        final Function<C, T3> getter3,
        final StreamCodec<? super B, T4> codec4,
        final Function<C, T4> getter4,
        final StreamCodec<? super B, T5> codec5,
        final Function<C, T5> getter5,
        final StreamCodec<? super B, T6> codec6,
        final Function<C, T6> getter6,
        final StreamCodec<? super B, T7> codec7,
        final Function<C, T7> getter7,
        final StreamCodec<? super B, T8> codec8,
        final Function<C, T8> getter8,
        final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> factory
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_330310_) {
                T1 t1 = codec1.decode(p_330310_);
                T2 t2 = codec2.decode(p_330310_);
                T3 t3 = codec3.decode(p_330310_);
                T4 t4 = codec4.decode(p_330310_);
                T5 t5 = codec5.decode(p_330310_);
                T6 t6 = codec6.decode(p_330310_);
                T7 t7 = codec7.decode(p_330310_);
                T8 t8 = codec8.decode(p_330310_);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }

            @Override
            public void encode(B p_332052_, C p_331912_) {
                codec1.encode(p_332052_, getter1.apply(p_331912_));
                codec2.encode(p_332052_, getter2.apply(p_331912_));
                codec3.encode(p_332052_, getter3.apply(p_331912_));
                codec4.encode(p_332052_, getter4.apply(p_331912_));
                codec5.encode(p_332052_, getter5.apply(p_331912_));
                codec6.encode(p_332052_, getter6.apply(p_331912_));
                codec7.encode(p_332052_, getter7.apply(p_331912_));
                codec8.encode(p_332052_, getter8.apply(p_331912_));
            }
        };
    }
	
	static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite9(
        final StreamCodec<? super B, T1> codec1,
        final Function<C, T1> getter1,
        final StreamCodec<? super B, T2> codec2,
        final Function<C, T2> getter2,
        final StreamCodec<? super B, T3> codec3,
        final Function<C, T3> getter3,
        final StreamCodec<? super B, T4> codec4,
        final Function<C, T4> getter4,
        final StreamCodec<? super B, T5> codec5,
        final Function<C, T5> getter5,
        final StreamCodec<? super B, T6> codec6,
        final Function<C, T6> getter6,
        final StreamCodec<? super B, T7> codec7,
        final Function<C, T7> getter7,
        final StreamCodec<? super B, T8> codec8,
        final Function<C, T8> getter8,
        final StreamCodec<? super B, T9> codec9,
        final Function<C, T9> getter9,
        final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> factory
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B p_330310_) {
                T1 t1 = codec1.decode(p_330310_);
                T2 t2 = codec2.decode(p_330310_);
                T3 t3 = codec3.decode(p_330310_);
                T4 t4 = codec4.decode(p_330310_);
                T5 t5 = codec5.decode(p_330310_);
                T6 t6 = codec6.decode(p_330310_);
                T7 t7 = codec7.decode(p_330310_);
                T8 t8 = codec8.decode(p_330310_);
                T9 t9 = codec9.decode(p_330310_);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
            }
            
            @Override
            public void encode(B p_332052_, C p_331912_) {
                codec1.encode(p_332052_, getter1.apply(p_331912_));
                codec2.encode(p_332052_, getter2.apply(p_331912_));
                codec3.encode(p_332052_, getter3.apply(p_331912_));
                codec4.encode(p_332052_, getter4.apply(p_331912_));
                codec5.encode(p_332052_, getter5.apply(p_331912_));
                codec6.encode(p_332052_, getter6.apply(p_331912_));
                codec7.encode(p_332052_, getter7.apply(p_331912_));
                codec8.encode(p_332052_, getter8.apply(p_331912_));
                codec9.encode(p_332052_, getter9.apply(p_331912_));
            }
        };
    }
}
