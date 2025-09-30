package yesman.epicfight.client.gui.screen;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillContainer;

@OnlyIn(Dist.CLIENT)
public class SlotSelectScreen extends Screen {
	private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "textures/gui/screen/slot_select.png");
	private final SkillBookScreen parent;
	private final List<SkillContainer> containers;
	
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
		int k = this.width / 2 - 80;
		int l = this.height / 2 - 45;
		
		for (SkillContainer container : this.containers) {
			String slotName = container.getSlot().toString().toLowerCase(Locale.ROOT);
			String skillName = container.getSkill() == null ? "Empty" : Component.translatable(container.getSkill().getTranslationKey()).getString();
			
			SlotButton slotbutton = new SlotButton(k, l, 167, 17, Component.literal(slotName + ": "+ skillName), (button) -> {
				this.parent.acquireSkillTo(container);
				
				if (Minecraft.getInstance().screen == this) {
					this.onClose();
				}
			});
			
			l+=22;
			
			this.addRenderableWidget(slotbutton);
		}
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int posX = (this.width - 184) / 2;
		int posY = (this.height - 150) / 2;
		
		this.parent.render(guiGraphics, mouseX, mouseY, partialTicks, true);
		
		// move z level, to prevent the button text displayed above the screen.
		guiGraphics.pose().translate(0, 0, 5000);
		
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		guiGraphics.blit(BACKGROUND, posX, posY, 0, 0, 191, 154);
		
		Component component = Component.translatable("gui.epicfight.select_slot_tooltip");
		int lineHeight = 0;
		
		for (FormattedCharSequence s : this.font.split(component, 250)) {
			guiGraphics.drawString(font, s, this.width / 2 - 84, this.height / 2 - 66 + lineHeight, 3158064, false);
			
			lineHeight += 10;
		}
		
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}
	
	@OnlyIn(Dist.CLIENT)
	class SlotButton extends Button {
		public SlotButton(int x, int y, int width, int height, Component title, OnPress pressedAction) {
			super(x, y, width, height, title, pressedAction, Button.DEFAULT_NARRATION);
		}
		
		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
			int y = (this.isHovered || !this.active) ? 171 : 154;
			guiGraphics.blit(BACKGROUND, this.getX(), this.getY(), 0, y, this.width, this.height);
			guiGraphics.drawString(SlotSelectScreen.this.font, this.getMessage(), this.getX()+3, this.getY()+3, -1, false);
		}
	}
}