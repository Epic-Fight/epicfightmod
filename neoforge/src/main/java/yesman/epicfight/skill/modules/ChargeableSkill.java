package yesman.epicfight.skill.modules;

import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/// This interface extends the holdable skill to add some little neat stuff
////
public interface ChargeableSkill extends HoldableSkill {
	/// Max charging ticks players can persist
	/// @return Class: [Integer] - how many ticks can the charge last.
	int getAllowedMaxChargingTicks();
	
	/// A limitation value for charging that returns at [PlayerPatch#getChargingTicks]
	/// @return Class: [Integer] - if charged beyond max, it will leave the max charge ticks.
	int getMaxChargingTicks();
	
	/// A required minimal charging tick to execute the skill
	/// @return Class: [Integer] - how little can a skill be charged.
	int getMinChargingTicks();
	
	@Override
	default void resetHolding(SkillContainer container) {
		container.getExecutor().setChargingTicks(0);
	}
	
	@Override
	default void holdTick(SkillContainer container) {
		HoldableSkill.super.holdTick(container);
		container.getExecutor().setChargingTicks(container.getExecutor().getChargingTicks() + 1);
	}
}