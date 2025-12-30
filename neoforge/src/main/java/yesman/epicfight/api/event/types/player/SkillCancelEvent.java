package yesman.epicfight.api.event.types.player;

import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillCancelEvent extends LivingEntityPatchEvent {
	private final SkillContainer skillContainer;
	
	public SkillCancelEvent(PlayerPatch<?> playerpatch, SkillContainer skillContainer) {
		super(playerpatch);
		
		this.skillContainer = skillContainer;
	}
	
	public SkillContainer getSkillContainer() {
		return this.skillContainer;
	}

    public final PlayerPatch<?> getPlayerPatch() {
        return (PlayerPatch<?>)this.getEntityPatch();
    }
}