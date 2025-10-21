package yesman.epicfight.api.utils.datastructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import yesman.epicfight.api.utils.datastructure.ParameterizedMap.ParameterizedKey;

@SuppressWarnings("unchecked")
public class ParameterizedHashMap<A extends ParameterizedKey<?>> implements ParameterizedMap<A> {
	private Map<A, Object> innerMap = new HashMap<> ();
	private boolean immutable;
	
	@Override
	public ParameterizedHashMap<A> makeImmutable() {
		this.innerMap = ImmutableMap.copyOf(this.innerMap);
		this.immutable = true;
		return this;
	}
	
	public <T> T put(A typeKeyHolder, T val) {
		if (this.immutable) {
			throw new UnsupportedOperationException("Immutable map");
		}
		
		return (T)this.innerMap.put(typeKeyHolder, val);
	}
	
	public <T> T get(A typeKey) {
		return (T)this.innerMap.get(typeKey);
	}
	
	public <T> T getOrDefault(A typeKey) {
		return (T)this.innerMap.getOrDefault(typeKey, typeKey.defaultValue());
	}
	
	public <T> T computeIfAbsent(A key, Function<A, T> mapper) {
		return (T)this.innerMap.computeIfAbsent(key, mapper);
	}
	
	public <T> T computeIfPresent(A key, BiFunction<A, T, T> mapper) {
		return (T)this.innerMap.computeIfPresent(key, (BiFunction<A, ? super Object, ? extends Object>)mapper);
	}
	
	@Override
	public int size() {
		return this.innerMap.size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.innerMap.isEmpty();
	}
	
	@Override
	public boolean containsKey(A key) {
		return this.innerMap.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return this.innerMap.containsValue(value);
	}
	
	@Override
	public <T> T remove(A key) {
		return (T) this.innerMap.remove(key);
	}
	
	@Override
	public void clear() {
		this.innerMap.clear();
	}
	
	@Override
	public Set<A> keySet() {
		return this.innerMap.keySet();
	}
	
	@Override
	public Collection<Object> values() {
		return this.innerMap.values();
	}
	
	@Override
	public Set<Map.Entry<A, Object>> entrySet() {
		return this.innerMap.entrySet();
	}
	
	@Override
	public void forEach(BiConsumer<A, Object> task) {
		this.innerMap.forEach(task);
	}

	@Override
	public void putAll(ParameterizedMap<A> map) {
		if (this.immutable) {
			throw new UnsupportedOperationException("Immutable map");
		}
		
		this.innerMap.putAll(map.innerMap());
	}

	@Override
	public Map<A, Object> innerMap() {
		return this.innerMap;
	}
	
	@Override
	public String toString() {
		return this.innerMap.toString();
	}
}