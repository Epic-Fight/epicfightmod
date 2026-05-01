package yesman.epicfight.config;

import java.util.function.Consumer;

public class OptionHandler<T> {
	protected T value;
	protected Consumer<T> onchanged;
	
	public OptionHandler(T value, Consumer<T> onchanged) {
		this.value = value;
		this.onchanged = onchanged;
	}
	
	public T getValue() {
		return this.value;
	}
	
	public void setValue(T value) {
		this.value = value;
		this.onchanged.accept(value);
	}
	
	public static <T> OptionHandler<T> of(T value, Consumer<T> onchanged) {
		return new OptionHandler<> (value, onchanged);
	}
}