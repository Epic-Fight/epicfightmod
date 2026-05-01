package yesman.epicfight.client.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.emote.EmoteIterator;
import yesman.epicfight.world.capabilities.emote.PlayerEmoteSlots;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EmoteWheelTab extends AbstractContainerWidget implements AnchoredWidget {
    private final List<TabButton> tabButtons = new ArrayList<>();
    private final List<WheelPage> wheelPages = new ArrayList<>();
    private final Consumer<Holder.Reference<Emote>> onPressWheelButton;
    private final Font font;
    private final PlayerSkin playerSkin;
    private final boolean editable;

    @Nullable
    private final TabButton addPageButton;
    @Nullable
    private final TabButton removePageButton;

    private int pageIndex = 0;
    @Nullable
    private EmoteWheelButton hoveringWheel;
    @Nullable
    private EmoteWheelButton lastPressedWheel;
    @Nullable
    private TransitionType transitionType = null;
    private int maxTransitionTime;
    private int transitionTime;
    private boolean edited;

    public EmoteWheelTab(
        Font font,
        PlayerEmoteSlots emoteSlots,
        PlayerSkin playerSkin,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        AnchoredWidget.VerticalAnchorType verticalAnchor,
        Consumer<Holder.Reference<Emote>> onPressWheelButton,
        boolean editable
    ) {
        super(0, 0, 0, 0, Component.empty());

        this.font = font;
        this.playerSkin = playerSkin;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.horizontalAnchorType = horizontalAnchor;
        this.verticalAnchorType = verticalAnchor;
        this.onPressWheelButton = onPressWheelButton;
        this.editable = editable;

        if (emoteSlots.tabs() == 0) {
            // Empty Initialization
            this.tabButtons.add(new TabButton(0, 0, Component.empty(), widget -> this.setPageIndex(0)));
            this.wheelPages.add(new WheelPage(playerSkin));
        } else {
            for (int i = 0; i < emoteSlots.tabs(); i++) {
                this.tabButtons.add(
                    new TabButton(
                        0,
                        0,
                        Component.empty(),
                        button -> {
                            int j = this.tabButtons.indexOf(button);
                            this.setPageIndex(j);
                        }
                    )
                );

                WheelPage newPage = new WheelPage(this.playerSkin);
                newPage.relocateButtons(this.getRectangle());
                this.wheelPages.add(newPage);
            }

            emoteSlots.listEmotes(this::setWheelEmote);
            this.setPageIndex(0);
        }

        if (editable) {
            this.addPageButton = new TabButton(0, 0, Component.literal("+"), widget -> {
                this.addPage();
                this.setPageIndex(this.tabButtons.size() - 1);
                this.setFocused(this.tabButtons.getLast());
            });
            this.removePageButton = new TabButton(0, 0, Component.literal("-"), widget -> this.removeCurrentPage());
            this.removePageButton.active = this.wheelPages.size() > 1;
        } else {
            this.addPageButton = null;
            this.removePageButton = null;
        }
    }

    @Nullable
    public EmoteWheelButton getHoveringWheel() {
        return this.hoveringWheel;
    }

    @Nullable
    public EmoteWheelButton getLastPressedWheel() {
        return this.lastPressedWheel;
    }

    public void tick() {
        if (this.transitionTime > 0) {
            --this.transitionTime;
        } else {
            this.transitionType = null;
        }

        if (this.isHoveringValidEmoteButton()) {
            this.hoveringWheel.getModelPreviewer().getAnimator().setHardPause(false);
            this.hoveringWheel.getModelPreviewer().tick();
        } else if (this.getFocused() instanceof EmoteWheelButton wheelButton) {
            wheelButton.getModelPreviewer().getAnimator().setHardPause(false);
            wheelButton.getModelPreviewer().tick();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        WheelPage currentPage = this.wheelPages.get(this.pageIndex);
        currentPage.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        if (this.transitionTime > 0) {
            return;
        }

        for (TabButton tabButton : this.tabButtons) {
            tabButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.addPageButton != null && this.removePageButton != null) {
            this.addPageButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.removePageButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        String emoteTitle = null;

        if (this.isHoveringValidEmoteButton()) {
            emoteTitle = this.hoveringWheel.getEmote().value().title();
        } else if (this.getFocused() instanceof WheelPage wheelPage && wheelPage.getFocused() instanceof EmoteWheelButton emoteWheelButton && emoteWheelButton.getEmote() != null) {
            emoteTitle = emoteWheelButton.getEmote().value().title();
        }

        if (emoteTitle != null) {
            guiGraphics.drawCenteredString(this.font, Component.translatable(emoteTitle), this.getX() + this.width / 2, this.getY() + this.height / 2 - 4, 0xFFFFFFFF);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        if (this.transitionTime > 0) {
            return List.of();
        }

        List<AbstractWidget> children = new ArrayList<>(this.tabButtons);

        if (this.addPageButton != null && this.removePageButton != null) {
            children.add(this.addPageButton);
            children.add(this.removePageButton);
        }

        children.add(this.wheelPages.get(this.pageIndex));

        return children;
    }

    public boolean isHoveringValidEmoteButton() {
        return this.hoveringWheel != null && this.hoveringWheel.getEmote() != null;
    }

    public void destroyModelPreviewerBuffers() {
        for (WheelPage page : this.wheelPages) {
            for (EmoteWheelButton wheelButton : page.wheelButtons) {
                wheelButton.modelPreviewer.onDestroy();
            }
        }
    }

    public void setWheelEmote(int pageIndex, int wheelIndex, @Nullable Holder.Reference<Emote> emote) {
        this.wheelPages.get(pageIndex).wheelButtons[wheelIndex].setEmote(emote);
    }

    public void addPage() {
        if (this.addPageButton == null || this.removePageButton == null) {
            return;
        }

        this.tabButtons.add(
            new TabButton(
                0,
                0,
                Component.empty(),
                button -> {
                    int i = this.tabButtons.indexOf(button);
                    this.setPageIndex(i);
                }
            )
        );

        WheelPage newPage = new WheelPage(this.playerSkin);
        newPage.relocateButtons(this.getRectangle());
        this.wheelPages.add(newPage);
        this.setPageIndex(this.tabButtons.size() - 1);
        this.addPageButton.active = this.tabButtons.size() < 9;
        this.removePageButton.active = this.tabButtons.size() > 1;
        this.repositionTabButtons();
    }

    public void removeCurrentPage() {
        if (this.addPageButton == null || this.removePageButton == null) {
            return;
        }

        this.tabButtons.remove(this.pageIndex);
        this.wheelPages.remove(this.pageIndex);
        this.addPageButton.active = this.tabButtons.size() < 9;
        this.removePageButton.active = this.tabButtons.size() > 1;
        this.setPageIndex(Math.min(this.pageIndex, this.tabButtons.size() - 1));
        this.repositionTabButtons();
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int pages() {
        return this.wheelPages.size();
    }

    public boolean isEdited() {
        return this.editable && this.edited;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    @Override
    public void relocate(ScreenRectangle screenRectangle) {
        AnchoredWidget.super.relocate(screenRectangle);

        ScreenRectangle widgetRect = this.getRectangle();

        for (WheelPage wheelPage : this.wheelPages) {
            wheelPage.setPosition(this.getX(), this.getY());
            wheelPage.setSize(this.getWidth(), this.getHeight());

            for (EmoteWheelButton emoteWheelButton : wheelPage.wheelButtons) {
                emoteWheelButton.relocate(widgetRect);
            }
        }

        this.repositionTabButtons();
    }

    private void setHoveringWidget(@Nullable EmoteWheelButton emoteWheelButton) {
        if (this.hoveringWheel != null) {
            this.hoveringWheel.setBasePoseAndPause();
        }

        this.hoveringWheel = emoteWheelButton;
    }

    private void repositionTabButtons() {
        ScreenRectangle screenRectangle = this.getRectangle();
        int xStart = 0;

        for (TabButton tabButton : this.tabButtons) {
            tabButton.setX1(xStart);
            tabButton.relocate(screenRectangle);
            xStart += TabButton.WIDTH + 2;
        }

        if (this.addPageButton != null && this.removePageButton != null) {
            this.addPageButton.setX1(xStart);
            this.removePageButton.setX1(xStart + TabButton.WIDTH + 2);
            this.addPageButton.relocate(screenRectangle);
            this.removePageButton.relocate(screenRectangle);
        }
    }

    public void listEmotes(EmoteIterator task) {
        int pageIndex = 0;

        for (WheelPage wheelPage : this.wheelPages) {
            int wheelIndex = 0;

            for (EmoteWheelButton wheelButton : wheelPage.wheelButtons) {
                task.doWork(pageIndex, wheelIndex, wheelButton.getEmote());
                wheelIndex++;
            }

            pageIndex++;
        }
    }

    public void playTabTransition(TransitionType transitionType, int transitionTime) {
        this.transitionType = transitionType;
        this.transitionTime = transitionTime;
        this.maxTransitionTime = transitionTime;
    }

    public enum TransitionType {
        ZOOM_IN((PoseStack poseStack, AbstractWidget widget, ScreenRectangle parentRectangle, float interpolation) -> {
            float centerX = parentRectangle.left() + parentRectangle.width() / 2.0F;
            float centerY = parentRectangle.top() + parentRectangle.height() / 2.0F;

            poseStack.translate(centerX, centerY, 0);
            poseStack.scale(interpolation, interpolation, interpolation);
            poseStack.translate(-centerX, -centerY, 0);
        }),
        SLIDE((PoseStack poseStack, AbstractWidget widget, ScreenRectangle parentRectangle, float interpolation) -> {

        });

        final TransitionApplier transitionApplier;

        TransitionType(TransitionApplier transitionApplier) {
            this.transitionApplier = transitionApplier;
        }
    }

    @FunctionalInterface
    interface TransitionApplier {
        void apply(PoseStack poseStack, AbstractWidget widget, ScreenRectangle parentRectangle, float interpolation);
    }

    private class TabButton extends AnchoredButton {
        private static final int WIDTH = 15;
        private static final int HEIGHT = 12;

        private TabButton(
            int x1,
            int y1,
            Component message,
            OnPress onPress
        ) {
            super(
                x1,
                WIDTH,
                y1,
                HEIGHT,
                message,
                onPress,
                Button.DEFAULT_NARRATION,
                AnchoredWidget.HorizontalAnchorType.LEFT_WIDTH,
                AnchoredWidget.VerticalAnchorType.TOP_HEIGHT,
                BuiltInTheme.NUMBERED_TAB,
                widget -> widget.isHoveredOrFocused() || tabButtons.get(pageIndex) == widget
            );
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.theme.renderBackground(this, guiGraphics, mouseX, mouseY, partialTick);
            boolean pageWidget = this.getMessage().getContents() == PlainTextContents.EMPTY;

            guiGraphics.drawString(
                font,
                pageWidget ? Component.literal(String.valueOf(tabButtons.indexOf(this) + 1)) : this.getMessage(),
                this.getX() + Math.round(this.getWidth() / 2.0F - font.width("0") / 2.0F),
                this.getY() + Math.round(this.getHeight() / 2.0F - font.lineHeight / 2.0F),
                this.isActive() ? -1 : 0xFFB6B6B6,
                false
            );
        }
    }

    public class WheelPage extends AbstractContainerWidget {
        private final EmoteWheelButton[] wheelButtons;

        public WheelPage(PlayerSkin playerSkin) {
            super(0, 0, 0, 0, Component.empty());

            this.wheelButtons = new EmoteWheelButton[] {
                new EmoteWheelButton(-51, 45, -29, 54, AnchoredButton.BuiltInTheme.UPSIDE_EMOTE_WHEEL, playerSkin),
                new EmoteWheelButton(1, 45, -61, 54, AnchoredButton.BuiltInTheme.UPSIDE_EMOTE_WHEEL, playerSkin),
                new EmoteWheelButton(53, 45, -29, 54, AnchoredButton.BuiltInTheme.UPSIDE_EMOTE_WHEEL, playerSkin),
                new EmoteWheelButton(53, 45, 29, 54, AnchoredButton.BuiltInTheme.DOWNSIDE_EMOTE_WHEEL, playerSkin),
                new EmoteWheelButton(1, 45, 61, 54, AnchoredButton.BuiltInTheme.DOWNSIDE_EMOTE_WHEEL, playerSkin),
                new EmoteWheelButton(-51, 45, 29, 54, AnchoredButton.BuiltInTheme.DOWNSIDE_EMOTE_WHEEL, playerSkin)
            };
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            for (EmoteWheelButton emoteWheelButton : this.wheelButtons) {
                if (transitionType != null && transitionTime > 0) {
                    float animationScale = MathUtils.bezierCurve(1.0F - Math.max(transitionTime - partialTick, 0) / maxTransitionTime);
                    guiGraphics.pose().pushPose();
                    transitionType.transitionApplier.apply(guiGraphics.pose(), emoteWheelButton, getRectangle(), animationScale);
                }

                emoteWheelButton.render(guiGraphics, mouseX, mouseY, partialTick);

                if (transitionType != null && transitionTime > 0) {
                    guiGraphics.pose().popPose();
                }
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.wheelButtons);
        }

        @Nullable
        public EmoteWheelButton getFocusedWheelButton() {
            return this.getFocused() instanceof EmoteWheelButton emoteWheelButton ? emoteWheelButton : null;
        }

        private void relocateButtons(ScreenRectangle screenRectangle) {
            for (EmoteWheelButton emoteWheelButton : this.wheelButtons) {
                emoteWheelButton.relocate(screenRectangle);
            }
        }
    }

    public class EmoteWheelButton extends AnchoredButton {
        private final ModelPreviewer modelPreviewer;
        @Nullable
        private Holder.Reference<Emote> emote;

        public EmoteWheelButton(
            int x1,
            int x2,
            int y1,
            int y2,
            AnchoredButton.ButtonTheme theme,
            PlayerSkin playerSkin
        ) {
            super(
                x1,
                x2,
                y1,
                y2,
                Component.empty(),
                null,
                EmoteWheelButton.DEFAULT_NARRATION,
                AnchoredWidget.HorizontalAnchorType.CENTER_WIDTH,
                AnchoredWidget.VerticalAnchorType.CENTER_HEIGHT,
                theme,
                widget -> hoveringWheel == widget || (editable && lastPressedWheel == widget) || hoveringWheel == null && widget.isFocused()
            );

            this.modelPreviewer = new ModelPreviewer(
                font,
                -2,
                0,
                2,
                0,
                AnchoredWidget.HorizontalAnchorType.LEFT_RIGHT,
                AnchoredWidget.VerticalAnchorType.TOP_BOTTOM,
                Armatures.BIPED,
                Meshes.BIPED
            );

            this.modelPreviewer.setBackgroundClearColor(new Vec4f(0.0F, 0.0F, 0.0F, 0.0F));
            this.modelPreviewer.setFigureTexture(playerSkin.texture());
            this.modelPreviewer.setMesh(playerSkin.model() == PlayerSkin.Model.SLIM ? Meshes.ALEX : Meshes.BIPED);
            this.active = editable;
        }

        @Override
        public ScreenRectangle getRectangle() {
            return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.isActive() && this.isHovered() && hoveringWheel != this && (!editable || lastPressedWheel != this)) {
                setHoveringWidget(this);
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(EpicFightSounds.HOVER_WIDGET.get(), 1.0F));
            } else if (hoveringWheel == this && !this.isHovered()) {
                setHoveringWidget(null);
            }

            this.theme.renderBackground(this, guiGraphics, mouseX, mouseY, partialTick);

            if (this.emote != null) {
                this.modelPreviewer.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        private void setBasePoseAndPause() {
            if (this.emote != null) {
                this.modelPreviewer.getAnimator().playAnimationInstantly(this.emote.value().animation());
                this.modelPreviewer.getAnimator().getPlayerFor(this.emote.value().animation()).setElapsedTime(this.emote.value().snapshotTimeStamp());
                this.modelPreviewer.getAnimator().setHardPause(true);
            }
        }

        @Override
        public void relocate(ScreenRectangle screenRectangle) {
            super.relocate(screenRectangle);
            this.modelPreviewer.relocate(this.getRectangle());
        }

        @Override
        public void onPress() {
            onPressWheelButton.accept(this.emote);
            lastPressedWheel = this;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (this.active && this.visible) {
                if (CommonInputs.selected(keyCode)) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onPress();
                    return true;
                } else if (keyCode == InputConstants.KEY_DELETE && this.emote != null) {
                    this.setEmote(null);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public void setFocused(boolean flag) {
            super.setFocused(flag);

            if (flag) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(EpicFightSounds.HOVER_WIDGET.get(), 1.0F));
            }
        }

        public void setEmote(@Nullable Holder.Reference<Emote> emote) {
            this.emote = emote;

            if (!editable) {
                this.active = emote != null;
                edited = true;
            }

            if (emote != null) {
                this.modelPreviewer.addAnimationToPlay(this.emote.value().animation());
                this.setBasePoseAndPause();
                this.modelPreviewer.setCameraTransform(
                    this.emote.value().previewCameraTransform().zoom(),
                    this.emote.value().previewCameraTransform().xRot(),
                    this.emote.value().previewCameraTransform().yRot(),
                    this.emote.value().previewCameraTransform().xMove(),
                    this.emote.value().previewCameraTransform().yMove()
                );
            } else {
                this.modelPreviewer.clearAnimations();
            }
        }

        @Nullable
        public Holder.Reference<Emote> getEmote() {
            return this.emote;
        }

        public ModelPreviewer getModelPreviewer() {
            return this.modelPreviewer;
        }
    }

    /*******************************************************************
     * {@link AnchoredWidget} implementations                          *
     *******************************************************************/
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
}
