package yesman.epicfight.api.utils.datastructure;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public interface ParameterizedMap<K> {
	
	public int size();

	public boolean isEmpty();

	public boolean containsKey(K key);

	public boolean containsValue(Object value);

	public <T> T remove(K key);
	
	public void clear();

	public Set<K> keySet();

	public Collection<Object> values();
	
	public Set<Map.Entry<K, Object>> entrySet();
	
	public ParameterizedMap<K> makeImmutable();
	
	public void forEach(BiConsumer<K, Object> task);
	
	public void putAll(ParameterizedMap<K> map);
	
	public Map<K, Object> innerMap();
	
	public interface ParameterizedKey<T> {
		T defaultValue();
	}
}
