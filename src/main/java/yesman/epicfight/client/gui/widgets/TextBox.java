package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.ScrollableWidget;

import javax.annotation.Nullable;

public class TextBox extends AbstractWidget implements AnchoredWidget, ScrollableWidget {
    private static final int LINE_HEIGHT = 18;

    private final Font font;
    private int scrollPosition;
    private int lines;

    public TextBox(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        Component message
    ) {
        super(0, 0, 0, 0, message);

        this.font = font;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;
    }

    @Override
    public int getMaxWidgetHeight() {
        return this.lines * this.getScrollStride();
    }

    @Override
    public int getScrollStride() {
        return LINE_HEIGHT;
    }

    @Override
    public int getScrollPosition() {
        return this.scrollPosition;
    }

    @Override
    public void setScrollPosition(int position) {
        this.scrollPosition = position;
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        this.lines = this.font.split(this.getMessage(), this.width - 6).size();
    }

    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        this.lines = this.font.split(this.getMessage(), this.width - 6).size();
        this.scrollPosition = 0;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), 0xBF000000);

        var textLine = this.font.split(this.getMessage(), this.width - 6 - (this.isScrollVisible() ? this.getScrollWidth() : 0));
        int textY = this.getY() + 6;

        this.preRenderProcess(guiGraphics);

        for (var text : textLine) {
            guiGraphics.drawString(this.font, text, this.getX() + 3, textY, -1);
            textY += this.getScrollStride();
        }

        this.postRenderProcess(guiGraphics);

        this.computeScrollPositionAndRender(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderScroll(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int minX, int minY, int maxX, int maxY) {
        guiGraphics.fill(minX, minY, maxX, maxY, 0x8BFFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean scrollVisible = this.isScrollVisible();
        if (scrollVisible) this.moveScroll(-MathUtils.getSign(scrollY));

        return scrollVisible;
    }

    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }

    /*******************************************************************
     * {@link AnchoredWidget} implementations                         *
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
}
