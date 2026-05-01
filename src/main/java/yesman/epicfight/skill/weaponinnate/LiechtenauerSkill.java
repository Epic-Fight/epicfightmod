package yesman.epicfight.skill.weaponinnate;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

import java.util.List;

public class LiechtenauerSkill extends WeaponInnateSkill {
    private int returnDuration;

    public LiechtenauerSkill(WeaponInnateSkill.Builder<?> builder) {
        super(builder);
    }

    @Override
    public void loadDatapackParameters(CompoundTag parameters) {
        super.loadDatapackParameters(parameters);
        this.returnDuration = parameters.getInt("return_duration");
    }

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.KILL_ENTITY,
            event -> {
                if (this.isActivated(skillContainer) && !this.isDisabled(skillContainer)) {
                    this.setDurationSynchronize(skillContainer, Math.min(this.maxDuration, skillContainer.getRemainDuration() + this.returnDuration));
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME,
            event -> {
                int phaseLevel = skillContainer.getExecutor().getEntityState().getLevel();

                if (event.getDamage() > 0.0F && this.isActivated(skillContainer) && !this.isDisabled(skillContainer) && phaseLevel > 0 && phaseLevel < 3 &&
                    this.canExecute(skillContainer) && isBlockableSource(event.getDamageSource())) {
                    DamageSource damageSource = event.getDamageSource();
                    boolean isFront = false;
                    Vec3 sourceLocation = damageSource.getSourcePosition();

                    if (sourceLocation != null) {
                        Vec3 viewVector = skillContainer.getExecutor().getOriginal().getViewVector(1.0F);
                        Vec3 toSourceLocation = sourceLocation.subtract(skillContainer.getExecutor().getOriginal().position()).normalize();

                        if (toSourceLocation.dot(viewVector) > 0.0D) {
                            isFront = true;
                        }
                    }

                    if (isFront) {
                        skillContainer.getExecutor().playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
                        ServerPlayer playerentity = skillContainer.getServerExecutor().getOriginal();
                        EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(playerentity.serverLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, playerentity, damageSource.getDirectEntity());

                        float knockback = 0.25F;

                        if (damageSource instanceof EpicFightDamageSource epicfightSource) {
                            knockback += Math.min(epicfightSource.calculateImpact() * 0.1F, 1.0F);
                        }

                        if (damageSource.getDirectEntity() instanceof LivingEntity livingentity) {
                            float modifiedKnockback = EnchantmentHelper.modifyKnockback((ServerLevel)livingentity.level(), livingentity.getItemInHand(livingentity.getUsedItemHand()), livingentity, damageSource, knockback);
                            knockback = (modifiedKnockback - knockback) * 0.1F;
                        }

                        EpicFightCapabilities.getUnparameterizedEntityPatch(event.getDamageSource().getEntity(), LivingEntityPatch.class).ifPresent(attackerpatch -> {
                            attackerpatch.setLastAttackEntity(skillContainer.getExecutor().getOriginal());
                        });

                        skillContainer.getExecutor().knockBackEntity(damageSource.getDirectEntity().position(), knockback);
                        event.cancel();
                        event.setResult(AttackResult.ResultType.BLOCKED);
                    }
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightClientEventHooks.Control.MAPPED_MOVEMENT_INPUT_UPDATE,
            event -> {
                if (this.isActivated(skillContainer) && skillContainer.getExecutor().getAdvancedHoldingItemCapability(InteractionHand.MAIN_HAND).getInnateSkill(skillContainer.getExecutor(), skillContainer.getExecutor().getOriginal().getMainHandItem()) == this) {
                    LocalPlayer clientPlayer = skillContainer.getClientExecutor().getOriginal();
                    clientPlayer.setSprinting(false);
                    clientPlayer.sprintTriggerTime = -1;
                    ControlEngine.setSprintingKeyStateNotDown();
                }
            },
            this
        );
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        container.getExecutor().playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F);

        if (this.isActivated(container)) {
            this.cancelOnServer(container, arguments);
        } else {
            super.executeOnServer(container, arguments);
            container.activate();
            container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
            container.getExecutor().playAnimationSynchronized(Animations.BIPED_LIECHTENAUER_READY, 0.0F);
        }
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag arguments) {
        container.deactivate();
        super.cancelOnServer(container, arguments);
        container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
    }

    @Override
    public void executeOnClient(SkillContainer container, CompoundTag arguments) {
        super.executeOnClient(container, arguments);
        container.activate();
    }

    @Override
    public void cancelOnClient(SkillContainer container, CompoundTag arguments) {
        super.cancelOnClient(container, arguments);
        container.deactivate();
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        if (container.getExecutor().isLogicalClient()) {
            return super.canExecute(container);
        } else {
            ItemStack itemstack = container.getExecutor().getOriginal().getMainHandItem();
            return EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(container.getExecutor(), itemstack) == this && container.getExecutor().getOriginal().getVehicle() == null;
        }
    }

    @Override
    public WeaponInnateSkill registerPropertiesToAnimation() {
        return this;
    }

    private static boolean isBlockableSource(DamageSource damageSource) {
        return !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !damageSource.is(DamageTypeTags.IS_EXPLOSION);
    }

    @Override @ClientOnly
    public List<Component> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerCap) {
        List<Component> list = Lists.newArrayList();
        List<Object> tooltipArgs = Lists.newArrayList();
        String traslatableText = this.getTranslationKey();

        tooltipArgs.add(this.maxDuration / 20);
        tooltipArgs.add(this.returnDuration / 20);

        list.add(Component.translatable(traslatableText).withStyle(ChatFormatting.WHITE).append(Component.literal(String.format("[%.0f]", this.consumption)).withStyle(ChatFormatting.AQUA)));
        list.add(Component.translatable(traslatableText + ".tooltip", tooltipArgs.toArray(new Object[0])).withStyle(ChatFormatting.DARK_GRAY));

        return list;
    }
}