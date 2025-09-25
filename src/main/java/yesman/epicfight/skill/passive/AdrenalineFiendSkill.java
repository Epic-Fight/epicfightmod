package yesman.epicfight.skill.passive;

import yesman.epicfight.api.neoevent.playerpatch.PlayerKilledEvent;
import yesman.epicfight.api.neoevent.playerpatch.StartActionEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;

public class AdrenalineFiendSkill extends PassiveSkill {
	public AdrenalineFiendSkill(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void playerKilledEvent(PlayerKilledEvent event, SkillContainer container) {
		if (!container.getExecutor().isLogicalClient() && event.getPlayerPatch().getStaminaRegenAwaitTicks() > 0) {
			if (event.getPlayerPatch().getStamina() < event.getPlayerPatch().getMaxStamina()) {
				event.getPlayerPatch().setStaminaRegenAwaitTicks(0);
				container.getDataManager().setData(EpicFightSkillDataKeys.TICK_RECORD, container.getExecutor().getOriginal().tickCount);
				container.getExecutor().sendToAllPlayersTrackingMe(new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.ADRENALINE_ACTIVATED));
			}
		}
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void modifyBaseDamageEvent(StartActionEvent event, SkillContainer container) {
		if (container.getDataManager().getDataValue(EpicFightSkillDataKeys.TICK_RECORD) + 30 > container.getExecutor().getOriginal().tickCount) {
			event.resetActionTick(false);
		}
	}
}
