package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.DataBoundWidget;
import yesman.epicfight.client.gui.widgets.common.PressableWidget;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class CheckBox extends AbstractWidget implements AnchoredWidget, DataBoundWidget<Boolean>, PressableWidget {
    private final Font font;
    private final boolean showMessage;
    private boolean checked;

    public CheckBox(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        AnchoredWidget.VerticalAnchorType verticalAnchor,
        Callable<Boolean> dataProvider,
        Consumer<Boolean> onWidgetChanged,
        Component message,
        boolean showMessage
    ) {
        super(0, 0, 0, 0, message);

        this.font = font;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;
        this.dataProvider = dataProvider;
        this.onWidgetChanged = onWidgetChanged;
        this.showMessage = showMessage;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.clicked(x, y);

                if (flag) {
                    // No play sound
                    this.onPressed();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.active && this.visible) {
            if (CommonInputs.selected(keyCode)) {
                this.onPressed();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected boolean clicked(double x, double y) {
        return this.active && this.visible && x >= (double)this.getX() && y >= (double) this.getY() && x < (double) (this.getX() + this.width) && y < (double) (this.getY() + this.height);
    }

    @Override
    public void onPressed() {
        this.checked = !this.checked;
        this.onWidgetChanged.accept(this.checked);
    }

    @Override
    public boolean isMouseOver(double x, double y) {
        int rectangleLength = Math.min(this.getWidth(), this.getHeight());
        return this.active && this.visible && x >= (double)this.getX() && y >= (double)this.getY() && x < (double)(this.getX() + rectangleLength) && y < (double)(this.getY() + rectangleLength);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        int rectangleLength = Math.min(this.getWidth(), this.getHeight());
        int outlineColor = this.isFocused() ? -1 : this.isActive() ? -6250336 : -12566463;

        guiGraphics.fill(this.getX(), this.getY(), this.getX() + rectangleLength, this.getY() + rectangleLength, outlineColor);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + rectangleLength - 1, this.getY() + rectangleLength - 1, -16777216);

        if (this.checked) {
            guiGraphics.fill(this.getX() + 2, this.getY() + 2, this.getX() + rectangleLength - 2, this.getY() + rectangleLength - 2, -1);
        }

        int fontColor = this.isActive() ? 16777215 : 4210752;

        if (this.showMessage) guiGraphics.drawString(this.font, this.getMessage(), this.getX() + rectangleLength + 4, this.getY() + this.height / 2 - this.font.lineHeight / 2 + 1, fontColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    /// Returns the state whether the widget is checked, not a bound data value
    public boolean getWidgetValue() {
        return this.checked;
    }

    /*******************************************************************
     * {@link @AnchoredWidget} implementations                         *
     *******************************************************************/
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private final AnchoredWidget.HorizontalAnchorType horizontalAnchorType;
    private final VerticalAnchorType verticalAnchorType;

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

    /*******************************************************************
     * {@link DataBoundWidget} implementations                         *
     *******************************************************************/
    private final Callable<Boolean> dataProvider;
    private final Consumer<Boolean> onWidgetChanged;

    @Override
    public Callable<Boolean> getDataProvider() {
        return this.dataProvider;
    }

    @Override
    public Consumer<Boolean> valueChangeCallback() {
        return this.onWidgetChanged;
    }

    @Override
    public Consumer<Boolean> valueSetter() {
        return (bool) -> this.checked = bool;
    }

    @Override
    public void reset() {
        this.checked = false;
    }
}