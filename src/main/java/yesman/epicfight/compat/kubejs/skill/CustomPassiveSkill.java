package yesman.epicfight.compat.kubejs.skill;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

public class CustomPassiveSkill extends CustomSkill {
    public CustomPassiveSkill(CustomSkillBuilder builder) {
        super(builder);
    }


    @Info("""
            Creates a custom passive skill.
            This builder type is basically just a preset for a passive skill.
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
