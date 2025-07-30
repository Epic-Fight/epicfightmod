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
	 * @param caster Class: {@link PlayerPatch} - the common-sided player
	 */
    void startCharging(PlayerPatch<?> caster);
	
	/**
	 * When a player takes another action while charging
	 * @param caster Class: {@link PlayerPatch} - the common-sided player
	 */
    void resetCharging(PlayerPatch<?> caster);
	
	/**
	 * Max charging ticks players can persist
	 * @return Class: {@link Integer} - how many ticks can the charge last.
	 */
	int getAllowedMaxChargingTicks();
	
	/**
	 * A limitation value for charging that returns at {@link PlayerPatch#getChargingAmount()}
	 * @return Class: {@link Integer} - if charged beyond max, it will leave the max charge ticks.
	 */
	int getMaxChargingTicks();
	
	/**
	 * A required minimal charging tick to execute the skill
	 * @return Class: {@link Integer} - how little can a skill be charged.
	 */
	int getMinChargingTicks();
	
	/**
	 * Called each tick during charging skill. Note: A tick is called 20 times per second, per side.
	 * @param caster Class: {@link PlayerPatch} - the common-sided player
	 */
	default void chargingTick(PlayerPatch<?> caster) {
		caster.setChargingAmount(caster.getChargingAmount() + 1);
	}
	
	/**
	 * Get how many ticks the player charged
	 * Default: (current tick - charging begin tick)
	 * @param caster Class: {@link PlayerPatch} - the common-sided player
	 */
	default int getChargingAmount(PlayerPatch<?> caster) {
		return caster.getChargingAmount();
	}
	
	/**
	 * Called on the server-side when the player has finished charging and executes skill
	 * @param caster Class: {@link ServerPlayerPatch} - the server-sided player
	 * @param skillContainer Class: {@link SkillContainer} the container that this skill is in.
	 * @param chargingTicks Class: {@link Integer} - this is how many ticks has been charged up.
	 * @param onMaxTick Class: {@link Boolean} - whether the skill has been fully charged or not.
	 * @param feedbackPacket Class: {@link SPSkillExecutionFeedback} the packet that is fed back to the client.
	 */
	void castSkill(ServerPlayerPatch caster, SkillContainer skillContainer, int chargingTicks, SPSkillExecutionFeedback feedbackPacket, boolean onMaxTick);

	/**
	 * Gathers any charging arguments whether the player starts charging the skill and writes to a packet.
	 * @param caster Class: {@link LocalPlayerPatch} - the client-sided player
	 * @param controlEngine Class: {@link ControlEngine} - the local control engine that is usually called in the client side
	 * @param buffer Class: {@link FriendlyByteBuf} - the execution packet that is sent to the server for logic
	 */
	@OnlyIn(Dist.CLIENT)
	void gatherChargingArguments(LocalPlayerPatch caster, ControlEngine controlEngine, FriendlyByteBuf buffer);

	/**
	 * Retrieves the keybind of the skill.
	 * @return {@link KeyMapping} the keybind that is given by the class.
	 */
	@OnlyIn(Dist.CLIENT)
	KeyMapping getKeyMapping();

	/**
	 * Gives the normal skill object of this {@link ChargeableSkill} object.
	 * @return {@link Skill}: casts this skill as a normal {@link Skill}
	 */
	default Skill asSkill() {
		return (Skill)this;
	}
}