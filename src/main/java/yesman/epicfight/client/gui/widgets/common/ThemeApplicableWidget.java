package yesman.epicfight.client.gui.widgets.common;

import org.jetbrains.annotations.NotNull;

/**
 * A utility interface for widgets that have various styles based on {@link WidgetTheme}
 */
public interface ThemeApplicableWidget<T extends WidgetTheme> {
    /**
     * Returns if the given theme enum is supported by this widget
     */
    boolean isSupportedTheme(WidgetTheme theme);

    /**
     * Returns the current theme of the widget
     */
    @NotNull
    T getTheme();

    /**
     * Applies a new theme
     */
    void setTheme(T theme);
}
