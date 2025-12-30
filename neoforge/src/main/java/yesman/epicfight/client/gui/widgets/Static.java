package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;

import javax.annotation.Nullable;

public class Static extends AbstractWidget implements AnchoredWidget {
    private final Font font;
    private int fontColor = 0xFFFFFFFF;

    public Static(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        String translatable
    ) {
        this(font, x1, x2, y1, y2, horizontalAnchor, verticalAnchor, Component.translatable(translatable), Component.translatable(translatable + ".tooltip"));
    }

    public Static(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        Component message
    ) {
        this(font, x1, x2, y1, y2, horizontalAnchor, verticalAnchor, message, null);
    }

    public Static(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        Component message,
        @Nullable Component tooltip
    ) {
        super(0, 0, 0, 0, message);

        this.font = font;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;

        this.setTooltip(tooltip == null ? null : Tooltip.create(tooltip));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        String correctedString = this.font.plainSubstrByWidth(this.getMessage().getString(), this.getWidth());
        guiGraphics.drawString(this.font, correctedString, this.getX(), this.getY() + this.height / 2 - this.font.lineHeight / 2, this.fontColor, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= (double) this.getX() && mouseY >= (double) this.getY() && mouseX < (double) (this.getX() + this.font.width(this.getMessage())) && mouseY < (double) (this.getY() + this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int action) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int action) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int action, double p_93648_, double p_93649_) {
        return false;
    }

    public void setColor(int r, int g, int b) {
        this.fontColor = 0xFF000000 | r << 24 | g << 16 | b << 8;
    }

    /*******************************************************************
     * {@link AnchoredWidget} implementations                          *
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