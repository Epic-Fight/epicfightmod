package yesman.epicfight.client.gui.screen;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangeSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

@OnlyIn(Dist.CLIENT)
public class SkillEditScreen extends Screen {
	private static final ResourceLocation SKILL_EDIT_UI = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "textures/gui/screen/skill_edit.png");
	private static final MutableComponent NO_SKILLS = Component.translatable("gui.epicfight.no_skills");
	private static final int MAX_SHOWING_BUTTONS = 6;
	
	private final Player player;
	private final CapabilitySkill skills;
	private final Map<SkillSlot, SlotButton> slotButtons = Maps.newHashMap();
	private final List<LearnSkillButton> learnedSkillButtons = Lists.newArrayList();
	private int start;
	private SlotButton selectedSlotButton;
	
	public SkillEditScreen(Player player, CapabilitySkill skills) {
		super(Component.translatable("gui.epicfight.skill_edit"));
		this.player = player;
		this.skills = skills;
	}
	
	@Override
	public void init() {
		int i = this.width / 2 - 96;
		int j = this.height / 2 - 82;
		
		this.slotButtons.clear();
		this.learnedSkillButtons.clear();
		
		for (SkillSlot skillSlot : SkillSlot.ENUM_MANAGER.universalValues()) {
			if ((this.player.isCreative() || !this.skills.getSkillContainersFor(skillSlot.category()).isEmpty()) && skillSlot.category().learnable()) {
				SlotButton slotButton = new SlotButton(i, j, 18, 18, skillSlot, this.skills.getSkillContainerFor(skillSlot).getSkill(), (button) -> {
					this.start = 0;
					
					for (Button shownButton : this.learnedSkillButtons) {
						this.children().remove(shownButton);
					}
					
					this.learnedSkillButtons.clear();
					int k = this.width / 2 - 69;
					
					MutableInt widgetHeight = new MutableInt(this.height / 2 - 78);
					Stream<Skill> learnedSkillCollection = this.player.isCreative() ? SkillManager.getSkills((skill) -> skill.getCategory() == skillSlot.category()).stream() : this.skills.listAcquiredSkills().filter(skill -> skill.getCategory() == skillSlot.category());
					
					learnedSkillCollection.forEach(skill -> {
						this.learnedSkillButtons.add(new LearnSkillButton(k, widgetHeight.intValue(), 147, 24, skill, Component.translatable(skill.getTranslationKey()), (pressedButton) -> {
							if (this.minecraft.player.experienceLevel >= skill.getRequiredXp() || this.minecraft.player.isCreative()) {
								if (!this.canPress(pressedButton)) {
									return;
								}
								
								this.skills.getSkillContainerFor(skillSlot).setSkill(skill);
								EpicFightNetworkManager.sendToServer(new CPChangeSkill(skillSlot, -1, !this.minecraft.player.isCreative(), skill));
								this.skills.addLearnedSkill(skill);
								
								this.onClose();
							}
						}).setActive(this.skills.getSkillContainer(skill) == null));
						
						widgetHeight.add(26);
					});
					
					for (Button shownButton : this.learnedSkillButtons) {
						this.addWidget(shownButton);
					}
					
					this.selectedSlotButton = (SlotButton)button;
					
				}, Component.translatable(SkillSlot.ENUM_MANAGER.toTranslated(skillSlot)));
				
				this.slotButtons.put(skillSlot, slotButton);
				this.addWidget(slotButton);
				j+=18;
			}
		}
		
		if (this.selectedSlotButton != null) {
			this.selectedSlotButton = this.slotButtons.get(this.selectedSlotButton.slot);
			this.selectedSlotButton.onPress();
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		
		if (this.canScroll()) {
			int scrollPosition = (int)(140 * (this.start / (float)(this.learnedSkillButtons.size() - MAX_SHOWING_BUTTONS)));
			guiGraphics.blit(SKILL_EDIT_UI, this.width / 2 + 80, this.height / 2 - 80 + scrollPosition, 12, 15, 231, 2, 12, 15, 256, 256);
		}
		
		int maxShowingButtons = Math.min(this.learnedSkillButtons.size(), MAX_SHOWING_BUTTONS);
		
		for (int i = this.start; i < maxShowingButtons + this.start; ++i) {
			this.learnedSkillButtons.get(i).render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		
		for (SlotButton sb : this.slotButtons.values()) {
			sb.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		
		if (this.slotButtons.isEmpty()) {
			int lineHeight = 0;
			
			for (FormattedCharSequence s : this.font.split(NO_SKILLS, 140)) {
				guiGraphics.drawString(this.font, s, this.width / 2 - 65, this.height / 2 - 72 + lineHeight, 3158064, false);
				
				lineHeight += 10;
			}
		}
	}
	
	@Override
	public void renderBackground(GuiGraphics guiGraphics) {
		super.renderBackground(guiGraphics);
		guiGraphics.blit(SKILL_EDIT_UI, this.width / 2 - 104, this.height / 2 - 100, 0, 0, 208, 200);
	}
	
	private boolean canScroll() {
		return this.learnedSkillButtons.size() > MAX_SHOWING_BUTTONS;
	}
	
	private boolean canPress(Button button) {
		int buttonOrder = this.learnedSkillButtons.indexOf(button);
		
		return buttonOrder >= this.start && buttonOrder <= this.start + MAX_SHOWING_BUTTONS;
	}
	
	@Override
	public boolean mouseScrolled(double x, double y, double wheel) {
		if (!this.canScroll()) {
			return false;
		} else {
			if (wheel > 0.0F) {
				if (this.start > 0) {
					--this.start;
					
					for (Button button : this.learnedSkillButtons) {
						button.setY(button.getY() + 26);
					}
					
					return true;
				}
			} else {
				if (this.start < (this.learnedSkillButtons.size() - MAX_SHOWING_BUTTONS)) {
					++this.start;
					
					for (Button button : this.learnedSkillButtons) {
						button.setY(button.getY() - 26);
					}
					
					return true;
				}
			}
			
			return false;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	class SlotButton extends Button {
		private final Skill iconSkill;
		private final SkillSlot slot;

		public SlotButton(int x, int y, int width, int height, SkillSlot slot, Skill skill, OnPress pressedAction, Component tooltipMessage) {
			super(x, y, width, height, Component.empty(), pressedAction, Button.DEFAULT_NARRATION);
			this.iconSkill = skill;
			this.slot = slot;
			this.setTooltip(Tooltip.create(tooltipMessage));
		}
		
		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			super.render(guiGraphics, mouseX, mouseY, partialTicks);
		}
		
		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			int y = (this.isHovered || SkillEditScreen.this.selectedSlotButton == this) ? 35 : 17;
			guiGraphics.blit(SKILL_EDIT_UI, this.getX(), this.getY(), 237, y, this.width, this.height);
			
			if (this.iconSkill != null) {
				RenderSystem.enableBlend();
				guiGraphics.blit(this.iconSkill.getSkillTexture(), this.getX() + 1, this.getY() + 1, 16, 16, 0, 0, 128, 128, 128, 128);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	class LearnSkillButton extends Button {
		private final Skill skill;

		public LearnSkillButton(int x, int y, int width, int height, Skill skill, Component title, OnPress pressedAction) {
			super(x, y, width, height, title, pressedAction, Button.DEFAULT_NARRATION);
			this.skill = skill;
		}
		
		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
			int texY = (this.isHovered || !this.active) ? 224 : 200;
			guiGraphics.blit(SKILL_EDIT_UI, this.getX(), this.getY(), 0, texY, this.width, this.height);
			
			RenderSystem.enableBlend();
			guiGraphics.blit(this.skill.getSkillTexture(), this.getX() + 5, this.getY() + 4, 16, 16, 0, 0, 128, 128, 128, 128);
			guiGraphics.drawString(SkillEditScreen.this.font, this.getMessage(), this.getX()+26, this.getY() + 2, -1, false);
			
			if (this.active) {
				int color = (SkillEditScreen.this.minecraft.player.experienceLevel >= this.skill.getRequiredXp() || SkillEditScreen.this.minecraft.player.isCreative()) ? 8453920 : 16736352;
				guiGraphics.drawString(SkillEditScreen.this.font, Component.translatable("gui.epicfight.changing_cost", this.skill.getRequiredXp()), this.getX()+70, this.getY() + 12, color, false);
			} else {
				guiGraphics.drawString(SkillEditScreen.this.font, Component.literal(
						SkillEditScreen.this.skills.getSkillContainer(this.skill).getSlot().toString().toLowerCase(Locale.ROOT)), this.getX()+26, this.getY() + 12, 16736352, false);
			}
		}
		
		@Override
		public boolean mouseClicked(double x, double y, int pressType) {
			if (this.visible && pressType == 1) {
				boolean flag = this.clickedNoCountActive(x, y);
				
				if (flag) {
					this.playDownSound(Minecraft.getInstance().getSoundManager());
					SkillEditScreen.this.minecraft.setScreen(new SkillBookScreen(SkillEditScreen.this.player, this.skill, null, SkillEditScreen.this));
					return true;
				}
			}
			
			return super.mouseClicked(x, y, pressType);
		}
		
		protected boolean clickedNoCountActive(double x, double y) {
			return this.visible && x >= (double) this.getX() && y >= (double) this.getY() && x < (double) (this.getX() + this.width) && y < (double) (this.getY() + this.height);
		}
		
		public LearnSkillButton setActive(boolean active) {
			this.active = active;
			return this;
		}
	}
}