package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class LiechtenauerSkill extends WeaponInnateSkill {
	private static final UUID EVENT_UUID = UUID.fromString("244c57c0-a837-11eb-bcbc-0242ac130002");
	private int returnDuration;
	
	public LiechtenauerSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
		super(builder);
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		this.returnDuration = parameters.getInt("return_duration");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		container.getExecutor().getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
			if (this.isActivated(container) && !this.isDisabled(container)) {
				if (event.getAttackDamage() > event.getTarget().getHealth()) {
					this.setDurationSynchronize(container, Math.min(this.maxDuration, container.getRemainDuration() + this.returnDuration));
				}
			}
		});
		
		container.getExecutor().getEventListener().addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event) -> {
			int phaseLevel = event.getPlayerPatch().getEntityState().getLevel();
			
			if (event.getDamage() > 0.0F && this.isActivated(container) && !this.isDisabled(container) && phaseLevel > 0 && phaseLevel < 3 && 
				this.canExecute(container) && isBlockableSource(event.getDamageSource())) {
				DamageSource damageSource = event.getDamageSource();
				boolean isFront = false;
				Vec3 sourceLocation = damageSource.getSourcePosition();
				
				if (sourceLocation != null) {
					Vec3 viewVector = event.getPlayerPatch().getOriginal().getViewVector(1.0F);
					Vec3 toSourceLocation = sourceLocation.subtract(event.getPlayerPatch().getOriginal().position()).normalize();
					
					if (toSourceLocation.dot(viewVector) > 0.0D) {
						isFront = true;
					}
				}
				
				if (isFront) {
					event.getPlayerPatch().playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
					ServerPlayer playerentity = event.getPlayerPatch().getOriginal();
					EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(playerentity.serverLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, playerentity, damageSource.getDirectEntity());

					float knockback = 0.25F;
					
					if (damageSource instanceof EpicFightDamageSource epicfightSource) {
						knockback += Math.min(epicfightSource.calculateImpact() * 0.1F, 1.0F);
					}
					
					if (damageSource.getDirectEntity() instanceof LivingEntity livingentity) {
						knockback += EnchantmentHelper.getKnockbackBonus(livingentity) * 0.1F;
					}
					
					EpicFightCapabilities.getUnparameterizedEntityPatch(event.getDamageSource().getEntity(), LivingEntityPatch.class).ifPresent(attackerpatch -> {
						attackerpatch.setLastAttackEntity(event.getPlayerPatch().getOriginal());
					});
					
					event.getPlayerPatch().knockBackEntity(damageSource.getDirectEntity().position(), knockback);
					event.setCanceled(true);
					event.setResult(AttackResult.ResultType.BLOCKED);
				}
			}
		}, 0);
		
		container.getExecutor().getEventListener().addEventListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
			if (this.isActivated(container)) {
				LocalPlayer clientPlayer = event.getPlayerPatch().getOriginal();
				clientPlayer.setSprinting(false);
				clientPlayer.sprintTriggerTime = -1;
                ControlEngine.setSprintingKeyStateNotDown();
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, 0);
		container.getExecutor().getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		container.getExecutor().playSound(SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F);
		
		if (this.isActivated(container)) {
			this.cancelOnServer(container, args);
		} else {
			super.executeOnServer(container, args);
			container.activate();
			container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
			container.getExecutor().playAnimationSynchronized(Animations.BIPED_LIECHTENAUER_READY, 0.0F);
		}
	}
	
	@Override
	public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
		container.deactivate();
		super.cancelOnServer(container, args);
		container.getServerExecutor().modifyLivingMotionByCurrentItem(false);
	}
	
	@Override
	public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
		super.executeOnClient(container, args);
		container.activate();
	}
	
	@Override
	public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
		super.cancelOnClient(container, args);
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
	
	@OnlyIn(Dist.CLIENT)
	@Override
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
	
	public enum Stance {
		VOM_TAG, PFLUG, OCHS
	}
}