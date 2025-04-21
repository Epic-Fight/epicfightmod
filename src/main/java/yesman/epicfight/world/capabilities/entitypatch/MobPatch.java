package yesman.epicfight.world.capabilities.entitypatch;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSetAttackTarget;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public abstract class MobPatch<T extends Mob> extends LivingEntityPatch<T> {
	protected final Faction mobFaction;
	
	public MobPatch() {
		this.mobFaction = Faction.NEUTRAL;
	}
	
	public MobPatch(Faction faction) {
		this.mobFaction = faction;
	}
	
	@Override
	public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
		super.onJoinWorld(entity, event);
		
		if (!entity.level().isClientSide() && !this.original.isNoAi()) {
			this.initAI();
		}
	}
	
	protected void initAI() {
		if (this.original.getBrain().availableBehaviorsByPriority.isEmpty()) {
			Set<Goal> toRemove = Sets.newHashSet();
			this.selectGoalToRemove(toRemove);
			toRemove.forEach(this.original.goalSelector::removeGoal);
		}
	}
	
	protected void selectGoalToRemove(Set<Goal> toRemove) {
		for (WrappedGoal wrappedGoal : this.original.goalSelector.getAvailableGoals()) {
			Goal goal = wrappedGoal.getGoal();
			
			if (goal instanceof MeleeAttackGoal || goal instanceof AnimatedAttackGoal || goal instanceof RangedAttackGoal || goal instanceof TargetChasingGoal) {
				toRemove.add(goal);
			}
		}
	}
	
	protected final void commonMobUpdateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.INACTION;
		} else {
			if (original.getVehicle() != null)
				currentLivingMotion = LivingMotions.MOUNT;
			else
				if (this.original.getDeltaMovement().y < -0.55F || this.isAirborneState())
					currentLivingMotion = LivingMotions.FALL;
				else if (original.walkAnimation.speed() > 0.01F)
					currentLivingMotion = LivingMotions.WALK;
				else
					currentLivingMotion = LivingMotions.IDLE;
		}
		
		this.currentCompositeMotion = this.currentLivingMotion;
	}
	
	protected final void commonAggressiveMobUpdateMotion(boolean considerInaction) {
		if (this.original.getHealth() <= 0.0F) {
			currentLivingMotion = LivingMotions.DEATH;
		} else if (this.state.inaction() && considerInaction) {
			currentLivingMotion = LivingMotions.IDLE;
		} else {
			if (original.getVehicle() != null) {
				currentLivingMotion = LivingMotions.MOUNT;
			} else {
				if (this.original.getDeltaMovement().y < -0.55F || this.isAirborneState())
					currentLivingMotion = LivingMotions.FALL;
				else if (original.walkAnimation.speed() > 0.08F)
					if (original.isAggressive())
						currentLivingMotion = LivingMotions.CHASE;
					else
						currentLivingMotion = LivingMotions.WALK;
				else
					currentLivingMotion = LivingMotions.IDLE;
			}
		}
		
		this.currentCompositeMotion = this.currentLivingMotion;
	}
	
	protected final void commonAggressiveRangedMobUpdateMotion(boolean considerInaction) {
		this.commonAggressiveMobUpdateMotion(considerInaction);
		UseAnim useAction = this.original.getItemInHand(this.original.getUsedItemHand()).getUseAnimation();
		
		if (this.original.isUsingItem()) {
			if (useAction == UseAnim.CROSSBOW)
				currentCompositeMotion = LivingMotions.RELOAD;
			else
				currentCompositeMotion = LivingMotions.AIM;
		} else {
			if (this.getClientAnimator().getCompositeLayer(Layer.Priority.MIDDLE).animationPlayer.getAnimation().get().isReboundAnimation())
				currentCompositeMotion = LivingMotions.NONE;
		}
		
		if (CrossbowItem.isCharged(this.original.getMainHandItem()))
			currentCompositeMotion = LivingMotions.AIM;
		else if (!this.getEntityState().inaction() && this.getClientAnimator().isAiming() && currentCompositeMotion != LivingMotions.AIM)
			this.playShootingAnimation();
	}
	
	@Override
	public void updateArmor(CapabilityItem fromCap, CapabilityItem toCap, EquipmentSlot slotType) {
		if (this.original.getAttributes().hasAttribute(EpicFightAttributes.STUN_ARMOR.get())) {
			if (fromCap != null) {
				this.original.getAttributes().removeAttributeModifiers(fromCap.getAttributeModifiers(slotType, this));
			}
			
			if (toCap != null) {
				this.original.getAttributes().addTransientAttributeModifiers(toCap.getAttributeModifiers(slotType, this));
			}
		}
	}
	
	@Override
	public boolean isTargetInvulnerable(Entity entity) {
		MobPatch<?> mobpatch = EpicFightCapabilities.getEntityPatch(entity, MobPatch.class);
		
		if (mobpatch != null && mobpatch.mobFaction.equals(this.mobFaction)) {
			if (this.getTarget() == null) {
				return true;
			} else {
				return !this.getTarget().is(entity);
			}
		}
		
		return super.isTargetInvulnerable(entity);
	}
	
	@Override
	public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
		boolean offhandValid = this.isOffhandItemValid();
		ItemStack mainHandItem = this.getOriginal().getMainHandItem();
		ItemStack offHandItem = this.getOriginal().getOffhandItem();
		Collection<AttributeModifier> mainHandAttributes = CapabilityItem.getAttributeModifiers(Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, this.original.getMainHandItem(), this);
		Collection<AttributeModifier> offHandAttributes = this.isOffhandItemValid() ? CapabilityItem.getAttributeModifiers(Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, this.original.getOffhandItem(), this) : Set.of();
		
		this.epicFightDamageSource = damageSource;
		this.setOffhandDamage(hand, mainHandItem, offHandItem, offhandValid, mainHandAttributes, offHandAttributes);
		this.original.doHurtTarget(target);
		this.recoverMainhandDamage(hand, mainHandItem, offHandItem, mainHandAttributes, offHandAttributes);
		this.epicFightDamageSource = null;
		
		return super.attack(damageSource, target, hand);
	}
	
	@Override
	public LivingEntity getTarget() {
		return this.original.getTarget();
	}
	
	public void setAttakTargetSync(LivingEntity entityIn) {
		if (!this.original.level().isClientSide()) {
			this.original.setTarget(entityIn);
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPSetAttackTarget(this.original.getId(), entityIn != null ? entityIn.getId() : -1), this.original);
		}
	}
	
	@Override
	public float getAttackDirectionPitch() {
		Entity attackTarget = this.getTarget();
		
		if (attackTarget != null) {
			float partialTicks = EpicFightSharedConstants.isPhysicalClient() ? Minecraft.getInstance().getFrameTime() : 1.0F;
			Vec3 target = attackTarget.getEyePosition(partialTicks);
			Vec3 vector3d = this.original.getEyePosition(partialTicks);
			double d0 = target.x - vector3d.x;
			double d1 = target.y - vector3d.y;
			double d2 = target.z - vector3d.z;
			double d3 = Math.sqrt(d0 * d0 + d2 * d2);
			return Mth.clamp(Mth.wrapDegrees((float) ((Mth.atan2(d1, d3) * (double) (180F / (float) Math.PI)))), -30.0F, 30.0F);
		} else {
			return super.getAttackDirectionPitch();
		}
	}
}