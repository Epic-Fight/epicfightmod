package yesman.epicfight.skill.passive;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.neoevent.playerpatch.AnimationBeginEvent;
import yesman.epicfight.api.neoevent.playerpatch.DodgeSuccessEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.skill.SkillSlots;

public class TechnicianSkill extends PassiveSkill {
	public TechnicianSkill(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void dodgeSuccessEvent(DodgeSuccessEvent event, SkillContainer skillContainer) {
		ServerPlayer serverplayer = event.getPlayerPatch().getOriginal();
		event.getPlayerPatch().playSound(EpicFightSounds.TECHNICIAN.get(), 1.0F, 1.0F, 1.0F);
		serverplayer.serverLevel().sendParticles(ParticleTypes.POOF, event.getLocation().x(), event.getLocation().y(), event.getLocation().z(), 4, 0.0D, 0.0D, 0.0D, 0.075D);
		float consumption = skillContainer.getExecutor().getModifiedStaminaConsume(skillContainer.getExecutor().getSkill(SkillSlots.DODGE).getSkill().getConsumption());
		skillContainer.getExecutor().setStamina(skillContainer.getExecutor().getStamina() + consumption);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void animationBeginEvent(AnimationBeginEvent event, SkillContainer skillContainer) {
		if (!skillContainer.getExecutor().isLogicalClient() && event.getAnimation() instanceof DodgeAnimation) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(new SPEntityPairingPacket(skillContainer.getServerExecutor().getOriginal().getId(), EntityPairingPacketTypes.TECHNICIAN_ACTIVATED), skillContainer.getServerExecutor().getOriginal());
		}
	}
}