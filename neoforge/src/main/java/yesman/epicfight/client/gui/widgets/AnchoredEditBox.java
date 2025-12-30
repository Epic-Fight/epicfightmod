package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.DataBoundWidget;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class AnchoredEditBox extends EditBox implements AnchoredWidget, DataBoundWidget<String> {
    public AnchoredEditBox(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        AnchoredWidget.VerticalAnchorType verticalAnchor,
        Callable<String> dataProvider,
        Consumer<String> onWidgetChanged,
        Component message
    ) {
        super(font, 0, 0, 0, 0, message);

        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;
        this.dataProvider = dataProvider;
        this.onValueChanged = onWidgetChanged;
    }

    /// ***************************************************************
    /// [AnchoredWidget] implementations                              *
    /// ***************************************************************
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private final AnchoredWidget.HorizontalAnchorType horizontalAnchorType;
    private final AnchoredWidget.VerticalAnchorType verticalAnchorType;

    @Override
    public int getX1() {
        return this.x1;
    }

    @Override
    public int getX2() {
        return this.x2;
    }

    @Override
    public int getY1() {
        return this.y1;
    }

    @Override
    public int getY2() {
        return this.y2;
    }

    @Override
    public void setX1(int i) {
        this.x1 = i;
    }

    @Override
    public void setX2(int i) {
        this.x2 = i;
    }

    @Override
    public void setY1(int i) {
        this.y1 = i;
    }

    @Override
    public void setY2(int i) {
        this.y2 = i;
    }

    @Override
    public HorizontalAnchorType getHorizontalAnchorType() {
        return this.horizontalAnchorType;
    }

    @Override
    public VerticalAnchorType getVerticalAnchorType() {
        return this.verticalAnchorType;
    }

    /// *****************************************************************
    /// [DataBoundWidget] implementations                               *
    /// *****************************************************************
    private final Callable<String> dataProvider;
    private final Consumer<String> onValueChanged;

    @Override
    public Callable<String> getDataProvider() {
        return this.dataProvider;
    }

    @Override
    public Consumer<String> valueChangeCallback() {
        return this.onValueChanged;
    }

    @Override
    public Consumer<String> valueSetter() {
        return this::setValue;
    }

    @Override
    public void reset() {
        /// TODO assign the default data type
        /// e.g.
        /// this.value = 0.0D; (for double type)
        /// this.value = 0; (for integer type)
        /// this.value = false; (for boolean type)
    }
}
