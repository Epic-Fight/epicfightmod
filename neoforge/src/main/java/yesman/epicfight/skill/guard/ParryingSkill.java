package yesman.epicfight.skill.guard;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.types.entity.TakeDamageEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ParryingSkill extends GuardSkill {
	public static GuardSkill.Builder createActiveGuardBuilder() {
		return GuardSkill.createGuardBuilder(ParryingSkill::new)
                .addAdvancedGuardMotion(WeaponCategories.SWORD, (itemCap, playerpatch) -> itemCap.getStyle(playerpatch) == Styles.ONE_HAND ? List.of(Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2) : List.of(Animations.SWORD_GUARD_ACTIVE_HIT2, Animations.SWORD_GUARD_ACTIVE_HIT3))
                .addAdvancedGuardMotion(WeaponCategories.LONGSWORD, (itemCap, playerpatch) -> List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2 ))
                .addAdvancedGuardMotion(WeaponCategories.UCHIGATANA, (itemCap, playerpatch) -> List.of(Animations.SWORD_GUARD_ACTIVE_HIT1, Animations.SWORD_GUARD_ACTIVE_HIT2 ))
                .addAdvancedGuardMotion(WeaponCategories.TACHI, (itemCap, playerpatch) -> List.of(Animations.LONGSWORD_GUARD_ACTIVE_HIT1, Animations.LONGSWORD_GUARD_ACTIVE_HIT2 ));
	}
	
	private int parryWindow;
	
	public ParryingSkill(GuardSkill.Builder builder) {
		super(builder);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		
		this.parryWindow = parameters.getInt("parry_window");
		
		if (this.parryWindow <= 0) {
			this.parryWindow = 8;
		}
	}

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        skillContainer.runOnServer(playerpatch -> {
            CapabilityItem itemCapability = skillContainer.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);

            if (this.isHoldingWeaponAvailable(skillContainer.getExecutor(), itemCapability, BlockType.GUARD) && this.isExecutableState(skillContainer.getExecutor())) {
                skillContainer.getExecutor().getOriginal().startUsingItem(InteractionHand.MAIN_HAND);
            }

            int lastActive = skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.LAST_ACTIVE);

            if (skillContainer.getExecutor().getOriginal().tickCount - lastActive > this.parryWindow * 2) {
                skillContainer.getDataManager().setData(EpicFightSkillDataKeys.LAST_ACTIVE, skillContainer.getExecutor().getOriginal().tickCount);
            }
        });
    }

	@Override
	public void startHolding(SkillContainer skillContainer) {
		super.startHolding(skillContainer);
		
		skillContainer.runOnServer(serverExecutor -> {
			 int lastActive = skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.LAST_ACTIVE);
			 if (serverExecutor.getOriginal().tickCount - lastActive > this.parryWindow * 2) {
				 skillContainer.getDataManager().setDataSync(EpicFightSkillDataKeys.LAST_ACTIVE, serverExecutor.getOriginal().tickCount);
			 }
		});
	}
	
	@Override
	public void guard(SkillContainer container, CapabilityItem itemCapability, ServerPlayerPatch playerPatch, TakeDamageEvent.Income event, float knockback, float impact, boolean advanced) {
		if (this.isHoldingWeaponAvailable(playerPatch, itemCapability, BlockType.ADVANCED_GUARD)) {
			DamageSource damageSource = event.getDamageSource();
			Entity offender = getOffender(damageSource);
			
			if (offender != null && this.isBlockableSource(damageSource, true)) {
				ServerPlayer serverplayer = playerPatch.getOriginal();
				boolean successParrying = serverplayer.tickCount - container.getDataManager().getDataValue(EpicFightSkillDataKeys.LAST_ACTIVE) < this.parryWindow;
				float penalty = container.getDataManager().getDataValue(EpicFightSkillDataKeys.PENALTY);
				playerPatch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
				EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serverplayer.serverLevel(), HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serverplayer, offender);
				
				if (successParrying) {
					event.setParried(true);
					penalty = 0.1F;
					knockback *= 0.4F;
					
					// Solution by Cyber2049(github): Fix continuous parry
					container.getDataManager().setData(EpicFightSkillDataKeys.LAST_ACTIVE, 0);
				} else {
					penalty += this.getPenalizer(itemCapability);
					container.getDataManager().setDataSync(EpicFightSkillDataKeys.PENALTY, penalty);
				}
				
				if (offender instanceof LivingEntity livingentity) {
					float modifiedKnockback = EnchantmentHelper.modifyKnockback(serverplayer.serverLevel(), livingentity.getItemInHand(livingentity.getUsedItemHand()), livingentity, damageSource, knockback);
					knockback = (modifiedKnockback - knockback) * 0.1F;
				}

                playerPatch.knockBackEntity(offender.position(), knockback);
				float consumeAmount = penalty * impact;
				boolean canAfford = playerPatch.consumeForSkill(this, Skill.Resource.STAMINA, consumeAmount);
				
				BlockType blockType = successParrying ? BlockType.ADVANCED_GUARD : (canAfford ? BlockType.GUARD : BlockType.GUARD_BREAK);
				AnimationAccessor<? extends StaticAnimation> animation = this.getGuardMotion(container, playerPatch, itemCapability, blockType);
				
				if (animation != null) {
					playerPatch.playAnimationSynchronized(animation, 0);
				}
				
				if (blockType == BlockType.GUARD_BREAK) {
					playerPatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
				}
				
				this.dealEvent(playerPatch, event, advanced);
				
				return;
			}
		}
		
		super.guard(container, itemCapability, playerPatch, event, knockback, impact, false);
	}
	
	@Override
	protected boolean isBlockableSource(DamageSource damageSource, boolean advanced) {
		return (damageSource.is(DamageTypeTags.IS_PROJECTILE) && advanced) || super.isBlockableSource(damageSource, false);
	}
	
	@Nullable
	protected AnimationAccessor<? extends StaticAnimation> getGuardMotion(SkillContainer container, PlayerPatch<?> playerpatch, CapabilityItem itemCapability, BlockType blockType) {
		if (blockType != BlockType.ADVANCED_GUARD)
        {
            return super.getGuardMotion(container, playerpatch, itemCapability, blockType);
        }
        SkillDataManager dataManager = container.getDataManager();
        AnimationAccessor<? extends StaticAnimation> result = itemCapability.getGuardMotion(this, blockType, playerpatch);
		if (result == null)
        {
            @SuppressWarnings("unchecked")
            List<AnimationAccessor<? extends StaticAnimation>> motions = (List<AnimationAccessor<? extends StaticAnimation>>)this.getGuardMotionMap(blockType).getOrDefault(itemCapability.getWeaponCategory(), (a, b) -> null).apply(itemCapability, playerpatch);

            if (motions != null) {
                int motionCounter = dataManager.getDataValue(EpicFightSkillDataKeys.PARRY_MOTION_COUNTER);
                motionCounter %= motions.size();
                result = motions.get(motionCounter);
            }
        }
        dataManager.setDataSyncF(EpicFightSkillDataKeys.PARRY_MOTION_COUNTER, (v) -> v + 1);
        return result != null ? result : super.getGuardMotion(container, playerpatch, itemCapability, blockType);
	}
	
	@Override
	public Skill getPriorSkill() {
		return EpicFightSkills.GUARD.get();
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