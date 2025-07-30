package yesman.epicfight.skill.modules;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

public interface HoldableSkill
{
    void holdTick(SkillContainer container);

    default Skill asSkill() {
        return (Skill)this;
    }

    @OnlyIn(Dist.CLIENT)
    KeyMapping getKeyMapping();

}
