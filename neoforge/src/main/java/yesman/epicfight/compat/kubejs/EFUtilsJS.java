package yesman.epicfight.compat.kubejs;

import dev.latvian.mods.kubejs.typings.Info;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class EFUtilsJS {
    @ClientOnly
    @Info("""
            Requests the server to execute a skill. Called from the client.
            """)
    public static SkillCastEvent requestExecuteSkill(Skill skill) {
        return EpicFightCapabilities.getCachedLocalPlayerPatch().getSkill(skill).sendCastRequest(EpicFightCapabilities.getCachedLocalPlayerPatch(), ControlEngine.getInstance());
    }
}
