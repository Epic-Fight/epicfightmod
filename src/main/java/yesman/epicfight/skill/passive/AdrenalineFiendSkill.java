package yesman.epicfight.skill.passive;

import java.util.UUID;

import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class AdrenalineFiendSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("678cff5d-dc70-492d-b960-f01241d1a0f9");
	
	public AdrenalineFiendSkill(SkillBuilder<? extends PassiveSkill> builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID, (event) -> {
			if (!container.getExecutor().isLogicalClient() && event.getPlayerPatch().getStaminaRegenAwaitTicks() > 0) {
				if (event.getPlayerPatch().getStamina() < event.getPlayerPatch().getMaxStamina()) {
					event.getPlayerPatch().setStaminaRegenAwaitTicks(0);
					container.getDataManager().setData(SkillDataKeys.TICK_RECORD.get(), container.getExecutor().getOriginal().tickCount);
					container.getExecutor().sendToAllPlayersTrackingMe(new SPEntityPairingPacket(container.getExecutor().getOriginal().getId(), EntityPairingPacketTypes.ADRENALINE_ACTIVATED));
				}
			}
		});
		
		listener.addEventListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID, (event) -> {
			if (container.getDataManager().getDataValue(SkillDataKeys.TICK_RECORD.get()) + 30 > container.getExecutor().getOriginal().tickCount) {
				event.resetActionTick(false);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.removeListener(EventType.PLAYER_KILLED_EVENT, EVENT_UUID);
		listener.removeListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID);
	}
}
