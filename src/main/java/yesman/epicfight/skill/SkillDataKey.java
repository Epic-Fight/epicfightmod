package yesman.epicfight.skill;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.IdMapper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.api.utils.datastruct.ClearableIdMapper;
import yesman.epicfight.main.EpicFightMod;

public class SkillDataKey<T> {
	private static final HashMultimap<Class<?>, SkillDataKey<?>> SKILL_DATA_KEYS = HashMultimap.create();
    private static final ResourceLocation CLASS_TO_DATA_KEYS = EpicFightMod.identifier("classtodatakeys");
    private static final ResourceLocation DATA_KEY_TO_ID = EpicFightMod.identifier("datakeytoid");
	
	private static class SkillDataKeyCallbacks implements IForgeRegistry.BakeCallback<SkillDataKey<?>>, IForgeRegistry.CreateCallback<SkillDataKey<?>>, IForgeRegistry.ClearCallback<SkillDataKey<?>> {
		static final SkillDataKeyCallbacks INSTANCE = new SkillDataKeyCallbacks();
		
		@Override
		@SuppressWarnings("unchecked")
        public void onBake(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
			final ClearableIdMapper<SkillDataKey<?>> skillDataKeyMap = owner.getSlaveMap(DATA_KEY_TO_ID, ClearableIdMapper.class);
			owner.forEach(skillDataKeyMap::add);
			
			final Map<Class<?>, Set<SkillDataKey<?>>> skillDataKeys = owner.getSlaveMap(CLASS_TO_DATA_KEYS, Map.class);
			
			SkillManager.getSkillRegistry().forEach((skill) -> {
				Class<?> skillClass = skill.getClass();
				Set<SkillDataKey<?>> dataKeySet = Sets.newHashSet();
				skillDataKeys.put(skillClass, dataKeySet);
				
				do {
					if (SKILL_DATA_KEYS.containsKey(skillClass)) {
						dataKeySet.addAll(SKILL_DATA_KEYS.get(skillClass));
					}
					
					skillClass = skillClass.getSuperclass();
				} while (Skill.class.isAssignableFrom(skillClass));
				
				if (!dataKeySet.isEmpty()) {
					EpicFightMod.LOGGER.info("Data keys "  + dataKeySet.stream().map(SkillDataKeys.REGISTRY.get()::getKey).toList() + " for " + skill.getRegistryName());
				}
			});
        }
		
		@Override
		public void onCreate(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
			owner.setSlaveMap(CLASS_TO_DATA_KEYS, Maps.newHashMap());
			owner.setSlaveMap(DATA_KEY_TO_ID, new ClearableIdMapper<SkillDataKey<?>> (owner.getKeys().size()));
		}
		
		@Override
        public void onClear(IForgeRegistryInternal<SkillDataKey<?>> owner, RegistryManager stage) {
			owner.getSlaveMap(CLASS_TO_DATA_KEYS, Map.class).clear();
            owner.getSlaveMap(DATA_KEY_TO_ID, ClearableIdMapper.class).clear();
        }
	}
	
	public static SkillDataKeyCallbacks getRegistryCallback() {
		return SkillDataKeyCallbacks.INSTANCE;
	}
	
	public static <T> SkillDataKey<T> createSkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, Class<?>... skillClass) {
		return createSkillDataKey(packetCodec, defaultValue, false, skillClass);
	}
	
	public static <T> SkillDataKey<T> createSkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers, Class<?>... skillClass) {
		SkillDataKey<T> key = new SkillDataKey<T> (packetCodec, defaultValue, syncronizeTrackingPlayers);
		
		for (Class<?> cls : skillClass) {
			SKILL_DATA_KEYS.put(cls, key);
		}
		
		return key;
	}
	
	@SuppressWarnings("unchecked")
	public static IdMapper<SkillDataKey<?>> getIdMap() {
		return SkillDataKeys.REGISTRY.get().getSlaveMap(DATA_KEY_TO_ID, IdMapper.class);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<Class<?>, Set<SkillDataKey<?>>> getSkillDataKeyMap() {
		return SkillDataKeys.REGISTRY.get().getSlaveMap(CLASS_TO_DATA_KEYS, Map.class);
	}
	
	private final PacketBufferCodec<T> packetCodec;
	private final T defaultValue;
	private final boolean syncronizeTrackingPlayers;
	
	public SkillDataKey(PacketBufferCodec<T> packetCodec, T defaultValue, boolean syncronizeTrackingPlayers) {
		this.packetCodec = packetCodec;
		this.defaultValue = defaultValue;
		this.syncronizeTrackingPlayers = syncronizeTrackingPlayers;
	}
	
	public T readFromBuffer(FriendlyByteBuf buffer) {
		return this.packetCodec.decode(buffer);
	}
	
	public void writeToBuffer(FriendlyByteBuf buffer, T value) {
		this.packetCodec.encode(value, buffer);
	}
	
	public T defaultValue() {
		return this.defaultValue;
	}
	
	public int getId() {
		return getIdMap().getId(this);
	}
	
	public boolean syncronizeToTrackingPlayers() {
		return this.syncronizeTrackingPlayers;
	}
}
