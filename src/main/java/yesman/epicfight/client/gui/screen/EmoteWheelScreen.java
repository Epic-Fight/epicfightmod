package yesman.epicfight.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.gui.widgets.AnchoredButton;
import yesman.epicfight.client.gui.widgets.EmoteWheelTab;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.generated.LangKeys;

public class EmoteWheelScreen extends Screen {
    private static final int ANIMATION_TIME = 6;

    private final EmoteWheelTab emoteWheelTab;
    private final AnchoredButton editEmoteButton;

    private int showUpAnimTime;
    private boolean shouldHoldToPresist = true;

    public EmoteWheelScreen(LocalPlayerPatch playerpatch) {
        super(Component.empty());

        // Load minecraft instance preemptively
        this.minecraft = Minecraft.getInstance();

        this.emoteWheelTab = new EmoteWheelTab(
            this.minecraft.font,
            playerpatch.getEmoteSlots(),
            playerpatch.getOriginal().getSkin(),
            10,
            10,
            25,
            25,
            AnchoredWidget.HorizontalAnchorType.LEFT_RIGHT,
            AnchoredWidget.VerticalAnchorType.TOP_BOTTOM,
            emote -> {
                playerpatch.playAnimationInClientSide(emote.value().animation(), 0.0F);
                this.onClose();
            },
            false
        );
        this.emoteWheelTab.playTabTransition(EmoteWheelTab.TransitionType.ZOOM_IN, ANIMATION_TIME);

        this.editEmoteButton =
            AnchoredButton.buttonBuilder(Component.translatable(LangKeys.GUI_MESSAGE_EMOTE_EDIT), button -> Minecraft.getInstance().setScreen(new EmoteEditScreen(playerpatch)))
                .horizontalAnchorType(AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH)
                .verticalAnchorType(AnchoredWidget.VerticalAnchorType.TOP_HEIGHT)
                .xParams(10, 80)
                .yParams(20, 18)
                .theme(AnchoredButton.BuiltInTheme.BLACK)
                .highlihgtFontWhen(AbstractWidget::isHoveredOrFocused)
            .build();

        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SPYGLASS_USE, 1.0F, 5.0F));
    }

    @Override
    public void init() {
        this.addRenderableWidget(this.emoteWheelTab);
        this.addRenderableWidget(this.editEmoteButton);
        this.repositionElements();
    }

    @Override
    protected void rebuildWidgets() {
        this.emoteWheelTab.relocate(this.getRectangle());
        this.editEmoteButton.relocate(this.getRectangle());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        for (Renderable renderable : this.renderables) {
            if (this.showUpAnimTime < ANIMATION_TIME && renderable != this.emoteWheelTab) {
                continue;
            }

            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        float animationScale = MathUtils.bezierCurve(Math.min(this.showUpAnimTime + partialTick, ANIMATION_TIME) / ANIMATION_TIME);
        guiGraphics.fillGradient(0, 0, this.width, (int)(animationScale * (this.height / 3)), 0xFF0A0A0A, 0x00000000);
        guiGraphics.fillGradient(0, (int)(this.height - animationScale * (this.height / 3)), this.width, this.height, 0x00000000, 0xFF0A0A0A);
    }

    @Override
    public void tick() {
        // FYI: keyReleased is unreliable since players can release the key before the screen is fully loaded and
        // prepared to call keyReleased method. Instead, we check key press in each tick so we won't miss the release
        // event which is one-shot.
        if (this.shouldHoldToPresist && !InputManager.isActionPhysicallyActive(EpicFightInputAction.OPEN_EMOTE_WHEEL_SCREEN)) {
            if (this.emoteWheelTab.isHoveringValidEmoteButton()) {
                this.emoteWheelTab.getHoveringWheel().onPress();
            } else if (this.emoteWheelTab.getFocused() instanceof EmoteWheelTab.WheelPage wheelTab && wheelTab.getFocusedWheelButton() != null) {
                wheelTab.getFocusedWheelButton().onPress();
            }

            this.onClose();
        }

        ++this.showUpAnimTime;

        this.emoteWheelTab.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.emoteWheelTab.destroyModelPreviewerBuffers();
    }

    public void relieveHolding() {
        this.shouldHoldToPresist = false;
    }
}
