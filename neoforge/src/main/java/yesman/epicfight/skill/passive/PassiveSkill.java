package yesman.epicfight.skill.passive;

import net.minecraft.client.gui.GuiGraphics;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;

import java.util.function.Function;

public abstract class PassiveSkill extends Skill {
	public static SkillBuilder<?> createPassiveBuilder(Function<SkillBuilder<?>, ? extends PassiveSkill> constructor) {
		return new SkillBuilder<>(constructor).setCategory(SkillCategories.PASSIVE).setResource(Resource.NONE);
	}
	
	@SuppressWarnings("rawtypes")
	public PassiveSkill(SkillBuilder<? extends SkillBuilder> builder) {
		super(builder);
	}
	
	@Override @ClientOnly
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
		guiGraphics.drawString(gui.getFont(), remainTime, x + 12 - 4 * remainTime.length(), (y+6), 16777215, true);
	}
}