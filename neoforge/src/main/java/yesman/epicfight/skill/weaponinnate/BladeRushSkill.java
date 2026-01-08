package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Maps;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.generated.LangKeys;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSynchedAnimationVariableKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BladeRushSkill extends WeaponInnateSkill {
    public static final class Builder extends WeaponInnateSkill.Builder<BladeRushSkill.Builder> {
        public Builder(Function<Builder, ? extends Skill> constructor) {
            super(constructor);
        }

        private final Map<EntityType<?>, AnimationAccessor<? extends StaticAnimation>> tryAnimations = Maps.newHashMap();

        public BladeRushSkill.Builder putTryAnimation(EntityType<?> entityType, AnimationAccessor<? extends StaticAnimation> tryAnimation) {
            this.tryAnimations.put(entityType, tryAnimation);
            return this;
        }
    }

    public static Builder createBladeRushBuilder() {
        BladeRushSkill.Builder builder = new BladeRushSkill.Builder(BladeRushSkill::new).setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
        builder.putTryAnimation(EntityType.ZOMBIE, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.HUSK, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.DROWNED, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.SKELETON, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.STRAY, Animations.BLADE_RUSH_TRY)
                .putTryAnimation(EntityType.CREEPER, Animations.BLADE_RUSH_TRY);

        return builder;
    }

    private final List<AnimationAccessor<? extends StaticAnimation>> comboAnimations = new ArrayList<> (3);
    private final Map<EntityType<?>, AnimationAccessor<? extends StaticAnimation>> tryAnimations;

    public BladeRushSkill(BladeRushSkill.Builder builder) {
        super(builder);

        this.comboAnimations.add(Animations.BLADE_RUSH_COMBO1);
        this.comboAnimations.add(Animations.BLADE_RUSH_COMBO2);
        this.comboAnimations.add(Animations.BLADE_RUSH_COMBO3);
        this.tryAnimations = builder.tryAnimations;
    }

    @Override @ClientOnly
    public void gatherArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(true);
        arguments.putBoolean("put", true);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener eventListener) {
        super.onInitiate(container, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.DELIVER_DAMAGE_POST,
            event -> {
                if (event.getDamageSource().getAnimation().idBetween(Animations.BLADE_RUSH_COMBO1, Animations.BLADE_RUSH_COMBO3) && this.tryAnimations.containsKey(event.getTarget().getType())) {
                    MobEffectInstance effectInstance = event.getTarget().getEffect(EpicFightMobEffects.INSTABILITY);
                    int amp = effectInstance == null ? 0 : effectInstance.getAmplifier() + 1;
                    event.getTarget().addEffect(new MobEffectInstance(EpicFightMobEffects.INSTABILITY, 100, amp));
                }
            },
            this
        );
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        LivingEntity target = container.getExecutor().getTarget();
        boolean instaKill = false;

        if (target != null) {
            if (target.hasEffect(EpicFightMobEffects.INSTABILITY) && target.getEffect(EpicFightMobEffects.INSTABILITY).getAmplifier() >= 2) {
                instaKill = true;
            } else {
                LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);

                if (entitypatch != null && entitypatch.getEntityState().hurtLevel() > 1 && this.tryAnimations.containsKey(target.getType())) {
                    instaKill = true;
                }
            }
        } else {
            return;
        }

        if (instaKill) {
            container.getDataManager().setData(EpicFightSkillDataKeys.COMBO_COUNTER, 0);
            container.getExecutor().getAnimator().getVariables().put(EpicFightSynchedAnimationVariableKeys.TARGET_ENTITY.get(), Animations.BLADE_RUSH_TRY, target.getId());
            container.getExecutor().playAnimationSynchronized(Animations.BLADE_RUSH_TRY, 0);
        } else {
            int counter = container.getDataManager().getDataValue(EpicFightSkillDataKeys.COMBO_COUNTER);
            AnimationAccessor<? extends StaticAnimation> animation = this.comboAnimations.get(counter);
            container.getDataManager().setDataF(EpicFightSkillDataKeys.COMBO_COUNTER, (v) -> (v + 1) % this.comboAnimations.size());
            container.getExecutor().getAnimator().getVariables().put(EpicFightSynchedAnimationVariableKeys.TARGET_ENTITY.get(), animation, target.getId());
            container.getExecutor().playAnimationSynchronized(animation, 0);
        }

        super.executeOnServer(container, arguments);
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
        List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.getFirst(), "Each Strike:");
        this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(1), "Execution:");

        return list;
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        Animations.BLADE_RUSH_COMBO1.get().phases[0].addProperties(this.properties.getFirst().entrySet());
        Animations.BLADE_RUSH_COMBO2.get().phases[0].addProperties(this.properties.getFirst().entrySet());
        Animations.BLADE_RUSH_COMBO3.get().phases[0].addProperties(this.properties.getFirst().entrySet());
        Animations.BLADE_RUSH_EXECUTE_BIPED.get().phases[0].addProperties(this.properties.get(1).entrySet());
        return this;
    }

    @Override
    public boolean checkExecuteCondition(SkillContainer container) {
        return container.getExecutor().getTarget() != null && container.getExecutor().getTarget().isAlive() && container.getExecutor().getOriginal().distanceToSqr(container.getExecutor().getTarget()) < 100.0D;
    }

    @Override @ClientOnly
    public void onScreen(LocalPlayerPatch playerpatch, float resolutionX, float resolutionY) {
    }

    @Override @ClientOnly
    public void validationFeedback(SkillContainer container) {
        Skill skill = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND).getInnateSkill(container.getExecutor(), container.getExecutor().getOriginal().getItemInHand(InteractionHand.MAIN_HAND));

        if (this.equals(skill) && !this.checkExecuteCondition(container)) {
            if (container.getExecutor().getTarget() == null || !container.getExecutor().getTarget().isAlive()) {
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(LangKeys.GUI_MESSAGE_INGAME_NO_TARGET_WARNING), false);
            } else {
                Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(LangKeys.GUI_MESSAGE_INGAME_TOO_FAR_TARGET_WARNING), false);
            }
        }
    }
}
