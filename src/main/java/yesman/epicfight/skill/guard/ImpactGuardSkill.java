package yesman.epicfight.skill.guard;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.event.types.entity.TakeDamageEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.List;
import java.util.Set;

public class ImpactGuardSkill extends GuardSkill {
	public static GuardSkill.Builder createImpactGuardBuilder() {
		return GuardSkill.createGuardBuilder(ImpactGuardSkill::new)
				.addAdvancedGuardMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
				.addAdvancedGuardMotion(WeaponCategories.SPEAR, (item, player) -> item.getStyle(player) == Styles.TWO_HAND ? Animations.SPEAR_GUARD_HIT : null)
				.addAdvancedGuardMotion(WeaponCategories.TACHI, (item, player) -> Animations.LONGSWORD_GUARD_HIT)
				.addAdvancedGuardMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.GREATSWORD_GUARD_HIT);
	}
	
	protected float superiorPenalizer;
	protected float damageReducer;
	
	public ImpactGuardSkill(GuardSkill.Builder builder) {
		super(builder);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		
		this.superiorPenalizer = parameters.getFloat("superior_penalizer");
		this.damageReducer = parameters.getFloat("damage_reducer");
	}

	@Override
	public void guard(SkillContainer container, CapabilityItem itemCapability, ServerPlayerPatch playerPatch, TakeDamageEvent.Income event, float knockback, float impact, boolean advanced) {
		boolean canUse = this.isHoldingWeaponAvailable(playerPatch, itemCapability, BlockType.ADVANCED_GUARD);
		
		if (event.getDamageSource().is(DamageTypeTags.IS_EXPLOSION)) {
			impact = event.getDamage();
		}
		
		super.guard(container, itemCapability, playerPatch, event, knockback, impact, canUse);
	}
	
	@Override
	public void dealEvent(PlayerPatch<?> playerpatch, TakeDamageEvent.Income event, boolean advanced) {
		boolean isSpecialSource = isAdvancedBlockableDamageSource(event.getDamageSource());
		
		if (isSpecialSource) {
			EpicFightDamageSource efsource;
			
			if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamagesource) {
				efsource = epicfightDamagesource;
			} else {
				efsource = EpicFightDamageSources.fromVanillaDamageSource(event.getDamageSource());
			}
			
			efsource.addRuntimeTag(EpicFightDamageTypeTags.UNBLOCKALBE);
			efsource.addRuntimeTag(EpicFightDamageTypeTags.NO_STUN);
			
			playerpatch.getOriginal().hurt(efsource, event.getDamage() * this.damageReducer);
		}
		
		event.setResult(isSpecialSource ? AttackResult.ResultType.SUCCESS : AttackResult.ResultType.BLOCKED);
		playerpatch.countHurtTime(event.getDamage());
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getDamageSource().getEntity(), LivingEntityPatch.class).ifPresent(attackerpatch -> {
			attackerpatch.setLastAttackEntity(playerpatch.getOriginal());
		});
		
		if (event.getDamageSource() instanceof EpicFightDamageSource epicfightDamageSource) {
			epicfightDamageSource.setStunType(StunType.NONE);
		}
		
		event.cancel();
		Entity directEntity = event.getDamageSource().getDirectEntity();
		
		if (advanced) {
			LivingEntity original = playerpatch.getOriginal();
			EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(((ServerLevel)original.level()), original, directEntity);
		}
		
		EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(directEntity, LivingEntity.class, LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.onAttackBlocked(event.getDamageSource(), playerpatch);
		});
	}
	
	@Override
	protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
		return !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
				&& !damageSource.is(EpicFightDamageTypeTags.UNBLOCKALBE)
				&& (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR)
					&& !damageSource.is(DamageTypeTags.IS_PROJECTILE)
					&& !damageSource.is(DamageTypeTags.IS_EXPLOSION)
					&& !damageSource.is(DamageTypes.MAGIC) 
					&& !damageSource.is(DamageTypeTags.IS_FIRE)
					|| advanced);
	}
	
	@Override
	public float getPenalizer(CapabilityItem itemCap) {
		return this.advancedGuardMotions.containsKey(itemCap.getWeaponCategory()) ? this.superiorPenalizer : this.penalizer;
	}
	
	private static boolean isAdvancedBlockableDamageSource(DamageSource damageSource) {
		return damageSource.is(DamageTypeTags.IS_EXPLOSION)
				|| damageSource.is(DamageTypes.MAGIC)
				|| damageSource.is(DamageTypeTags.IS_FIRE)
				|| damageSource.is(DamageTypeTags.IS_PROJECTILE)
				|| damageSource.is(DamageTypeTags.BYPASSES_ARMOR);
	}
	
	@Override
	public Skill getPriorSkill() {
		return EpicFightSkills.GUARD.get();
	}
	
	@Override
	protected boolean isAdvancedGuard() {
		return true;
	}
	
	@Override @ClientOnly
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(String.format("%.1f", this.damageReducer * 100.0D));
		
		return list;
	}
	
	@Override
	public Set<WeaponCategory> getAvailableWeaponCategories() {
		return this.advancedGuardMotions.keySet();
	}
}
