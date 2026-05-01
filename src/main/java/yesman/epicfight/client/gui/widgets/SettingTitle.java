package yesman.epicfight.client.gui.widgets;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.gui.widgets.common.PressableWidget;
import yesman.epicfight.registry.entries.EpicFightSounds;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingTitle extends AbstractWidget implements AnchoredWidget, PressableWidget {
    private final Font font;
    private final Component tooltip;
    private final Consumer<Component> tooltipMessageSetter;
    private final Supplier<SettingTitle> hoveringWidgetProvider;
    private final Consumer<SettingTitle> hoveringWidgetSetter;
    private final Supplier<GuiEventListener> focusedWidgetProvider;

    public SettingTitle(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        VerticalAnchorType verticalAnchor,
        String translatable,
        Consumer<Component> tooltipMessageSetter,
        Supplier<SettingTitle> hoveringWidgetProvider,
        Consumer<SettingTitle> hoveringWidgetSetter,
        Supplier<GuiEventListener> focusedWidgetProvider
    ) {
        super(0, 0, 0, 0, Component.translatable(translatable));

        this.font = font;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;
        this.tooltip = Component.translatable(translatable.replace("widget", "tooltip"));
        this.tooltipMessageSetter = tooltipMessageSetter;
        this.hoveringWidgetProvider = hoveringWidgetProvider;
        this.hoveringWidgetSetter = hoveringWidgetSetter;
        this.focusedWidgetProvider = focusedWidgetProvider;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        SettingTitle hoveringTitle = this.hoveringWidgetProvider.get();

        if (this.isActive() && this.isHovered() && hoveringTitle != this) {
            this.hoveringWidgetSetter.accept(this);
            this.tooltipMessageSetter.accept(this.tooltip);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(EpicFightSounds.HOVER_WIDGET.get(), 1.0F));
        } else if (hoveringTitle == this && !this.isHovered()) {
            this.hoveringWidgetSetter.accept(null);

            if (!this.isFocused()) {
                GuiEventListener focusedWidget = this.focusedWidgetProvider.get();

                if (focusedWidget instanceof SettingTitle settingTitle) {
                    this.tooltipMessageSetter.accept(settingTitle.tooltip);
                } else {
                    this.tooltipMessageSetter.accept(Component.empty());
                }
            }
        }

        guiGraphics.pose().translate(this.getX(), this.getY(), partialTick);
        guiGraphics.pose().translate(-this.getX(), -this.getY(), partialTick);

        int leftGradientStart = this.getX() + 10;
        int leftGradientEnd = this.getX() + 20;
        int rightGradientStart = Math.min(leftGradientEnd + this.font.width(this.getMessage()), this.getWidth() - 10);
        int rightGradientEnd = rightGradientStart + 10;
        int yStart = this.getY() + this.getHeight() / 2 - 1;
        int yEnd = this.getY() + this.getHeight() / 2;
        int color = this.isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF9F9F9F;

        guiGraphics.fill(this.getX(), yStart, leftGradientStart, yEnd, color);
        fillGradientLeftToRight(guiGraphics, leftGradientStart, yStart, leftGradientEnd, yEnd, color, 0x00000000);
        fillGradientLeftToRight(guiGraphics, rightGradientStart, yStart, rightGradientEnd, yEnd, 0x00000000, color);
        guiGraphics.fill(rightGradientEnd, yStart, this.getX() + this.getWidth(), yEnd, color);

        String correctedString = this.font.plainSubstrByWidth(this.getMessage().getString(), this.getWidth() - 20);
        guiGraphics.drawString(this.font, correctedString, leftGradientEnd, this.getY() + this.height / 2 - this.font.lineHeight / 2, color, false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.clicked(mouseX, mouseY);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onPressed();

                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focused) this.onPressed();
    }

    @Override
    public void onPressed() {
        this.tooltipMessageSetter.accept(this.tooltip);
    }

    /// Since [GuiGraphics#fillGradient] only draws the gradient direction from top to bottom we created this method to draw gradient from left to right
    private static void fillGradientLeftToRight(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int pColorFrom, int pColorTo) {
        VertexConsumer consumer = guiGraphics.bufferSource().getBuffer(RenderType.gui());

        float f = FastColor.ARGB32.alpha(pColorFrom) / 255.0F;
        float f1 = FastColor.ARGB32.red(pColorFrom) / 255.0F;
        float f2 = FastColor.ARGB32.green(pColorFrom) / 255.0F;
        float f3 = FastColor.ARGB32.blue(pColorFrom) / 255.0F;
        float f4 = FastColor.ARGB32.alpha(pColorTo) / 255.0F;
        float f5 = FastColor.ARGB32.red(pColorTo) / 255.0F;
        float f6 = FastColor.ARGB32.green(pColorTo) / 255.0F;
        float f7 = FastColor.ARGB32.blue(pColorTo) / 255.0F;
        Matrix4f matrix4f = guiGraphics.pose().last().pose();

        consumer.addVertex(matrix4f, x1, y1, 0).setColor(f1, f2, f3, f);
        consumer.addVertex(matrix4f, x1, y2, 0).setColor(f1, f2, f3, f);
        consumer.addVertex(matrix4f, x2, y2, 0).setColor(f5, f6, f7, f4);
        consumer.addVertex(matrix4f, x2, y1, 0).setColor(f5, f6, f7, f4);
    }

    /// ***************************************************************
    /// [AnchoredWidget] implementations                              *
    /// ***************************************************************
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
