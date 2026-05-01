package yesman.epicfight.client.gui.widgets.common;

/**
 * An utility interface to mark the widget to warn users some exceptional cases
 */
public interface WarningMarkableWidget<T> extends DataBoundWidget<T> {
    /**
     * Set this component as marked with some eye-catching color or warning icons
     */
    void mark();

    /**
     * Remove the mark with the default color or getting rid of warning icons
     */
    void unmark();
}
