package yesman.epicfight.compat.kubejs;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

public class EFUtilsJS {
    @OnlyIn(Dist.CLIENT)
    @Info("""
            Requests the server to execute a skill. Called from the client.
            """)
    public static SkillCastEvent requestExecuteSkill(Skill skill) {
        return ClientEngine.getInstance().getPlayerPatch().getSkill(skill).sendCastRequest(ClientEngine.getInstance().getPlayerPatch(), ClientEngine.getInstance().controlEngine);
    }
}
