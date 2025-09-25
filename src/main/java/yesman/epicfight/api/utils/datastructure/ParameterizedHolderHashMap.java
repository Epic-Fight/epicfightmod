package yesman.epicfight.api.utils.datastructure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;

import net.minecraft.core.Holder;
import yesman.epicfight.api.utils.datastructure.ParameterizedMap.ParameterizedKey;

@SuppressWarnings("unchecked")
public class ParameterizedHolderHashMap<A extends ParameterizedKey<?>> implements ParameterizedMap<Holder<? extends A>> {
	private Map<Holder<? extends A>, Object> innerMap = new HashMap<> ();
	private boolean immutable;
	
	@Override
	public ParameterizedHolderHashMap<A> makeImmutable() {
		this.innerMap = ImmutableMap.copyOf(this.innerMap);
		this.immutable = true;
		return this;
	}
	
	public <T> T put(Holder<? extends A> typeKeyHolder, T val) {
		if (this.immutable) {
			throw new UnsupportedOperationException("Immutable map");
		}
		
		return (T)this.innerMap.put(typeKeyHolder, val);
	}
	
	public <T> T get(Holder<? extends A> typeKey) {
		return (T)this.innerMap.get(typeKey);
	}
	
	public <T> T getOrDefault(Holder<? extends A> typeKey) {
		return (T)this.innerMap.getOrDefault(typeKey, typeKey.value().defaultValue());
	}
	
	public <T> T computeIfAbsent(Holder<? extends A> key, Function<Holder<? extends A>, T> mapper) {
		return (T)this.innerMap.computeIfAbsent(key, mapper);
	}
	
	public <T> T computeIfPresent(Holder<? extends A> key, BiFunction<Holder<? extends A>, T, T> mapper) {
		return (T)this.innerMap.computeIfPresent(key, (BiFunction<Holder<? extends A>, ? super Object, ? extends Object>)mapper);
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
	public boolean containsKey(Holder<? extends A> key) {
		return this.innerMap.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return this.innerMap.containsValue(value);
	}
	
	@Override
	public <T> T remove(Holder<? extends A> key) {
		return (T) this.innerMap.remove(key);
	}
	
	@Override
	public void clear() {
		this.innerMap.clear();
	}
	
	@Override
	public Set<Holder<? extends A>> keySet() {
		return this.innerMap.keySet();
	}
	
	@Override
	public Collection<Object> values() {
		return this.innerMap.values();
	}
	
	@Override
	public Set<Map.Entry<Holder<? extends A>, Object>> entrySet() {
		return this.innerMap.entrySet();
	}
	
	@Override
	public void forEach(BiConsumer<Holder<? extends A>, Object> task) {
		this.innerMap.forEach(task);
	}

	@Override
	public void putAll(ParameterizedMap<Holder<? extends A>> map) {
		this.innerMap.putAll(map.innerMap());
	}

	@Override
	public Map<Holder<? extends A>, Object> innerMap() {
		return this.innerMap;
	}
	
	@Override
	public String toString() {
		return this.innerMap.toString();
	}
}
