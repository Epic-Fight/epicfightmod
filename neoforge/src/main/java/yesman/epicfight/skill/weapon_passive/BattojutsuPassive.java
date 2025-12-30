package yesman.epicfight.skill.weapon_passive;

import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class BattojutsuPassive extends Skill {
    public BattojutsuPassive(SkillBuilder<?> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Animation.START_ACTION,
            event -> {
                if (!event.getEntityPatch().isLogicalClient()) {
                    this.setConsumptionSynchronize(skillContainer, 0.0F);
                    this.setStackSynchronize(skillContainer, 0);
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Player.USE_ITEM,
            event -> {
                this.onReset(skillContainer);
            },
            this
        );
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