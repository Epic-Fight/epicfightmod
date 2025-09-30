package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillCancelEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private final SkillContainer skillContainer;
	
	public SkillCancelEvent(PlayerPatch<?> playerpatch, SkillContainer skillContainer) {
		super(playerpatch, false);
		
		this.skillContainer = skillContainer;
	}
	
	public SkillContainer getSkillContainer() {
		return this.skillContainer;
	}
}