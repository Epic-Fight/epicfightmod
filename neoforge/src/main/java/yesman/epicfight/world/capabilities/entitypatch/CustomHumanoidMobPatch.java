package yesman.epicfight.world.capabilities.entitypatch;

import com.mojang.datafixers.util.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.behavior.AnimatedCombatBehavior;
import yesman.epicfight.world.entity.ai.behavior.MoveToTargetSinkStopInaction;
import yesman.epicfight.world.entity.ai.brain.BrainRecomposer;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class CustomHumanoidMobPatch<T extends PathfinderMob> extends HumanoidMobPatch<T> {
	private final MobPatchReloadListener.CustomHumanoidMobPatchProvider provider;

	public CustomHumanoidMobPatch(T original, Faction faction, MobPatchReloadListener.CustomHumanoidMobPatchProvider provider) {
		super(original, faction);
		
		this.provider = provider;
		this.weaponLivingMotions = this.provider.getHumanoidWeaponMotions();
		this.weaponAttackMotions = this.provider.getHumanoidCombatBehaviors();
	}
	
	@Override
	public void setAIAsInfantry(boolean holdingRanedWeapon) {
		boolean useBrain = !this.original.getBrain().availableBehaviorsByPriority.isEmpty();
		
		if (useBrain) {
			if (!holdingRanedWeapon) {
				CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
				BrainRecomposer.recomposeBrainByType(this.original.getType(), this.original.getBrain(), (builder != null) ? new AnimatedCombatBehavior<>(this, builder.build(this)) : null, new MoveToTargetSinkStopInaction());
			}
		} else {
			if (!holdingRanedWeapon) {
				CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
				
				if (builder != null) {
					this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, builder.build(this)));
					this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.getOriginal(), this.provider.getChasingSpeed(), true));
				}
			}
		}
	}
	
	@Override
	public void readData(CompoundTag compound) {
		super.readData(compound);
		this.initAttributes();
	}
	
	public void initAttributes() {
		this.original.getAttribute(EpicFightAttributes.MAX_STRIKES).setBaseValue(this.provider.getAttributeValues().getDouble(EpicFightAttributes.MAX_STRIKES));
		this.original.getAttribute(EpicFightAttributes.ARMOR_NEGATION).setBaseValue(this.provider.getAttributeValues().getDouble(EpicFightAttributes.ARMOR_NEGATION));
		this.original.getAttribute(EpicFightAttributes.IMPACT).setBaseValue(this.provider.getAttributeValues().getDouble(EpicFightAttributes.IMPACT));
		this.original.getAttribute(EpicFightAttributes.STUN_ARMOR).setBaseValue(this.provider.getAttributeValues().getDouble(EpicFightAttributes.STUN_ARMOR));
		
		if (this.provider.getAttributeValues().containsKey(Attributes.ATTACK_DAMAGE)) {
			this.original.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.provider.getAttributeValues().getDouble(Attributes.ATTACK_DAMAGE));
		}
	}
	
	@Override
	public void initAnimator(Animator animator) {
		super.initAnimator(animator);
		
		for (Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>> pair : this.provider.getDefaultAnimations()) {
			animator.addLivingAnimation(pair.getFirst(), pair.getSecond());
		}
	}
	
	@Override
	public void updateMotion(boolean considerInaction) {
		super.commonAggressiveMobUpdateMotion(considerInaction);
		
		if (this.original.isUsingItem()) {
			CapabilityItem activeItem = this.getHoldingItemCapability(this.original.getUsedItemHand());
			UseAnim useAnim = this.original.getItemInHand(this.original.getUsedItemHand()).getUseAnimation();
			UseAnim secondUseAnim = activeItem.getUseAnimation(this);
			
			if (useAnim == UseAnim.BLOCK || secondUseAnim == UseAnim.BLOCK)
				if (activeItem.getWeaponCategory() == WeaponCategories.SHIELD)
					currentCompositeMotion = LivingMotions.BLOCK_SHIELD;
				else
					currentCompositeMotion = LivingMotions.BLOCK;
			else if (useAnim == UseAnim.BOW || useAnim == UseAnim.SPEAR)
				currentCompositeMotion = LivingMotions.AIM;
			else if (useAnim == UseAnim.CROSSBOW)
				currentCompositeMotion = LivingMotions.RELOAD;
			else
				currentCompositeMotion = currentLivingMotion;
		} else {
			if (CrossbowItem.isCharged(this.original.getMainHandItem()))
				currentCompositeMotion = LivingMotions.AIM;
			else if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().get().isReboundAnimation())
				currentCompositeMotion = LivingMotions.NONE;
			else if (this.original.swinging && this.original.getSleepingPos().isEmpty())
				currentCompositeMotion = LivingMotions.DIGGING;
			else
				currentCompositeMotion = currentLivingMotion;
		}
	}
	
	@Override
	public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
		return this.provider.getStunAnimations().get(stunType);
	}
	
	@Override
	public SoundEvent getWeaponHitSound(InteractionHand hand) {
		CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
		
		if (itemCap.isEmpty()) {
			return this.provider.getHitSound();
		}
		
		return itemCap.getHitSound();
	}
	
	@Override
	public SoundEvent getSwingSound(InteractionHand hand) {
		CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
		
		if (itemCap.isEmpty()) {
			return this.provider.getSwingSound();
		}
		
		return itemCap.getSmashingSound();
	}
	
	@Override
	public HitParticleType getWeaponHitParticle(InteractionHand hand) {
		CapabilityItem itemCap = this.getAdvancedHoldingItemCapability(hand);
		
		if (itemCap.isEmpty()) {
			return this.provider.getHitParticle();
		}
		
		return itemCap.getHitParticle();
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		float scale = this.provider.getScale();
		
		return super.getModelMatrix(partialTicks).scale(scale, scale, scale);
	}
}