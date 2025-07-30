package yesman.epicfight.skill.modules;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

/**
 * This interface is meant to be implemented into the skill to make the skill be able to charge.
 */
public interface ChargeableSkill {
	/**
	 * Runs some logic when a player starts to charge skill the said skill.
	 * @param caster the common-sided player
	 */
    void startCharging(PlayerPatch<?> caster);
	
	/**
	 * When a player takes another action while charging
	 * @param caster the common-sided player
	 */
    void resetCharging(PlayerPatch<?> caster);
	
	/**
	 * Max charging ticks players can persist
	 * @return {@link Integer} how many ticks can the charge last.
	 */
	int getAllowedMaxChargingTicks();
	
	/**
	 * A limitation value for charging that returns at {@link PlayerPatch#getChargingAmount()}
	 * @return {@link Integer} if charged beyond max, it will leave the max charge ticks.
	 */
	int getMaxChargingTicks();
	
	/**
	 * A required minimal charging tick to execute the skill
	 * @return {@link Integer}how little can a skill be charged.
	 */
	int getMinChargingTicks();
	
	/**
	 * Called each tick during charging skill
	 * @param caster the common-sided player
	 */
	default void chargingTick(PlayerPatch<?> caster) {
		caster.setChargingAmount(caster.getChargingAmount() + 1);
	}
	
	/**
	 * Get how many ticks the player charged
	 * Default: (current tick - charging begin tick)
	 * @param caster the player
	 */
	default int getChargingAmount(PlayerPatch<?> caster) {
		return caster.getChargingAmount();
	}
	
	/**
	 * Called when player finished charging and executes skill
	 * @param caster the server-sided player
	 * @param chargingTicks this is how many ticks has been charged up.
	 * @param onMaxTick some unique logic when skill is fully charged
	 */
	void castSkill(ServerPlayerPatch caster, SkillContainer skillContainer, int chargingTicks, SPSkillExecutionFeedback feedbackPacket, boolean onMaxTick);

	/**
	 *
	 * @param caster the client-sided player
	 * @param controlEngine the local control engine
	 * @param buffer the execution packet that is sent to the server for logic
	 */
	@OnlyIn(Dist.CLIENT)
	void gatherChargingArguments(LocalPlayerPatch caster, ControlEngine controlEngine, FriendlyByteBuf buffer);

	/**
	 *
	 * @return {@link KeyMapping} a key mapping to a keybind.
	 */
	@OnlyIn(Dist.CLIENT)
	KeyMapping getKeyMapping();

	/**
	 *
	 * @return {@link Skill}: casts this skill as a normal {@link Skill}
	 */
	default Skill asSkill() {
		return (Skill)this;
	}
}