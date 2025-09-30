package yesman.epicfight.skill.passive;

import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class StaminaPillagerSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("20807880-fd30-11eb-9a03-0242ac130003");
	
	protected float regenPercentage;
	
	public StaminaPillagerSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		this.regenPercentage = parameters.getFloat("regen_rate");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID, (event) -> {
			float currentStamina = event.getPlayerPatch().getStamina();
			float staminaLoss = event.getPlayerPatch().getMaxStamina() - currentStamina;
			event.getPlayerPatch().setStamina(currentStamina + Math.min(staminaLoss * this.regenPercentage * 0.01F, 2.0F));
			event.getKilledEntity().playSound(EpicFightSounds.STAMINA_PILLAGER_DEATH.get());
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPEntityPairingPacket(event.getKilledEntity().getId(), EntityPairingPacketTypes.STAMINA_PILLAGER_BODY_ASHES), event.getKilledEntity());
			
			SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(event.getPlayerPatch().getOriginal().getId(), EntityPairingPacketTypes.FLASH_WHITE);
			
			// durationTick, maxOverlay, maxBrightness, disableRedOverlay
			pairingPacket.getBuffer().writeInt(8);
			pairingPacket.getBuffer().writeInt(3);
			pairingPacket.getBuffer().writeInt(6);
			pairingPacket.getBuffer().writeBoolean(false);
			
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, event.getPlayerPatch().getOriginal());
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(String.format("%.0f", this.regenPercentage));
		
		return list;
	}
}