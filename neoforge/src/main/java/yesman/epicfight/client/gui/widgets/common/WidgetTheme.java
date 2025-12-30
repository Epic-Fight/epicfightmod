package yesman.epicfight.client.gui.widgets.common;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

/**
 * Enum interface for widget styles
 */
public interface WidgetTheme extends ExtensibleEnum {
    ExtensibleEnumManager<WidgetTheme> ENUM_MANAGER = new ExtensibleEnumManager<> ("widget_theme");
}
