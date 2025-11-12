package yesman.epicfight.compat.kubejs;

import dev.latvian.mods.kubejs.typings.Info;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.neoevent.playerpatch.SkillCastEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.skill.Skill;

public class EFUtilsJS {
    @OnlyIn(Dist.CLIENT)
    @Info("""
            Requests the server to execute a skill. Called from the client.
            """)
    public static SkillCastEvent requestExecuteSkill(Skill skill) {
        return ClientEngine.getInstance().getPlayerPatch().getSkill(skill).sendCastRequest(ClientEngine.getInstance().getPlayerPatch(), ControlEngine.getInstance());
    }
}
