package yesman.epicfight.client.gui.datapack.widgets;

import java.util.function.Consumer;

import javax.annotation.Nullable;


public interface DataBindingComponent<T, R> extends ResizableComponent {
	public void reset();
	public T _getValue();
	public void _setValue(@Nullable T value);
	public void _setResponder(Consumer<R> responder);
	public Consumer<R> _getResponder();
}