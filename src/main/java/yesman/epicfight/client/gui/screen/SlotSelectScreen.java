package yesman.epicfight.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.generated.LangKeys;
import yesman.epicfight.skill.SkillContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SlotSelectScreen extends Screen {
    private static final int MAX_ROWS = 2;
    private static final int MAX_COLUMNS = 3;
    private static final int STRIDE = 60;

    private final SkillBookScreen parent;
    private final List<SkillContainer> containers;
    private final List<SlotButton> slotButtons = new ArrayList<> ();

    private int maxScroll;
    private int scroll = 0;

    public SlotSelectScreen(Set<SkillContainer> containers, SkillBookScreen parent) {
        super(Component.empty());
        this.parent = parent;
        this.containers = containers.stream().sorted((c1, c2) -> {
            if (c1.getSlotId() > c2.getSlotId()) {
                return 1;
            } else if (c1.getSlotId() < c2.getSlotId()) {
                return -1;
            }

            return 0;
        }).toList();
    }

    @Override
    protected void init() {
        this.parent.init(this.minecraft, this.width, this.height);
        int buttonX = this.width / 2 - 84;
        int buttonY = this.height / 2 - 40;
        int columns = 0;

        this.maxScroll = Math.max((this.containers.size() / MAX_COLUMNS + 1) - MAX_ROWS, 0);

        if (this.maxScroll > 0) {
            this.addRenderableWidget(new ScrollArrow(this.width / 2 - 8, this.height / 2 - 98, 16, 16, button -> this.scrollUp(), true));
            this.addRenderableWidget(new ScrollArrow(this.width / 2 - 8, this.height / 2 + 82, 16, 16, button -> this.scrollDown(), false));
        }

        this.slotButtons.clear();

        for (SkillContainer container : this.containers) {
            String slotName = container.getSlot().toString().toLowerCase(Locale.ROOT);
            String skillName = container.getSkill() == null ? "Empty" : Component.translatable(container.getSkill().getTranslationKey()).getString();

            SlotButton slotbutton =
                new SlotButton(
                    buttonX,
                    buttonY,
                    48,
                    48,
                    Component.literal(slotName + ": " + skillName),
                    container.getSkill() == null ? SkillEditScreen.EMPTY_SKILL_SLOT_ICON : container.getSkill().getSkillTexture(),
                    button -> {
                        this.parent.acquireSkillTo(container);

                        if (this.minecraft.screen == this) {
                            this.onClose();
                        }
                    }
                );

            if (!this.parent.consumesItem() && container.onReplaceCooldown()) {
                slotbutton.active = false;
                slotbutton.setTooltip(
                    Tooltip.create(
                        Component
                            .literal(slotName + ": "+ skillName + "\n")
                            .append(
                                Component.translatable(
                                    LangKeys.GUI_MESSAGE_SELECT_SKILL_CONTAINER_ON_COOLDOWN,
                                    container.getReplaceCooldown() / 20
                                )
                            )
                    )
                );
            }

            buttonX+=STRIDE;
            columns++;

            if (columns >= MAX_COLUMNS) {
                buttonX = this.width / 2 - 84;
                buttonY += STRIDE;
                columns = 0;
            }

            this.slotButtons.add(slotbutton);
            this.addRenderableWidget(slotbutton);
        }

        this.scroll = 0;
        this.setScrollVisibilities();
    }

    protected void scrollUp() {
        int nextScroll = Mth.clamp(this.scroll - 1, 0, this.maxScroll);

        if (this.scroll != nextScroll) {
            this.scroll = nextScroll;
            this.slotButtons.forEach(button -> button.setY(button.getY() + STRIDE));
            this.setScrollVisibilities();
        }
    }

    protected void scrollDown() {
        int nextScroll = Mth.clamp(this.scroll + 1, 0, this.maxScroll);

        if (this.scroll != nextScroll) {
            this.scroll = nextScroll;
            this.slotButtons.forEach(button -> button.setY(button.getY() - STRIDE));
            this.setScrollVisibilities();
        }
    }

    protected void setScrollVisibilities() {
        int i = 0;

        for (SlotButton slotButton : this.slotButtons) {
            slotButton.visible = i / MAX_COLUMNS >= this.scroll && i / MAX_COLUMNS < this.scroll + MAX_ROWS;
            i++;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double xScroll, double yScroll) {
        if (yScroll > 0) this.scrollUp();
        else this.scrollDown();
        return true;
    }

    @Override
    public void onClose() {
        if (this.parent != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int boxWidth = 192;
        int boxHeight = 160;
        int left = (this.width - boxWidth) / 2;
        int top = (this.height - boxHeight) / 2;

        this.parent.render(guiGraphics, mouseX, mouseY, partialTick, true);
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // move z level, to prevent the button text displayed above the screen.
        guiGraphics.pose().translate(0, 0, 5000);
        guiGraphics.fill(left, top, left + boxWidth, top + boxHeight, -6250336);
        guiGraphics.fill(left + 1, top + 1, left + boxWidth - 1, top + boxHeight - 1, -16777215);

        Component component = Component.translatable(LangKeys.GUI_MESSAGE_SELECT_SKILL_SELECT_SLOT_TOOLTIP);
        int lineHeight = 0;

        for (FormattedCharSequence s : this.font.split(component, 250)) {
            guiGraphics.drawString(font, s, (this.width - boxWidth) / 2 + 8, this.height / 2 - 70 + lineHeight, 0xFFE8E8E8, false);
            lineHeight += 10;
        }

        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    class ScrollArrow extends Button {
        final boolean up;

        protected ScrollArrow(int x, int y, int width, int height, Button.OnPress onPress, boolean up) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);

            this.up = up;
        }

        @Override
        protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (this.up && scroll != 0) {
                guiGraphics.blit(SkillEditScreen.SCROLL_ARROW_UP, this.getX(), this.getY(), this.width, this.height, 0, 0, 16, 16, 16, 16);
            } else if (!this.up && scroll != maxScroll) {
                guiGraphics.blit(SkillEditScreen.SCROLL_ARROW_DOWN, this.getX(), this.getY(), this.width, this.height, 0, 0, 16, 16, 16, 16);
            }
        }

        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            return super.clicked(mouseX, mouseY) && (this.up && scroll != 0 || !this.up && scroll != maxScroll);
        }
    }

    static class SlotButton extends Button {
        final ResourceLocation texture;

        protected SlotButton(int x, int y, int width, int height, Component tooltip, ResourceLocation textureLocation, Button.OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.texture = textureLocation;
            this.setTooltip(Tooltip.create(tooltip));
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

            RenderSystem.enableBlend();
            if (!this.active) RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
            guiGraphics.blit(this.texture, this.getX() + 2, this.getY() + 2, this.width - 4, this.height - 4, 0, 0, 32, 32, 32, 32);
            if (!this.active) RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }
}