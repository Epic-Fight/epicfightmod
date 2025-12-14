package yesman.epicfight.api.utils.datastruct;

import java.util.HashMap;

import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap.TypeKey;

@SuppressWarnings("serial")
public class TypeFlexibleHashMap<A extends TypeKey<?>> extends HashMap<A, Object> {
	final boolean immutable;
	
    public TypeFlexibleHashMap(boolean immutable) {
        this.immutable = immutable;
    }
	
	@SuppressWarnings("unchecked")
	public <T> T put(TypeKey<T> typeKey, T val) {
		if (this.immutable) {
			throw new UnsupportedOperationException();
		}
		
		return (T)super.put((A)typeKey, val);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(A typeKey) {
		return (T)super.get(typeKey);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOrDefault(A typeKey) {
		return (T)super.getOrDefault(typeKey, typeKey.defaultValue());
	}
	
	public interface TypeKey<T> {
		T defaultValue();
	}
}