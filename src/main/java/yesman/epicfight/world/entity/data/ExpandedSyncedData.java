package yesman.epicfight.world.entity.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.registries.DeferredHolder;
import yesman.epicfight.api.utils.datastructure.ParameterizedHolderHashMap;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.EpicFightNetworkManager.PayloadBundleBuilder;
import yesman.epicfight.network.server.SPModifyExpandedEntityData;

public final class ExpandedSyncedData {
	private final Set<Holder<ExpandedEntityDataAccessor<?>>> registeredKeys = new HashSet<> ();
	private final ParameterizedHolderHashMap<ExpandedEntityDataAccessor<?>> dataMap = new ParameterizedHolderHashMap<> ();
	private final Map<Holder<ExpandedEntityDataAccessor<?>>, Object> pendingDirtyData = new HashMap<> ();
	private final Supplier<Integer> entityIdProvider;
	private final boolean isLogicalServer;
	
	public ExpandedSyncedData(Supplier<Integer> entityIdProvider, boolean isServer) {
		this.entityIdProvider = entityIdProvider;
		this.isLogicalServer = isServer;
	}
	
	public void register(Holder<ExpandedEntityDataAccessor<?>> key) {
		this.registeredKeys.add(key);
		this.dataMap.put(key, key.value().defaultValue());
	}
	
	@ApiStatus.Internal
	public void load(CompoundTag compound) {
		CompoundTag synchedDataCompound = compound.getCompound("expanded_sycned_data");
		
		if (synchedDataCompound != null) {
			MapLike<Tag> compoundConverted = NbtOps.INSTANCE.getMap(synchedDataCompound).result().orElseThrow();
			
			this.registeredKeys.stream().forEach(holder -> {
				Object data = holder.value().readFromTag(compoundConverted);
				
				if (data != null) {
					this.dataMap.put(holder, data);
					this.pendingDirtyData.put(holder, data);
				}
			});
		}
	}
	
	@SuppressWarnings("unchecked")
	@ApiStatus.Internal
	public void saveData(CompoundTag compound) {
		RecordBuilder<Tag> recordBuilder = NbtOps.INSTANCE.mapBuilder();
		
		
		this.dataMap.forEach((k, v) -> {
			((ExpandedEntityDataAccessor<Object>)k.value()).saveData(v, recordBuilder);
		});
		
		recordBuilder.build(new CompoundTag()).result().ifPresent(result -> {
			compound.put("expanded_sycned_data", result);
		});
	}
	
	@ApiStatus.Internal
	public <T> void setRaw(Holder<ExpandedEntityDataAccessor<?>> key, @NonNull Object val) {
		if (!this.registeredKeys.contains(key)) {
			throw new IllegalArgumentException("Unregistered key " + key.getRegisteredName());
		}
		
		this.dataMap.put(key, val);
	}
	
	public <T> void set(DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<T>> key, @NonNull T val) {
		if (!this.registeredKeys.contains(key)) {
			throw new IllegalArgumentException("Unregistered key " + key.getRegisteredName());
		}
		
		this.dataMap.put(key, val);
		
		if (this.isLogicalServer) {
			this.pendingDirtyData.put(key, val);
		}
	}
	
	public <T> T get(DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<T>> key) {
		if (!this.registeredKeys.contains(key)) {
			throw new IllegalArgumentException("Unregistered key " + key.getRegisteredName());
		}
		
		return this.dataMap.get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public EpicFightNetworkManager.PayloadBundleBuilder prepareDataToSend() {
		if (this.pendingDirtyData.isEmpty()) {
			return null;
		}
		
		PayloadBundleBuilder payloadBundleBuilder = PayloadBundleBuilder.create();
		
		for (Map.Entry<Holder<ExpandedEntityDataAccessor<?>>, Object> dirtyData : this.pendingDirtyData.entrySet()) {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			((ExpandedEntityDataAccessor<Object>)dirtyData.getKey().value()).streamCodec().encode(buf, dirtyData.getValue());
			SPModifyExpandedEntityData payload = new SPModifyExpandedEntityData(this.entityIdProvider.get(), (Holder<ExpandedEntityDataAccessor<?>>)dirtyData.getKey(), buf);
			payloadBundleBuilder.and(payload);
		}
		
		this.pendingDirtyData.clear();
		
		return payloadBundleBuilder;
	}
}
