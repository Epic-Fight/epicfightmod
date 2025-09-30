package yesman.epicfight.skill.passive;

import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class TechnicianSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("99e5c782-fdaf-11eb-9a03-0242ac130003");
	
	public TechnicianSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.DODGE_SUCCESS_EVENT, EVENT_UUID, (event) -> {
			ServerPlayer serverplayer = event.getPlayerPatch().getOriginal();
			event.getPlayerPatch().playSound(EpicFightSounds.TECHNICIAN.get(), 1.0F, 1.0F, 1.0F);
			serverplayer.serverLevel().sendParticles(ParticleTypes.POOF, event.getLocation().x(), event.getLocation().y(), event.getLocation().z(), 4, 0.0D, 0.0D, 0.0D, 0.075D);
			float consumption = container.getExecutor().getModifiedStaminaConsume(container.getExecutor().getSkill(SkillSlots.DODGE).getSkill().getConsumption());
			container.getExecutor().setStamina(container.getExecutor().getStamina() + consumption);
		});
		
		container.getExecutor().getEventListener().addEventListener(EventType.ANIMATION_BEGIN_EVENT, EVENT_UUID, (event) -> {
			if (!container.getExecutor().isLogicalClient() && event.getAnimation() instanceof DodgeAnimation) {
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(new SPEntityPairingPacket(container.getServerExecutor().getOriginal().getId(), EntityPairingPacketTypes.TECHNICIAN_ACTIVATED), container.getServerExecutor().getOriginal());
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.DODGE_SUCCESS_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.ANIMATION_BEGIN_EVENT, EVENT_UUID);
	}
}