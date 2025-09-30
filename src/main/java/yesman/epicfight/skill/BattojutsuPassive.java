package yesman.epicfight.skill;

import java.util.UUID;

import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class BattojutsuPassive extends Skill {
	private static final UUID EVENT_UUID = UUID.fromString("a416c93a-42cb-11eb-b378-0242ac130002");
	
	public BattojutsuPassive(SkillBuilder<? extends Skill> builder) {
		super(builder);
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID, (event) -> {
			container.getSkill().setConsumptionSynchronize(container, 0.0F);
			container.getSkill().setStackSynchronize(container, 0);
		});
		
		container.getExecutor().getEventListener().addEventListener(EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID, (event) -> {
			this.onReset(container);
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.ACTION_EVENT_SERVER, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
	}
	
	@Override
	public void onReset(SkillContainer container) {
		if (!container.getExecutor().isLogicalClient()) {
			if (container.getDataManager().getDataValue(SkillDataKeys.SHEATH.get())) {
				container.getDataManager().setDataSync(SkillDataKeys.SHEATH.get(), false);
				container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
			}
			
			container.getSkill().setConsumptionSynchronize(container, 0);
		}
	}
	
	@Override
	public void setConsumption(SkillContainer container, float value) {
		if (!container.getExecutor().isLogicalClient()) {
			if (container.getMaxResource() < value) {
				container.getDataManager().setDataSync(SkillDataKeys.SHEATH.get(), true);
				container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
				container.getServerExecutor().playAnimationInClientSide(Animations.BIPED_UCHIGATANA_SCRAP, 0.0F);
			}
		}
		
		super.setConsumption(container, value);
	}
	
	@Override
	public boolean shouldDeactivateAutomatically(PlayerPatch<?> executor) {
		return true;
	}
	
	@Override
	public float getCooldownRegenPerSecond(PlayerPatch<?> playerpatch) {
		return (playerpatch.getEntityState().inaction() || playerpatch.isHoldingAny()) ? 0.0F : 1.0F;
	}
}