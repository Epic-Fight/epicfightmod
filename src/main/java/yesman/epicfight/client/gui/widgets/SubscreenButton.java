package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.util.function.Supplier;

public class SubscreenButton extends AnchoredButton {
    protected final Supplier<Screen> openScreen;

    public SubscreenButton(
        int x1,
        int x2,
        int y1,
        int y2,
        HorizontalAnchorType horizontalAnchorType,
        VerticalAnchorType verticalAnchorType,
        Button.OnPress onPress,
        Supplier<Screen> openScreen
    ) {
        super(x1, x2, y1, y2, CommonComponents.ELLIPSIS, onPress, Button.DEFAULT_NARRATION, horizontalAnchorType, verticalAnchorType, BuiltInTheme.VANILLA, AbstractWidget::isHoveredOrFocused);
        this.openScreen = openScreen;
    }

    @Override
    public void onPress() {
        Minecraft.getInstance().setScreen(this.openScreen.get());
    }
}
