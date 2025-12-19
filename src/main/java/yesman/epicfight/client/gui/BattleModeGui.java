package yesman.epicfight.client.gui;

import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.ScreenCalculations.AlignDirection;
import yesman.epicfight.client.gui.ScreenCalculations.HorizontalBasis;
import yesman.epicfight.client.gui.ScreenCalculations.VerticalBasis;
import yesman.epicfight.client.gui.screen.config.UISetupScreen;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.modules.ChargeableSkill;

public class BattleModeGui {
	private Minecraft minecraft;
	private int sliding;
	private boolean slidingToggle;
	private final List<SkillContainer> skillIcons = new LinkedList<> ();

	public BattleModeGui(Minecraft minecraft) {
		this.sliding = 29;
		this.slidingToggle = false;
		this.minecraft = minecraft;
	}
	
	public void renderTick() {
		if (this.sliding > 50) {
			return;
		} else if (this.sliding > 0) {
			if (this.slidingToggle) {
				this.sliding -= 2;
			} else {
				this.sliding += 2;
			}
		}
	}
	
	public void renderStaminaBar(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (Minecraft.getInstance().screen instanceof UISetupScreen) {
			return;
		}
		
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch == null) {
			return;
		}
		
		float maxStamina = playerpatch.getMaxStamina();
		float stamina = playerpatch.getStamina();
		
		if (maxStamina > 0.0F && stamina < maxStamina) {
			Vec2i pos = ClientConfig.getStaminaPosition(screenWidth, screenHeight);
			float prevStamina = playerpatch.getStaminaO();
			float ratio = (prevStamina + (stamina - prevStamina) * partialTick) / maxStamina;

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, this.sliding, 0);
			RenderSystem.setShaderColor(1.0F, ratio, 0.25F, 1.0F);
			guiGraphics.blit(EntityUI.BATTLE_ICON, pos.x, pos.y, 118, 4, 2, 38, 237, 9, 255, 255);
			guiGraphics.blit(EntityUI.BATTLE_ICON, pos.x, pos.y, (int)(118*ratio), 4, 2, 47, (int)(237*ratio), 9, 255, 255);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.pose().popPose();
		}
	}
	
	public void renderNormalSkills(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (Minecraft.getInstance().screen instanceof UISetupScreen) {
			return;
		}
		
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch == null) {
			return;
		}
		
		for (SkillSlot slot : SkillSlot.ENUM_MANAGER.universalValues()) {
			if (slot == SkillSlots.WEAPON_INNATE) {
				continue;
			}
			
			SkillContainer container = playerpatch.getSkill(slot);
			
			if (!container.isEmpty()) {
				if (!this.skillIcons.contains(container) && container.getSkill().shouldDraw(container)) {
					this.skillIcons.add(container);
				}
			}
		}
		
		this.skillIcons.removeIf(skillContainer -> skillContainer.isEmpty() || !skillContainer.getSkill().shouldDraw(skillContainer));
		AlignDirection alignDirection = ClientConfig.passiveAlignDirection;
		HorizontalBasis horBasis = ClientConfig.passiveBaseX;
		VerticalBasis verBasis = ClientConfig.passiveBaseY;
		int passiveX = horBasis.positionGetter.apply(screenWidth, ClientConfig.passiveX);
		int passiveY = verBasis.positionGetter.apply(screenHeight, ClientConfig.passiveY);
		int icons = this.skillIcons.size();
		Vec2i slotCoord = alignDirection.startCoordGetter.get(passiveX, passiveY, 24, 24, icons, horBasis, verBasis);
		
		for (SkillContainer container : this.skillIcons) {
			if (!container.isEmpty()) {
				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				container.getSkill().drawOnGui(this, container, guiGraphics, slotCoord.x, slotCoord.y, partialTick);
				slotCoord = alignDirection.nextPositionGetter.getNext(horBasis, verBasis, slotCoord, 24, 24);
			}
		}
	}
	
	public void renderWeaponInnateSkill(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (Minecraft.getInstance().screen instanceof UISetupScreen) {
			return;
		}
		
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch == null) {
			return;
		}
		
		SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);

		if (!container.isEmpty() && container.getSkill().shouldDraw(container)) {
			Window sr = Minecraft.getInstance().getWindow();
			int width = sr.getGuiScaledWidth();
			int height = sr.getGuiScaledHeight();
			Vec2i pos = ClientConfig.getWeaponInnatePosition(width, height);
			container.getSkill().drawOnGui(this, container, guiGraphics, pos.x, pos.y, partialTick);
		}
	}
	
	public void renderCharingBar(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
		if (Minecraft.getInstance().screen instanceof UISetupScreen) {
			return;
		}
		
		LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
		
		if (playerpatch == null) {
			return;
		}
		
		if (playerpatch.isHoldingAny() && playerpatch.getHoldingSkill() instanceof ChargeableSkill chargeableSkill) {
			int chargeAmount = playerpatch.getChargingAmount();
			int prevChargingAmount = playerpatch.getPrevChargingAmount();
			float ratio = Math.min((prevChargingAmount + (chargeAmount - prevChargingAmount) * partialTick) / chargeableSkill.getMaxChargingTicks(), 1.0F);
			Vec2i pos = ClientConfig.getChargingBarPosition(screenWidth, screenHeight);

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, this.sliding, 0);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.blit(EntityUI.BATTLE_ICON, pos.x, pos.y, 1, 71, 238, 13, 255, 255);
			guiGraphics.blit(EntityUI.BATTLE_ICON, pos.x, pos.y, 1, 57, (int)(238 * ratio), 13, 255, 255);

			ResourceLocation rl = ResourceLocation.parse(chargeableSkill.toString());
			String skillName = Component.translatable(String.format("skill.%s.%s", rl.getNamespace(), rl.getPath())).getString();
			
			int stringWidth = this.minecraft.font.width(skillName);
			guiGraphics.drawString(this.minecraft.font, skillName, (pos.x + 120 - stringWidth * 0.5F), pos.y - 12, 16777215, true);

			guiGraphics.pose().popPose();
		}
	}
	
	public void slideUp() {
		this.sliding = 49;
		this.slidingToggle = true;
	}

	public void slideDown() {
		this.sliding = 1;
		this.slidingToggle = false;
	}
	
	public void init() {
		this.skillIcons.clear();
	}

	public int getSlidingProgression() {
		return this.sliding;
	}
	
	public Font getFont() {
		return this.minecraft.font;
	}
}