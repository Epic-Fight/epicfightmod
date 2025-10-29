package yesman.epicfight.api.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.chat.Component;
import yesman.epicfight.main.EpicFightMod;

public class ExtendableEnumManager<T extends ExtendableEnum> {
	private final Int2ObjectMap<T> enumMapByOrdinal = new Int2ObjectLinkedOpenHashMap<>();
	private final Map<String, T> enumMapByName = Maps.newLinkedHashMap();
	private final Map<String, Class<?>> enums = Maps.newConcurrentMap();
	private final String enumName;
	private int lastOrdinal = 0;
	
	public ExtendableEnumManager(String enumName) {
		this.enumName = enumName;
	}
	
	public void registerEnumCls(String modid, Class<? extends ExtendableEnum> cls) {
		if (this.enums.containsKey(modid)) {
			EpicFightMod.LOGGER.error(modid + " is already registered in " + this.enumName);
		}
		
		EpicFightMod.LOGGER.debug("Registered Extendable Enum " + cls +" in " + this.enumName);
		this.enums.put(modid, cls);
	}
	
	public void loadEnum() {
		List<String> orderByModid = new ArrayList<> (this.enums.keySet());
		Collections.sort(orderByModid);
		Class<?> cls = null;
		
		try {
			for (String modid : orderByModid) {
				cls = this.enums.get(modid);
				
				Method m = cls.getMethod("values");
				m.invoke(null);
				
				EpicFightMod.LOGGER.debug("Loaded enums in " + cls);
			}
		} catch (ClassCastException e) {
			EpicFightMod.LOGGER.error(cls.getCanonicalName() + " is not an ExtendableEnum!");
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			EpicFightMod.LOGGER.error(cls.getCanonicalName() + " is not an Enum class!");
			e.printStackTrace();
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			EpicFightMod.LOGGER.warn("Error while loading extendable enum " + cls.getCanonicalName());
			e.printStackTrace();
		}
		
		EpicFightMod.LOGGER.debug("All enums are loaded: " + this.enumName +" "+ this.enumMapByName.values());
	}
	
	public int assign(T value) {
		int lastOrdinal = this.lastOrdinal;
		String enumName = ParseUtil.toLowerCase(value.toString());
		
		if (this.enumMapByName.containsKey(enumName)) {
			throw new IllegalArgumentException("Enum name " + enumName + " already exists in " + this.enumName);
		}
		
		this.enumMapByOrdinal.put(lastOrdinal, value);
		this.enumMapByName.put(enumName, value);
		++this.lastOrdinal;
		
		return lastOrdinal;
	}
	
	public T getOrThrow(int id) throws NoSuchElementException {
		if (!this.enumMapByOrdinal.containsKey(id)) {
			throw new NoSuchElementException("Enum id " + id + " does not exist in " + this.enumName);
		}
		
		return this.enumMapByOrdinal.get(id);
	}
	
	public T getOrThrow(String name) throws NoSuchElementException {
		String key = ParseUtil.toLowerCase(name);
		
		if (!this.enumMapByName.containsKey(key)) {
			throw new NoSuchElementException("Enum name " + key + " does not exist in " + this.enumName);
		}
		
		return this.enumMapByName.get(key);
	}
	
	public T get(int id) {
		return this.enumMapByOrdinal.get(id);
	}
	
	public T get(String name) {
		return this.enumMapByName.get(ParseUtil.toLowerCase(name));
	}
	
	public Collection<T> universalValues() {
		return this.enumMapByOrdinal.values();
	}
	
	public String toTranslated(ExtendableEnum e) {
		return Component.translatable(EpicFightMod.MODID + "." + this.enumName + "." + ParseUtil.toLowerCase(e.toString())).getString();
	}
}