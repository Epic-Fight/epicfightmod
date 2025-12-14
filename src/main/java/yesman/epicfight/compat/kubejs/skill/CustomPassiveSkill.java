package yesman.epicfight.compat.kubejs.skill;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

public class CustomPassiveSkill extends CustomSkill {
    public CustomPassiveSkill(CustomSkillBuilder builder) {
        super(builder);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0.0F, (float)gui.getSlidingProgression(), 0.0F);
        guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0.0F, 0.0F, 1, 1, 1, 1);
        String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
        guiGraphics.drawString(gui.getFont(), remainTime, x + 12.0F - (float)(4 * remainTime.length()), y + 6.0F, 16777215, true);
        poseStack.popPose();
    }

    @Info("""
            Creates a custom passive skill.
            This builder type is basically just a preset for a passive skill.
            Ideally, you should not override `drawOnGui()`, as that is pre-set for passive skills. Otherwise, it wouldn't be necessary to use this builder type.
            """)
    public static class CustomPassiveSkillBuilder extends CustomSkillBuilder {
        public CustomPassiveSkillBuilder(ResourceLocation id) {
            super(id);
        }

        @Override
        public Skill createObject() {
            return new CustomPassiveSkill(this);
        }
    }
}
