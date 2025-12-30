package yesman.epicfight.skill.weaponinnate;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.registry.entries.EpicFightSynchedAnimationVariableKeys;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;

public class SteelWhirlwindSkill extends WeaponInnateSkill implements ChargeableSkill {
    private AnimationAccessor<? extends StaticAnimation> chargingAnimation;
    private AnimationAccessor<? extends AttackAnimation> attackAnimation;

    public SteelWhirlwindSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);

        this.chargingAnimation = Animations.STEEL_WHIRLWIND_CHARGING;
        this.attackAnimation = Animations.STEEL_WHIRLWIND;
    }

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        eventListener.registerEvent(
            EpicFightClientEventHooks.Control.MAPPED_MOVEMENT_INPUT_UPDATE,
            event -> {
                if (skillContainer.getExecutor().isHoldingSkill(this)) {
                    LocalPlayer clientPlayer = skillContainer.getClientExecutor().getOriginal();
                    clientPlayer.setSprinting(false);
                    clientPlayer.sprintTriggerTime = -1;
                    ControlEngine.setSprintingKeyStateNotDown();

                    final PlayerInputState current = event.getInputState();
                    final PlayerInputState updated = current
                        .withForwardImpulse(current.forwardImpulse() * (1.0F - 0.8F * skillContainer.getExecutor().getSkillChargingTicks() / 30.0F));
                    InputManager.setInputState(updated);
                }
            },
            this
        );
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        AttackAnimation anim = this.attackAnimation.get();

        for (Phase phase : anim.phases) {
            phase.addProperties(this.properties.get(0).entrySet());
        }

        return this;
    }

    @Override
    public int getAllowedMaxChargingTicks() {
        return 60;
    }

    @Override
    public int getMaxChargingTicks() {
        return 30;
    }

    @Override
    public int getMinChargingTicks() {
        return 6;
    }

    @Override
    public void startHolding(SkillContainer container) {
        AssetAccessor<? extends StaticAnimation> currentPlaying = container.getExecutor().getAnimator().getPlayerFor(null).getRealAnimation();

        if (currentPlaying.get().isMainFrameAnimation()) {
            container.getExecutor().stopPlaying(currentPlaying);
        }

        container.getExecutor().playAnimationSynchronized(this.chargingAnimation, 0.0F);
    }

    @Override
    public void resetHolding(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            container.getExecutor().getAnimator().stopPlaying(this.chargingAnimation);
        } else {
            container.getExecutor().stopPlaying(this.chargingAnimation);
        }
    }

    @Override
    public void onStopHolding(SkillContainer container, SPSkillFeedback feedback) {
        container.getExecutor().getAnimator().getVariables().put(EpicFightSynchedAnimationVariableKeys.CHARGING_TICKS.get(), this.attackAnimation, container.getExecutor().getAccumulatedChargeTicks());
        container.getExecutor().playAnimationSynchronized(this.attackAnimation, 0.0F);
        this.cancelOnServer(container, null);
    }

    @Override
    public void holdTick(SkillContainer container) {
        ChargeableSkill.super.holdTick(container);
    }

    @Override @ClientOnly
    public KeyMapping getKeyMapping() {
        return EpicFightKeyMappings.WEAPON_INNATE_SKILL;
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
        List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");

        return list;
    }
}