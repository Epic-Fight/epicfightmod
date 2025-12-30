package yesman.epicfight.skill;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPHandleSkillData;
import yesman.epicfight.network.server.SPHandleSkillData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class SkillDataManager {
	private final Map<Holder<SkillDataKey<?>>, Object> data = new HashMap<> ();
	private final SkillContainer container;
	
	public SkillDataManager(SkillContainer container) {
		this.container = container;
	}
	
	public void registerData(Holder<SkillDataKey<?>> key) {
		if (this.hasData(key)) {
			throw new IllegalStateException(key + " is already registered!");
		}
		
		this.data.put(key, key.value().defaultValue());
	}
	
	public void transferDataTo(SkillDataManager dest) {
		dest.data.putAll(this.data);
	}
	
	public void removeData(Holder<SkillDataKey<?>> key) {
		this.data.remove(key);
	}
	
	public Set<Holder<SkillDataKey<?>>> keySet() {
		return this.data.keySet();
	}
	
	/**
	 * Use setData() or setDataSync() which is type-safe
	 */
	@ApiStatus.Internal
	public void setDataRawtype(Holder<SkillDataKey<?>> key, Object data) {
		if (!this.data.containsKey(key)) {
			throw new IllegalStateException(key + " is unregistered.");
		}
		
		this.data.put(key, data);
	}
	
	public <T> void setData(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key, T data) {
		this.setDataRawtype(key, data);
	}
	
	public <T> void setDataF(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key, Function<T, T> dataMapper) {
		this.setDataRawtype(key, dataMapper.apply(this.getDataValue(key)));
	}
	
	public <T> void setDataSync(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key, T data) {
		this.setData(key, data);
		
		if (!this.container.getExecutor().isLogicalClient()) {
			this.syncServerPlayerData(key, this.container.getServerExecutor().getOriginal());
		} else {
			this.syncLocalPlayerData(key, this.container.getClientExecutor().getOriginal());
		}
	}
	
	public <T> void setDataSyncF(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key, Function<T, T> dataManipulator) {
		this.setDataF(key, dataManipulator);
		
		if (!this.container.getExecutor().isLogicalClient()) {
			this.syncServerPlayerData(key, this.container.getServerExecutor().getOriginal());
		} else {
			this.syncLocalPlayerData(key, this.container.getClientExecutor().getOriginal());
		}
	}
	
	private <T> void syncServerPlayerData(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key, ServerPlayer serverplayer) {
		SPHandleSkillData msg = new SPHandleSkillData(SPHandleSkillData.WorkType.MODIFY, this.container.getSlot(), serverplayer.getId(), key);
		key.value().encode(msg.buffer(), this.getDataValue(key));
		EpicFightNetworkManager.sendToPlayer(msg, serverplayer);
		
		if (key.value().syncronizeToRemotePlayers()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, serverplayer);
		}
	}

    @ClientOnly
	private <T> void syncLocalPlayerData(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key, LocalPlayer player) {
		CPHandleSkillData msg = new CPHandleSkillData(this.container.getSlot(), key);
		key.value().encode(msg.buffer(), this.getDataValue(key));
		EpicFightNetworkManager.sendToServer(msg);
	}
	
	@SuppressWarnings("unchecked")
	public void onTracked(EpicFightNetworkManager.PayloadBundleBuilder bundleBuilder) {
		this.data.forEach((key, val) -> {
			if (key.value().syncronizeToRemotePlayers()) {
				SPHandleSkillData msg = new SPHandleSkillData(SPHandleSkillData.WorkType.MODIFY, this.container.getSlot(), this.container.executor.getOriginal().getId(), key);
				((SkillDataKey<Object>)key.value()).encode(msg.buffer(), val);
				
				bundleBuilder.and(msg);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getDataValue(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key) {
		return this.hasData(key) ? (T)this.data.get(key) : null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getDataValueOptional(DeferredHolder<SkillDataKey<?>, ? extends SkillDataKey<T>> key) {
		return Optional.ofNullable((T)this.data.get(key));
	}
	
	@ApiStatus.Internal
	public Object getRawDataValue(Holder<SkillDataKey<?>> key) {
		return this.data.get(key);
	}
	
	public boolean hasData(Holder<SkillDataKey<?>> key) {
		return this.data.containsKey(key);
	}
	
	public void clearData() {
		this.data.clear();
	}
}