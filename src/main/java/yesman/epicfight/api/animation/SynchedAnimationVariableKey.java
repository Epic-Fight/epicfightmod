package yesman.epicfight.api.animation;

import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.core.IdMapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.animation.AnimationVariables.IndependentAnimationVariableKey;
import yesman.epicfight.api.animation.AnimationVariables.SharedAnimationVariableKey;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.api.utils.datastruct.ClearableIdMapper;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPAnimationVariablePacket;
import yesman.epicfight.network.common.AnimationVariablePacket;
import yesman.epicfight.network.server.SPAnimationVariablePacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public interface SynchedAnimationVariableKey<T> {
	public static <T> SynchedSharedAnimationVariableKey<T> shared(Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> codec) {
		return new SynchedSharedAnimationVariableKey<> (defaultValueSupplier, mutable, codec);
	}
	
	public static <T> SynchedIndependentAnimationVariableKey<T> independent(Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> codec) {
		return new SynchedIndependentAnimationVariableKey<> (defaultValueSupplier, mutable, codec);
	}

    public static final ResourceLocation BY_ID_REGISTRY = EpicFightMod.identifier("variablekeytoid");
	
	public static class SynchedAnimationVariableKeyCallbacks implements IForgeRegistry.BakeCallback<SynchedAnimationVariableKey<?>>, IForgeRegistry.CreateCallback<SynchedAnimationVariableKey<?>>, IForgeRegistry.ClearCallback<SynchedAnimationVariableKey<?>> {
		private static final SynchedAnimationVariableKeyCallbacks INSTANCE = new SynchedAnimationVariableKeyCallbacks();
		
		@Override
		@SuppressWarnings("unchecked")
        public void onBake(IForgeRegistryInternal<SynchedAnimationVariableKey<?>> owner, RegistryManager stage) {
			final ClearableIdMapper<SynchedAnimationVariableKey<?>> synchedanimationvariablekeybyid = owner.getSlaveMap(BY_ID_REGISTRY, ClearableIdMapper.class);
			owner.forEach(synchedanimationvariablekeybyid::add);
        }
		
		@Override
		public void onCreate(IForgeRegistryInternal<SynchedAnimationVariableKey<?>> owner, RegistryManager stage) {
			owner.setSlaveMap(BY_ID_REGISTRY, new ClearableIdMapper<SynchedAnimationVariableKey<?>> (owner.getKeys().size()));
		}
		
		@Override
        public void onClear(IForgeRegistryInternal<SynchedAnimationVariableKey<?>> owner, RegistryManager stage) {
            owner.getSlaveMap(BY_ID_REGISTRY, ClearableIdMapper.class).clear();
        }
	}
	
	public static SynchedAnimationVariableKeyCallbacks getRegistryCallback() {
		return SynchedAnimationVariableKeyCallbacks.INSTANCE;
	}
	
	@SuppressWarnings("unchecked")
	public static IdMapper<SynchedAnimationVariableKey<?>> getIdMap() {
		return SynchedAnimationVariableKeys.REGISTRY.get().getSlaveMap(BY_ID_REGISTRY, IdMapper.class);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> SynchedAnimationVariableKey<T> byId(int id) {
		return (SynchedAnimationVariableKey<T>)getIdMap().byId(id);
	}
	
	public PacketBufferCodec<T> getPacketBufferCodec();
	
	public boolean isSharedKey();
	
	default int getId() {
		return getIdMap().getId(this);
	}
	
	default void sync(LivingEntityPatch<?> entitypatch, @Nullable AssetAccessor<? extends StaticAnimation> animation, T value, AnimationVariablePacket.Action action) {
		if (entitypatch.isLogicalClient()) {
			EpicFightNetworkManager.sendToServer(new CPAnimationVariablePacket<> (this, animation, value, action));
		} else {
			entitypatch.sendToAllPlayersTrackingMe(new SPAnimationVariablePacket<> (entitypatch, this, animation, value, action));
		}
	}
	
	public static class SynchedSharedAnimationVariableKey<T> extends SharedAnimationVariableKey<T> implements SynchedAnimationVariableKey<T> {
		private final PacketBufferCodec<T> packetBufferCodec;
		
		protected SynchedSharedAnimationVariableKey(Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> packetBufferCodec) {
			super(defaultValueSupplier, mutable);
			this.packetBufferCodec = packetBufferCodec;
		}
		
		@Override
		public boolean isSynched() {
			return true;
		}
		
		@Override
		public PacketBufferCodec<T> getPacketBufferCodec() {
			return this.packetBufferCodec;
		}
	}
	
	public static class SynchedIndependentAnimationVariableKey<T> extends IndependentAnimationVariableKey<T> implements SynchedAnimationVariableKey<T> {
		private final PacketBufferCodec<T> packetBufferCodec;
		
		protected SynchedIndependentAnimationVariableKey(Function<Animator, T> defaultValueSupplier, boolean mutable, PacketBufferCodec<T> packetBufferCodec) {
			super(defaultValueSupplier, mutable);
			this.packetBufferCodec = packetBufferCodec;
		}
		
		@Override
		public boolean isSharedKey() {
			return false;
		}
		
		@Override
		public PacketBufferCodec<T> getPacketBufferCodec() {
			return this.packetBufferCodec;
		}
	}
}