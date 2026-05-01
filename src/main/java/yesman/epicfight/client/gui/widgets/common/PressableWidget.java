package yesman.epicfight.client.gui.widgets.common;

import net.minecraft.client.gui.components.AbstractWidget;

/// This widget interface is a temporary solution to avoid implementing a deprecated, [AbstractWidget#onClick]
///
/// Splitting [AbstractWidget#onClick] and [AbstractWidget#mouseClicked] is still beneficial since it's reusable
/// by key press, especially for controller support.
public interface PressableWidget {
    void onPressed();
}
