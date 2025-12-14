package yesman.epicfight.skill.passive;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;

public abstract class PassiveSkill extends Skill {
	public static SkillBuilder<PassiveSkill> createPassiveBuilder() {
		return (new SkillBuilder<PassiveSkill>()).setCategory(SkillCategories.PASSIVE).setResource(Resource.NONE);
	}
	
	public PassiveSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
		guiGraphics.drawString(gui.getFont(), remainTime, x + 12 - 4 * remainTime.length(), (y+6), 16777215, true);
		poseStack.popPose();
	}
}