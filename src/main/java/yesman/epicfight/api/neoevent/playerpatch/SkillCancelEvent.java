package yesman.epicfight.api.neoevent.playerpatch;

import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillCancelEvent extends PlayerPatchEvent<PlayerPatch<?>> {
	private final SkillContainer skillContainer;
	
	public SkillCancelEvent(PlayerPatch<?> playerpatch, SkillContainer skillContainer) {
		super(playerpatch);
		
		this.skillContainer = skillContainer;
	}
	
	public SkillContainer getSkillContainer() {
		return this.skillContainer;
	}
}