package yesman.epicfight.skill.passive;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.neoevent.playerpatch.PlayerKilledEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;

public class StaminaPillagerSkill extends PassiveSkill {
	protected float regenRate;
	
	public StaminaPillagerSkill(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		this.regenRate = parameters.getFloat("regen_rate");
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void playerKilled(PlayerKilledEvent event, SkillContainer skillContainer) {
		float currentStamina = event.getPlayerPatch().getStamina();
		float staminaLoss = event.getPlayerPatch().getMaxStamina() - currentStamina;
		event.getPlayerPatch().setStamina(currentStamina + Math.min(staminaLoss * this.regenRate * 0.01F, 2.0F));
		event.getKilledEntity().playSound(EpicFightSounds.STAMINA_PILLAGER_DEATH.get());
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPEntityPairingPacket(event.getKilledEntity().getId(), EntityPairingPacketTypes.STAMINA_PILLAGER_BODY_ASHES), event.getKilledEntity());
		
		SPEntityPairingPacket pairingPacket = new SPEntityPairingPacket(event.getPlayerPatch().getOriginal().getId(), EntityPairingPacketTypes.FLASH_WHITE);
		
		// durationTick, maxOverlay, maxBrightness, disableRedOverlay
		pairingPacket.buffer().writeInt(8);
		pairingPacket.buffer().writeInt(3);
		pairingPacket.buffer().writeInt(6);
		pairingPacket.buffer().writeBoolean(false);
		
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntityWithSelf(pairingPacket, event.getPlayerPatch().getOriginal());
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(String.format("%.0f", this.regenRate));
		
		return list;
	}
}