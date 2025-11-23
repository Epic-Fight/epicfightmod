package yesman.epicfight.skill.guard;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSetSkillContainerValue;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillCancelEvent;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

public class GuardSkill extends Skill implements HoldableSkill {
	protected static final UUID EVENT_UUID = UUID.fromString("b422f7a0-f378-11eb-9a03-0242ac130003");
	
	public static class Builder extends SkillBuilder<GuardSkill> {
		protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions = Maps.newHashMap();
		protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> advancedGuardMotions = Maps.newHashMap();
		protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardBreakMotions = Maps.newHashMap();
		
		public Builder addGuardMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>> function) {
			this.guardMotions.put(weaponCategory, function);
			return this;
		}
		
		public Builder addAdvancedGuardMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?> function) {
			this.advancedGuardMotions.put(weaponCategory, function);
			return this;
		}
		
		public Builder addGuardBreakMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>> function) {
			this.guardBreakMotions.put(weaponCategory, function);
			return this;
		}
	}
	
	public static GuardSkill.Builder createGuardBuilder() {
		return new GuardSkill.Builder()
				.addGuardMotion(WeaponCategories.AXE, (item, player) -> Animations.SWORD_GUARD_HIT)
				.addGuardMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_HIT)
				.addGuardMotion(WeaponCategories.UCHIGATANA, (item, player) -> Animations.UCHIGATANA_GUARD_HIT)
				.addGuardMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
				.addGuardMotion(WeaponCategories.SPEAR, (item, player) -> item.getStyle(player) == Styles.TWO_HAND ? Animations.SPEAR_GUARD_HIT : null)
				.addGuardMotion(WeaponCategories.SWORD, (item, player) -> item.getStyle(player) == Styles.ONE_HAND ? Animations.SWORD_GUARD_HIT : Animations.SWORD_DUAL_GUARD_HIT)
				.addGuardMotion(WeaponCategories.TACHI, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
				.addGuardBreakMotion(WeaponCategories.AXE, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
				.addGuardBreakMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_BREAK)
				.addGuardBreakMotion(WeaponCategories.UCHIGATANA, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
				.addGuardBreakMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
				.addGuardBreakMotion(WeaponCategories.SPEAR, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
				.addGuardBreakMotion(WeaponCategories.SWORD, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
				.addGuardBreakMotion(WeaponCategories.TACHI, (item, player) -> Animations.BIPED_COMMON_NEUTRALIZED)
				.setCategory(SkillCategories.GUARD)
				.setActivateType(ActivateType.HELD)
				.setResource(Resource.STAMINA)
				;
	}
	
	protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions;
	protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> advancedGuardMotions;
	protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardBreakMotions;
	
	protected float penalizer;
	
	public GuardSkill(GuardSkill.Builder builder) {
		super(builder);
		this.guardMotions = builder.guardMotions;
		this.advancedGuardMotions = builder.advancedGuardMotions;
		this.guardBreakMotions = builder.guardBreakMotions;
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		this.penalizer = parameters.getFloat("penalizer");
	}

	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
			container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), 0.0F);
		});
		
		container.getExecutor().getEventListener().addEventListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
			if (container.isActivated() && event.getPlayerPatch().getHoldingSkill() == this) {
				event.getPlayerPatch().getOriginal().setSprinting(false);
				event.getPlayerPatch().getOriginal().sprintTriggerTime = -1;

                ControlEngine.setSprintingKeyStateNotDown();
                final PlayerInputState current = event.getInputState();
                final PlayerInputState updated = current
                        .withForwardImpulse(current.forwardImpulse() * 0.5f)
                        .withLeftImpulse(current.leftImpulse() * 0.5f);
                InputManager.setInputState(updated);
			}
		});
		
		container.getExecutor().getEventListener().addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event) -> {
			CapabilityItem itemCapability = event.getPlayerPatch().getHoldingItemCapability(InteractionHand.MAIN_HAND);
			
			if (container.isActivated() && event.getPlayerPatch().getHoldingSkill() == this) {
				DamageSource damageSource = event.getDamageSource();
				boolean isFront = false;
				Vec3 sourceLocation = damageSource.getSourcePosition();
				
				if (sourceLocation != null) {
					Vec3 viewVector = event.getPlayerPatch().getOriginal().getViewVector(1.0F);
					viewVector = viewVector.subtract(0, viewVector.y, 0).normalize();
					
					Vec3 toSourceLocation = sourceLocation.subtract(event.getPlayerPatch().getOriginal().position()).normalize();
					
					if (toSourceLocation.dot(viewVector) > 0.0D) {
						isFront = true;
					}
				}
				
				if (isFront) {
					float impact = 0.5F;
					float knockback = 0.25F;
					
					if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamageSource) {
						if (epicfightDamageSource.is(EpicFightDamageTypeTags.GUARD_PUNCTURE)) {
							return;
						}

						impact = epicfightDamageSource.calculateImpact();
						knockback += Math.min(impact * 0.1F, 1.0F);
					}
					
					this.guard(container, itemCapability, event, knockback, impact, false);
				}
			}
		}, 1);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onInitiateClient(SkillContainer container) {
		super.onInitiateClient(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.UPDATE_COMPOSITE_LIVING_MOTION_EVENT, EVENT_UUID, (event) -> {
			if (container.isActivated() && this.isHoldingWeaponAvailable(container.getExecutor(), container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND), GuardSkill.BlockType.GUARD)) {
				event.setMotion(LivingMotions.BLOCK);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, 1);
		container.getExecutor().getEventListener().removeListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.CLIENT_ITEM_USE_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.SERVER_ITEM_USE_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.SERVER_ITEM_STOP_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onRemoveClient(SkillContainer container) {
		super.onRemoveClient(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.UPDATE_COMPOSITE_LIVING_MOTION_EVENT, EVENT_UUID);
	}
	
	public void guard(SkillContainer container, CapabilityItem itemCapability, TakeDamageEvent.Attack event, float knockback, float impact, boolean advanced) {
		DamageSource damageSource = event.getDamageSource();
		Entity offender = getOffender(damageSource);
		
		if (offender != null && this.isBlockableSource(damageSource, advanced)) {
			event.getPlayerPatch().playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
			ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
			EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serverPlayer.serverLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serverPlayer, offender);
			
			if (offender instanceof LivingEntity livingEntity) {
				knockback += EnchantmentHelper.getKnockbackBonus(livingEntity) * 0.1F;
			}
			
			float penalty = container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get()) + this.getPenalizer(itemCapability);
			float consumeAmount = penalty * impact;
			boolean canAfford = event.getPlayerPatch().consumeForSkill(this, Skill.Resource.STAMINA, consumeAmount);
			
			event.getPlayerPatch().knockBackEntity(offender.position(), knockback);
			container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), penalty);
			container.getDataManager().setDataSync(SkillDataKeys.PENALTY_RESTORE_COUNTER.get(), container.getServerExecutor().getOriginal().tickCount);
			
			BlockType blockType = canAfford ? BlockType.GUARD : BlockType.GUARD_BREAK;
			AnimationAccessor<? extends StaticAnimation> animation = this.getGuardMotion(container, event.getPlayerPatch(), itemCapability, blockType);
			
			if (animation != null) {
				event.getPlayerPatch().playAnimationSynchronized(animation, 0.0F);
			}
			
			if (blockType == BlockType.GUARD_BREAK) {
				event.getPlayerPatch().playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
			}
			
			this.dealEvent(event.getPlayerPatch(), event, advanced);
		}
	}
	
	public void dealEvent(PlayerPatch<?> playerpatch, TakeDamageEvent.Attack event, boolean advanced) {
		event.setCanceled(true);
		event.setResult(AttackResult.ResultType.BLOCKED);
		playerpatch.countHurtTime(event.getDamage());
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getDamageSource().getEntity(), LivingEntityPatch.class)
			.ifPresent(attackerpatch -> attackerpatch.setLastAttackEntity(playerpatch.getOriginal()));
		
		EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(event.getDamageSource().getDirectEntity(), LivingEntity.class, LivingEntityPatch.class)
			.ifPresent(entitypatch -> entitypatch.onAttackBlocked(event.getDamageSource(), playerpatch));
	}
	
	@Override
	public void cancelOnServer(SkillContainer container, FriendlyByteBuf args) {
		container.deactivate();
		container.getExecutor().resetHolding();
		
		ServerPlayerPatch executor = container.getServerExecutor();
		SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, container);
		executor.getEventListener().triggerEvents(EventType.SKILL_CANCEL_EVENT, skillCancelEvent);
		
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPSetSkillContainerValue.activate(container.getSlot(), false, container.getExecutor().getOriginal().getId()), container.getExecutor().getOriginal());
	}
	
	@OnlyIn(Dist.CLIENT)
	public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
		container.deactivate();
		
		super.cancelOnClient(container, args);
	}
	
	@Override
	public void startHolding(SkillContainer container) {
		container.activate();
		
		if (!container.getExecutor().isLogicalClient()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPSetSkillContainerValue.activate(container.getSlot(), true, container.getExecutor().getOriginal().getId()), container.getExecutor().getOriginal());
		}
	}
	
	@Override
	public void holdTick(SkillContainer container) {
		if (!container.getExecutor().isLogicalClient() && container.isActivated()) {
			container.getDataManager().setDataSync(SkillDataKeys.PENALTY_RESTORE_COUNTER.get(), container.getServerExecutor().getOriginal().tickCount);
		}
	}

	@Override
	public void resetHolding(SkillContainer container) {
		container.deactivate();
	}

	@Override
	public void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, FriendlyByteBuf buffer) {
	}

	@Override
	public void onStopHolding(SkillContainer container, SPSkillExecutionFeedback feedback) {
		container.deactivate();
	}
	
  @Override
	public KeyMapping getKeyMapping() {
		return EpicFightKeyMappings.GUARD;
	}
	
    @Override
    public boolean canExecute(SkillContainer container) {
		return this.checkExecuteCondition(container) && this.isHoldingWeaponAvailable(container.getExecutor(), container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND), BlockType.GUARD);
	}
    
	protected float getPenalizer(CapabilityItem itemCapability) {
		return this.penalizer;
	}
	
	protected Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> getGuardMotionMap(BlockType blockType) {
        return switch (blockType) {
            case GUARD_BREAK -> this.guardBreakMotions;
            case GUARD -> this.guardMotions;
            case ADVANCED_GUARD -> this.advancedGuardMotions;
        };
	}
	
	public boolean isHoldingWeaponAvailable(PlayerPatch<?> playerpatch, CapabilityItem itemCapability, BlockType blockType) {
		AnimationAccessor<? extends StaticAnimation> anim = itemCapability.getGuardMotion(this, blockType, playerpatch);
		
		if (anim != null) {
			return true;
		}
		
		Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, ?>> guardMotions = this.getGuardMotionMap(blockType);
		
		if (!guardMotions.containsKey(itemCapability.getWeaponCategory())) {
			return false;
		}
		
		Object motion = guardMotions.get(itemCapability.getWeaponCategory()).apply(itemCapability, playerpatch);
		
		return motion != null;
	}
	
	/**
	 * Not safe from null pointer exception
	 * Must call isAvailableState first to check if it's safe
	 * 
	 * @return AnimationAccessor
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	protected AnimationAccessor<? extends StaticAnimation> getGuardMotion(SkillContainer container, PlayerPatch<?> playerpatch, CapabilityItem itemCapability, BlockType blockType) {
		AnimationAccessor<? extends StaticAnimation> animation = itemCapability.getGuardMotion(this, blockType, playerpatch);
		
		if (animation != null) {
			return animation;
		}
		
		return (AnimationAccessor<? extends StaticAnimation>)this.getGuardMotionMap(blockType).getOrDefault(itemCapability.getWeaponCategory(), (a, b) -> null).apply(itemCapability, playerpatch);
	}
	
	@Override
	public void updateContainer(SkillContainer container) {
		super.updateContainer(container);
		
		if (!container.getExecutor().isLogicalClient()) {
			if (!container.getExecutor().isHoldingSkill(this)) {
				float penalty = container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get());
				
				if (penalty > 0.0F) {
					int hitTick = container.getDataManager().getDataValue(SkillDataKeys.PENALTY_RESTORE_COUNTER.get());
					
					if (container.getExecutor().getOriginal().tickCount - hitTick > 40) {
						container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), 0.0F);
					}
				}
			} else {
				container.getExecutor().resetActionTick();
			}
		}
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		return executor.isEpicFightMode() && !(executor.isInAir() || executor.getEntityState().hurt()) && executor.getEntityState().canUseSkill() && !executor.isHoldingAny();
	}
	
	protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
		return !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				&& !damageSource.is(EpicFightDamageTypeTags.UNBLOCKALBE)
				&& !damageSource.is(DamageTypeTags.BYPASSES_ARMOR)
				&& !damageSource.is(DamageTypeTags.IS_PROJECTILE)
				&& !damageSource.is(DamageTypeTags.IS_EXPLOSION)
				&& !damageSource.is(DamageTypes.MAGIC)
				&& !damageSource.is(DamageTypeTags.IS_FIRE);
	}
	
	@Override
	public boolean shouldDraw(SkillContainer container) {
		return container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get()) > 0.0F;
	}
	
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
		guiGraphics.blit(EpicFightSkills.GUARD.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		guiGraphics.drawString(gui.getFont(), String.format("x%.1f", container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get())), x, y + 6, 16777215, true);
		poseStack.popPose();
	}
	
	
	public static Entity getOffender(DamageSource damageSource) {
		return damageSource.getDirectEntity() == null ? damageSource.getEntity() : damageSource.getDirectEntity();
	}
	
	@Override
	public Set<WeaponCategory> getAvailableWeaponCategories() {
		return this.guardMotions.keySet();
	}
	
	@Override
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("skill.epicfight.guard.consume.tooltip"), SkillBookScreen.STAMINA_TEXTURE_INFO);
		return true;
	}
	
	protected boolean isAdvancedGuard() {
		return false;
	}
	
	public enum BlockType {
		GUARD_BREAK, GUARD, ADVANCED_GUARD
	}
}