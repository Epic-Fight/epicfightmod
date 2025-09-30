package yesman.epicfight.skill;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPModifySkillData;
import yesman.epicfight.network.server.SPModifySkillData;

public class SkillDataManager {
	private final Map<SkillDataKey<?>, Object> data = Maps.newHashMap();
	private final SkillContainer container;
	
	public SkillDataManager(SkillContainer container) {
		this.container = container;
	}
	
	public <T> void registerData(SkillDataKey<T> key) {
		if (this.hasData(key)) {
			throw new IllegalStateException(key + " is already registered!");
		}
		
		this.data.put(key, key.defaultValue());
	}
	
	public void transferDataTo(SkillDataManager dest) {
		dest.data.putAll(this.data);
	}
	
	public <T> void removeData(SkillDataKey<T> key) {
		this.data.remove(key);
	}
	
	public Set<SkillDataKey<?>> keySet() {
		return this.data.keySet();
	}
	
	/**
	 * Use setData() or setDataSync() which is type-safe
	 */
	@Deprecated
	public void setDataRawtype(SkillDataKey<?> key, Object data) {
		if (!this.data.containsKey(key)) {
			throw new IllegalStateException(key + " is unregistered.");
		}
		
		this.data.put(key, data);
	}
	
	public <T> void setData(SkillDataKey<T> key, T data) {
		this.setDataRawtype(key, data);
	}
	
	public <T> void setDataF(SkillDataKey<T> key, Function<T, T> dataManipulator) {
		this.setDataRawtype(key, dataManipulator.apply(this.getDataValue(key)));
	}
	
	/**
	 * Use optimized version below
	 */
	@Deprecated(forRemoval = true, since = "1.21.1")
	public <T> void setDataSync(SkillDataKey<T> key, T data, ServerPlayer player) {
		this.setData(key, data);
		this.syncServerPlayerData(key, player);
	}
	
	public <T> void setDataSync(SkillDataKey<T> key, T data) {
		this.setData(key, data);
		
		if (!this.container.getExecutor().isLogicalClient()) {
			this.syncServerPlayerData(key, this.container.getServerExecutor().getOriginal());
		} else {
			this.syncLocalPlayerData(key, this.container.getClientExecutor().getOriginal());
		}
	}
	
	/**
	 * Use optimized version below
	 */
	@Deprecated(forRemoval = true, since = "1.21.1")
	public <T> void setDataSyncF(SkillDataKey<T> key, Function<T, T> dataManipulator, ServerPlayer serverplayer) {
		this.setDataF(key, dataManipulator);
		this.syncServerPlayerData(key, serverplayer);
	}
	
	public <T> void setDataSyncF(SkillDataKey<T> key, Function<T, T> dataManipulator) {
		this.setDataF(key, dataManipulator);
		
		if (!this.container.getExecutor().isLogicalClient()) {
			this.syncServerPlayerData(key, this.container.getServerExecutor().getOriginal());
		} else {
			this.syncLocalPlayerData(key, this.container.getClientExecutor().getOriginal());
		}
	}
	
	private <T> void syncServerPlayerData(SkillDataKey<T> key, ServerPlayer serverplayer) {
		SPModifySkillData msg = new SPModifySkillData(key, this.container.getSlot(), this.getDataValue(key), serverplayer.getId());
		EpicFightNetworkManager.sendToPlayer(msg, serverplayer);
		
		if (key.syncronizeToTrackingPlayers()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, serverplayer);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private <T> void syncLocalPlayerData(SkillDataKey<T> key, LocalPlayer player) {
		CPModifySkillData msg = new CPModifySkillData(key, this.container.getSlot(), this.getDataValue(key));
		EpicFightNetworkManager.sendToServer(msg);
	}
	
	public void onTracked(EpicFightNetworkManager.PayloadBundleBuilder bundleBuilder) {
		this.data.forEach((key, val) -> {
			if (key.syncronizeToTrackingPlayers()) {
				bundleBuilder.and(new SPModifySkillData(key, this.container.getSlot(), val, this.container.executor.getOriginal().getId()));
			}
		});
	}
	
	/**
	 * Use optimized version above
	 */
	@Deprecated(forRemoval = true, since = "1.21.1")
	@OnlyIn(Dist.CLIENT)
	public <T> void setDataSync(SkillDataKey<T> key, T data, LocalPlayer player) {
		this.setData(key, data);
		this.syncLocalPlayerData(key, player);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getDataValue(SkillDataKey<T> key) {
		return (T)this.data.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getDataValueOptional(SkillDataKey<T> key) {
		return Optional.ofNullable((T)this.data.get(key));
	}
	
	public boolean hasData(SkillDataKey<?> key) {
		return this.data.containsKey(key);
	}
	
	public void clearData() {
		this.data.clear();
	}
}