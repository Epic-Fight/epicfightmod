package yesman.epicfight.client.gui.datapack.widgets;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/// We're refactoring UI codes, use [yesman.epicfight.client.gui.widgets.common.DataBoundWidget] instead
@Deprecated
public interface DataBindingComponent<T, R> extends ResizableComponent {
	public void reset();
	public T _getValue();
	public void _setValue(@Nullable T value);
	public void _setResponder(Consumer<R> responder);
	public Consumer<R> _getResponder();
}