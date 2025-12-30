package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.DataBoundWidget;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClampedNumberBox<T extends Number> extends AbstractContainerWidget implements AnchoredWidget, DataBoundWidget<T> {
    private final T minValue;
    private final T maxValue;
    private final T increment;
    private final T decrement;

    private final Function<String, T> textParser;
    private final Function<T, String> toString;
    private final Comparator<T> comparator;
    private final BiFunction<T, T, T> sum;

    private final AnchoredButton increaseButton;
    private final AnchoredButton decreaseButton;
    private final EditBox editBox;

    private int focusIndex;

    public ClampedNumberBox(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        Callable<T> dataProvider,
        Consumer<T> onWidgetChanged,
        Component message,
        T minValue,
        T maxValue,
        T increment,
        T decrement,
        Function<String, T> valueParser,
        Function<T, String > toString,
        Comparator<T> comparator,
        BiFunction<T, T, T> sum
    ) {
        super(0, 0, 0, 0, message);
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;
        this.dataProvider = dataProvider;
        this.onWidgetChanged = onWidgetChanged;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.increment = increment;
        this.decrement = decrement;
        this.textParser = valueParser;
        this.toString = toString;
        this.comparator = comparator;
        this.sum = sum;

        this.editBox = new EditBox(font, 0, 0, message);
        this.editBox.setFilter(text -> text.isEmpty() || ParseUtil.isParsable(text, valueParser));
        this.editBox.setResponder(this::responder);

        this.increaseButton =
            AnchoredButton
                .buttonBuilder(Component.literal("+"), button -> this.increase())
                .horizontalAnchorType(HorizontalAnchorType.LEFT_WIDTH)
                .verticalAnchorType(VerticalAnchorType.CENTER_HEIGHT)
                .theme(AnchoredButton.BuiltInTheme.TRANSPARENT)
                .highlihgtFontWhen(AbstractWidget::isHoveredOrFocused)
                .build();

        this.decreaseButton =
            AnchoredButton
                .buttonBuilder(Component.literal("-"), button -> this.decrease())
                .horizontalAnchorType(HorizontalAnchorType.RIGHT_WIDTH)
                .verticalAnchorType(VerticalAnchorType.CENTER_HEIGHT)
                .theme(AnchoredButton.BuiltInTheme.TRANSPARENT)
                .highlihgtFontWhen(AbstractWidget::isHoveredOrFocused)
                .build();
    }

    private void responder(String text) {
        try {
            T val = this.textParser.apply(text);

            if (this.comparator.compare(val, this.minValue) < 0) {
                val = this.minValue;
            } else if (this.comparator.compare(val, this.maxValue) > 0) {
                val = this.maxValue;
            }

            this.editBox.setResponder(null);
            this.editBox.setValue(this.toString.apply(val));
            this.editBox.setResponder(this::responder);
            this.valueChangeCallback().accept(val);
        } catch (NumberFormatException ignore) {
        }
    }

    private T getAsValue() throws NumberFormatException {
        return this.textParser.apply(this.editBox.getValue());
    }

    public void setValue(T value) {
        String text = this.toString.apply(value);

        if (ParseUtil.isParsable(text, this.textParser)) {
            T val = this.textParser.apply(text);

            if (this.comparator.compare(val, this.minValue) < 0) {
                val = this.minValue;
            } else if (this.comparator.compare(val, this.maxValue) > 0) {
                val = this.maxValue;
            }

            this.editBox.setValue(this.toString.apply(val));
            this.editBox.moveCursorToEnd(true);
            this.valueChangeCallback().accept(val);
        } else {
            this.editBox.setValue(text);
            this.editBox.moveCursorToEnd(true);
        }
    }

    public void increase() {
        try {
            this.setValue(this.sum.apply(this.getAsValue(), this.increment));
        } catch (NumberFormatException ignored) {
        }
    }

    public void decrease() {
        try {
            this.setValue(this.sum.apply(this.getAsValue(), this.decrement));
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.increaseButton.render(guiGraphics, mouseX, mouseY, partialTick);
        this.editBox.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        this.decreaseButton.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(this.increaseButton, this.editBox, this.decreaseButton);
    }

    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (focusNavigationEvent instanceof FocusNavigationEvent.InitialFocus) {
            this.focusIndex = 0;
            return ComponentPath.path(this, ComponentPath.leaf(this.children().get(0)));
        }

        if (this.isFocused()) {
            if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation(ScreenDirection direction)) {
                switch (direction) {
                    case LEFT -> {
                        return this.focusIndex > 0 ? ComponentPath.path(this, ComponentPath.leaf(this.children().get(--this.focusIndex))) : null;
                    }
                    case RIGHT -> {
                        return this.focusIndex < this.children().size() - 1 ? ComponentPath.path(this, ComponentPath.leaf(this.children().get(++this.focusIndex))) : null;
                    }
                }

                return null;
            } else if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation(boolean forward)) {
                if (forward) {
                    return this.focusIndex < this.children().size() - 1 ? ComponentPath.path(this, ComponentPath.leaf(this.children().get(++this.focusIndex))) : null;
                } else {
                    return this.focusIndex > 0 ? ComponentPath.path(this, ComponentPath.leaf(this.children().get(--this.focusIndex))) : null;
                }
            }
        } else {
            if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation(ScreenDirection direction)) {
                this.focusIndex = switch (direction) {
                    case UP, DOWN -> this.focusIndex;
                    case LEFT -> this.children().size() - 1;
                    case RIGHT -> 0;
                };

                return ComponentPath.path(this, ComponentPath.leaf(this.children().get(this.focusIndex)));
            } else if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation(boolean forward)) {
                if (forward) {
                    this.focusIndex = 0;
                } else {
                    this.focusIndex = this.children().size() - 1;
                }

                return ComponentPath.path(this, ComponentPath.leaf(this.children().get(this.focusIndex)));
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.focusIndex = 0;

        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(guieventlistener);

                if (button == 0) {
                    this.setDragging(true);
                }

                return true;
            }

            this.focusIndex++;
        }

        this.focusIndex = 0;

        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);

        if (this.getFocused() != null && !focused) {
            this.getFocused().setFocused(false);
            this.setFocused(null);
        }
    }

    /*******************************************************************
     * {@link AnchoredWidget} implementations                          *
     *******************************************************************/
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private final HorizontalAnchorType horizontalAnchorType;
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

    public void relocate(ScreenRectangle screenRectangle) {
        AnchoredWidget.super.relocate(screenRectangle);
        ScreenRectangle widgetRectangle = new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        int buttonSize = (int)Math.min(this.height, this.width * 0.2F);

        this.increaseButton.setX2(buttonSize);
        this.increaseButton.setY2(buttonSize);
        this.increaseButton.relocate(widgetRectangle);
        this.editBox.setPosition(this.getX() + buttonSize + 1, this.getY());
        this.editBox.setSize(this.getWidth() - (buttonSize + 1) * 2, this.getHeight());
        this.editBox.moveCursorToEnd(true);
        this.decreaseButton.setX2(buttonSize);
        this.decreaseButton.setY2(buttonSize);
        this.decreaseButton.relocate(widgetRectangle);
    }

    /*******************************************************************
     * {@link DataBoundWidget} implementations                         *
     *******************************************************************/
    private final Callable<T> dataProvider;
    private final Consumer<T> onWidgetChanged;

    @Override
    public Callable<T> getDataProvider() {
        return this.dataProvider;
    }

    @Override
    public Consumer<T> valueChangeCallback() {
        return this.onWidgetChanged;
    }

    @Override
    public Consumer<T> valueSetter() {
        return this::setValue;
    }

    @Override
    public void reset() {
        this.editBox.setValue("");
    }
}
