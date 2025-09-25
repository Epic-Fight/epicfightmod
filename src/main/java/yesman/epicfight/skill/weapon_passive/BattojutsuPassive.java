package yesman.epicfight.skill.weapon_passive;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yesman.epicfight.api.neoevent.playerpatch.StartActionEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class BattojutsuPassive extends Skill {
	public BattojutsuPassive(SkillBuilder<?> builder) {
		super(builder);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void actionEvent(StartActionEvent event, SkillContainer skillContainer) {
		this.setConsumptionSynchronize(skillContainer, 0.0F);
		this.setStackSynchronize(skillContainer, 0);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void itemUseEvent(PlayerInteractEvent.RightClickItem event, SkillContainer skillContainer) {
		this.onReset(skillContainer);
	}
	
	@Override
	public void onReset(SkillContainer container) {
		container.runOnServer(serverExecutor -> {
			if (container.getDataManager().getDataValue(EpicFightSkillDataKeys.SHEATH)) {
				container.getDataManager().setDataSync(EpicFightSkillDataKeys.SHEATH, false);
				serverExecutor.modifyLivingMotionByCurrentItem(false);
			}
			
			container.getSkill().setConsumptionSynchronize(container, 0);
		});
	}
	
	@Override
	public void setConsumption(SkillContainer container, float value) {
		container.runOnServer(serverExecutor -> {
			if (container.getMaxResource() < value) {
				container.getDataManager().setDataSync(EpicFightSkillDataKeys.SHEATH, true);
				serverExecutor.modifyLivingMotionByCurrentItem(false);
				serverExecutor.playAnimationInClientSide(Animations.BIPED_UCHIGATANA_SCRAP, 0.0F);
			}
		});
		
		super.setConsumption(container, value);
	}
	
	@Override
	public boolean shouldDeactivateAutomatically(PlayerPatch<?> executer) {
		return true;
	}
	
	@Override
	public float getCooldownRegenPerSecond(PlayerPatch<?> playerpatch) {
		return (playerpatch.getEntityState().inaction() || playerpatch.isHoldingAny()) ? 0.0F : 1.0F;
	}
}