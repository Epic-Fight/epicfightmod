package yesman.epicfight.client.gui.datapack.screen;

import java.util.Map;
import java.util.function.Supplier;


public class PackEntry<K, T> implements Map.Entry<K, T> {
	public static <K, T> PackEntry<K, T> of(K packKey, Supplier<T> valueGetter) {
		return new PackEntry<>(packKey, valueGetter.get());
	}
	
	public static <K, T> PackEntry<K, T> ofValue(K packKey, T value) {
		return new PackEntry<>(packKey, value);
	}
	
	private K packKey;
	private T packValue;
	
	private PackEntry(K packKey, T valueGetter) {
		this.packKey = (packKey);
		this.packValue = valueGetter;
	}
	
	public void setPackKey(K packKey) {
		this.packKey = packKey;
	}
	
	@Override
	public K getKey() {
		return this.packKey;
	}

	@Override
	public T getValue() {
		return this.packValue;
	}

	@Override
	public T setValue(T value) {
		this.packValue = value;
		return value;
	}
}
