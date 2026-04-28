package yesman.epicfight.skill.guard;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;

public class ParryingSkill extends GuardSkill {
	private int PARRY_WINDOW;
	
	public static GuardSkill.Builder createActiveGuardBuilder() {
		return GuardSkill.createGuardBuilder()
				.addAdvancedGuardMotion(WeaponCategories.SWORD, (itemCap, playerpatch) -> itemCap.getStyle(playerpatch) == Styles.ONE_HAND ?
					List.of(Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2) : List.of(Animations.SWORD_GUARD_ACTIVE_HIT2, Animations.SWORD_GUARD_ACTIVE_HIT3))
				.addAdvancedGuardMotion(WeaponCategories.LONGSWORD, (itemCap, playerpatch) ->
					List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2 ))
				.addAdvancedGuardMotion(WeaponCategories.UCHIGATANA, (itemCap, playerpatch) ->
					List.of(Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2 ))
				.addAdvancedGuardMotion(WeaponCategories.TACHI, (itemCap, playerpatch) ->
					List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2 ));
	}
	
	public ParryingSkill(GuardSkill.Builder builder) {
		super(builder);
	}

	@Override
	public void startHolding(SkillContainer container)
	{
		super.startHolding(container);
		if (container.getExecutor().isLogicalClient())
			return;
        int lastActive = container.getDataManager().getDataValue(SkillDataKeys.LAST_ACTIVE.get());
		if (container.getServerExecutor().getOriginal().tickCount - lastActive > PARRY_WINDOW * 2) {
			container.getDataManager().setDataSync(SkillDataKeys.LAST_ACTIVE.get(), container.getServerExecutor().getOriginal().tickCount);
		}
	}

	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
	}
	
	@Override
	public void guard(SkillContainer container, CapabilityItem itemCapability, TakeDamageEvent.Attack event, float knockback, float impact, boolean advanced) {
		if (this.isHoldingWeaponAvailable(event.getPlayerPatch(), itemCapability, BlockType.ADVANCED_GUARD)) {
			DamageSource damageSource = event.getDamageSource();
			Entity offender = getOffender(damageSource);
			
			if (offender != null && this.isBlockableSource(damageSource, true)) {
				ServerPlayer serverPlayer = event.getPlayerPatch().getOriginal();
				boolean successParrying = serverPlayer.tickCount - container.getDataManager().getDataValue(SkillDataKeys.LAST_ACTIVE.get()) < PARRY_WINDOW;
				float penalty = container.getDataManager().getDataValue(SkillDataKeys.PENALTY.get());
				event.getPlayerPatch().playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
				
				EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serverPlayer.serverLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serverPlayer, offender);
				
				if (successParrying) {
					event.setParried(true);
					penalty = 0.1F;
					knockback *= 0.4F;
					
					// Solution by Cyber2049(github): Fix continuous parry
					container.getDataManager().setData(SkillDataKeys.LAST_ACTIVE.get(), 0);
				} else {
					penalty += this.getPenalizer(itemCapability);
					container.getDataManager().setDataSync(SkillDataKeys.PENALTY.get(), penalty);
				}
				
				if (offender instanceof LivingEntity livingentity) {
					knockback += EnchantmentHelper.getKnockbackBonus(livingentity) * 0.1F;
				}

                assert offender != null;
                event.getPlayerPatch().knockBackEntity(offender.position(), knockback);
				float consumeAmount = penalty * impact;
				boolean canAfford = event.getPlayerPatch().consumeForSkill(this, Skill.Resource.STAMINA, consumeAmount);
				
				BlockType blockType = successParrying ? BlockType.ADVANCED_GUARD : (canAfford ? BlockType.GUARD : BlockType.GUARD_BREAK);
				AnimationAccessor<? extends StaticAnimation> animation = this.getGuardMotion(container, event.getPlayerPatch(), itemCapability, blockType);
				
				if (animation != null) {
					event.getPlayerPatch().playAnimationSynchronized(animation, 0);
				}
				
				if (blockType == BlockType.GUARD_BREAK) {
					event.getPlayerPatch().playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
				}
				
				this.dealEvent(event.getPlayerPatch(), event, advanced);
				
				return;
			}
		}
		
		super.guard(container, itemCapability, event, knockback, impact, false);
	}
	
	@Override
	protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
		return (damageSource.is(DamageTypeTags.IS_PROJECTILE) && advanced) || super.isBlockableSource(damageSource, false);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	protected AnimationAccessor<? extends StaticAnimation> getGuardMotion(SkillContainer container, PlayerPatch<?> playerpatch, CapabilityItem itemCapability, BlockType blockType) {
		AnimationAccessor<? extends StaticAnimation> animation = itemCapability.getGuardMotion(this, blockType, playerpatch);
		
		if (animation != null) {
			return animation;
		}
		
		if (blockType == BlockType.ADVANCED_GUARD) {
			List<AnimationAccessor<? extends StaticAnimation>> motions = (List<AnimationAccessor<? extends StaticAnimation>>)this.getGuardMotionMap(blockType).getOrDefault(itemCapability.getWeaponCategory(), (a, b) -> null).apply(itemCapability, playerpatch);
			
			if (motions != null) {
				SkillDataManager dataManager = container.getDataManager();
				int motionCounter = dataManager.getDataValue(SkillDataKeys.PARRY_MOTION_COUNTER.get());
				dataManager.setDataF(SkillDataKeys.PARRY_MOTION_COUNTER.get(), (v) -> v + 1);
				motionCounter %= motions.size();
				
				return motions.get(motionCounter);
			}
		}
		
		return super.getGuardMotion(container, playerpatch, itemCapability, blockType);
	}

	@Override
	public void setParams(CompoundTag parameters)
	{
		super.setParams(parameters);
		PARRY_WINDOW = parameters.getInt("parry_window");
		if (PARRY_WINDOW <= 0) {
			PARRY_WINDOW = 8;
		}
	}

	@Override
	public Skill getPriorSkill() {
		return EpicFightSkills.GUARD;
	}
	
	@Override
	protected boolean isAdvancedGuard() {
		return true;
	}
	
	@Override
	public Set<WeaponCategory> getAvailableWeaponCategories() {
		return this.advancedGuardMotions.keySet();
	}
}