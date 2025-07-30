package yesman.epicfight.world.capabilities.entitypatch;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.AttackResult.ResultType;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.model.armature.types.ToolHolderArmature;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.TargetIndicatorCheckEvent;

public abstract class LivingEntityPatch<T extends LivingEntity> extends HurtableEntityPatch<T> {
	protected static EntityDataAccessor<Float> STUN_SHIELD;
	protected static EntityDataAccessor<Float> MAX_STUN_SHIELD;
	protected static EntityDataAccessor<Integer> EXECUTION_RESISTANCE;
	protected static EntityDataAccessor<Boolean> AIRBORNE;
	
	public static void initLivingEntityDataAccessor() {
		STUN_SHIELD = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
		MAX_STUN_SHIELD = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
		EXECUTION_RESISTANCE = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
		AIRBORNE = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
	}
	
	public static void createSyncedEntityData(LivingEntity livingentity) {
		livingentity.getEntityData().define(STUN_SHIELD, Float.valueOf(0.0F));
		livingentity.getEntityData().define(MAX_STUN_SHIELD, Float.valueOf(0.0F));
		livingentity.getEntityData().define(EXECUTION_RESISTANCE, Integer.valueOf(0));
		livingentity.getEntityData().define(AIRBORNE, Boolean.valueOf(false));
	}
	
	public static final double WEIGHT_CORRECTION = 37.037D;
	
	protected Armature armature;
	protected Animator animator;
	protected EntityState state = EntityState.DEFAULT_STATE;
	
	protected Vec3 lastAttackPosition;
	protected EpicFightDamageSource epicFightDamageSource;
	
	protected boolean isLastAttackSuccess;
	protected float lastDealDamage;
	protected ResultType lastAttackResultType;
	
	protected Entity lastTryHurtEntity;
	protected LivingEntity grapplingTarget;
	
	public LivingMotion currentLivingMotion = LivingMotions.IDLE;
	public LivingMotion currentCompositeMotion = LivingMotions.IDLE;
	
	private Style oStyle;
	private final Map<InteractionHand, Joint> parentJointOfHands = Maps.newHashMap();
	
	@Override
	public void onConstructed(T entityIn) {
		super.onConstructed(entityIn);
		
		this.armature = Armatures.getArmatureFor(this);
		
		Animator animator = EpicFightSharedConstants.getAnimator(this);
		this.animator = animator;
		
		this.initAnimator(animator);
		animator.postInit();
	}
	
	protected void initAnimator(Animator animator) {
		animator.getVariables().putDefaultSharedVariable(AttackAnimation.HIT_ENTITIES);
		animator.getVariables().putDefaultSharedVariable(AttackAnimation.HURT_ENTITIES);
		animator.getVariables().putDefaultSharedVariable(ActionAnimation.ACTION_ANIMATION_COORD);
		
		if (this.armature instanceof ToolHolderArmature toolArmature) {
			this.setParentJointOfHand(InteractionHand.MAIN_HAND, toolArmature.rightToolJoint());
			this.setParentJointOfHand(InteractionHand.OFF_HAND, toolArmature.leftToolJoint());
		}
	}
	
	@Override
	public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
		super.onJoinWorld(entity, event);
		
		if (entity.getAttributeBaseValue(EpicFightAttributes.WEIGHT.get()) == 0.0D) {
			EntityDimensions entityDimensions = entity.getDimensions(net.minecraft.world.entity.Pose.STANDING);
			double weight = entityDimensions.width * entityDimensions.height * WEIGHT_CORRECTION;
			
			entity.getAttribute(EpicFightAttributes.WEIGHT.get()).setBaseValue(weight);
		}
	}
	
	public abstract void updateMotion(boolean considerInaction);
	
	public Armature getArmature() {
		return this.armature;
	}
	
	@Override
	public void tick(LivingEvent.LivingTickEvent event) {
		super.tick(event);
		
		this.oStyle = this.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(this);
		
		if (this.original.getHealth() <= 0.0F) {
			this.original.setXRot(0);
			
			AnimationPlayer animPlayer = this.getAnimator().getPlayerFor(null);
			
			if (this.original.deathTime >= 19 && !animPlayer.isEmpty() && !animPlayer.isEnd()) {
				this.original.deathTime--;
			}
		}
		
		this.animator.tick();
		
		if (this.isLogicalClient()) {
			this.clientTick(event);
		} else {
			this.serverTick(event);
		}
		
		if (this.original.deathTime == 19) {
			this.aboutToDeath();
		}
		
		if (!this.getEntityState().inaction() && this.original.onGround && this.isAirborneState()) {
			this.setAirborneState(false);
		}
	}
	
	protected void clientTick(LivingEvent.LivingTickEvent event) {}
	
	protected void serverTick(LivingEvent.LivingTickEvent event) {}
	
	public void poseTick(DynamicAnimation animation, Pose pose, float elapsedTime, float partialTicks) {
		if (pose.hasTransform("Head") && this.armature.hasJoint("Head")) {
			if (animation.doesHeadRotFollowEntityHead()) {
				float headRotO = this.original.yBodyRotO - this.original.yHeadRotO;
				float headRot = this.original.yBodyRot - this.original.yHeadRot;
				float partialHeadRot = MathUtils.lerpBetween(headRotO, headRot, partialTicks);
				OpenMatrix4f toOriginalRotation = new OpenMatrix4f(this.armature.getBindedTransformFor(pose, this.armature.searchJointByName("Head"))).removeScale().removeTranslation().invert();
				Vec3f xAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.X_AXIS, null);
				Vec3f yAxis = OpenMatrix4f.transform3v(toOriginalRotation, Vec3f.Y_AXIS, null);
				OpenMatrix4f headRotation = OpenMatrix4f.createRotatorDeg(partialHeadRot, yAxis).rotateDeg(-this.original.getXRot(), xAxis);
				pose.orElseEmpty("Head").frontResult(JointTransform.fromMatrix(headRotation), OpenMatrix4f::mul);
			}
		}
	}
	
	public void onFall(LivingFallEvent event) {
		if (!this.getOriginal().level().isClientSide() && this.isAirborneState()) {
			AssetAccessor<? extends StaticAnimation> fallAnimation = this.getAnimator().getLivingAnimation(LivingMotions.LANDING_RECOVERY, this.getHitAnimation(StunType.FALL));
			
			if (fallAnimation != null) {
				this.playAnimationSynchronized(fallAnimation, 0);
			}
		}
		
		this.setAirborneState(false);
	}
	
	@Override
	public void onDeath(LivingDeathEvent event) {
		this.getAnimator().playDeathAnimation();
		this.currentLivingMotion = LivingMotions.DEATH;
	}
	
	public void updateEntityState() {
		this.state = this.animator.getEntityState();
	}
	
	public void updateEntityState(EntityState entityState) {
		this.state = entityState;
	}
	
	public void cancelItemUse() {
		if (this.original.isUsingItem()) {
			this.original.stopUsingItem();
			ForgeEventFactory.onUseItemStop(this.original, this.original.getUseItem(), this.original.getUseItemRemainingTicks());
		}
	}
	
	public CapabilityItem getHoldingItemCapability(InteractionHand hand) {
		return EpicFightCapabilities.getItemStackCapability(this.original.getItemInHand(hand));
	}
	
	/**
	 * Returns an empty capability if the item in mainhand is incompatible with the item in offhand 
	 */
	public CapabilityItem getAdvancedHoldingItemCapability(InteractionHand hand) {
		if (hand == InteractionHand.MAIN_HAND) {
			return getHoldingItemCapability(hand);
		} else {
			return this.isOffhandItemValid() ? this.getHoldingItemCapability(hand) : CapabilityItem.EMPTY;
		}
	}
	
	public EpicFightDamageSource getDamageSource(AnimationAccessor<? extends StaticAnimation> animation, InteractionHand hand) {
		EpicFightDamageSources damageSources = EpicFightDamageSources.of(this.original.level());
		EpicFightDamageSource damagesource = damageSources.mobAttack(this.original).setAnimation(animation);
		damagesource.setImpact(this.getImpact(hand));
		damagesource.setArmorNegation(this.getArmorNegation(hand));
		damagesource.setHurtItem(this.original.getItemInHand(hand));
		
		return damagesource;
	}
	
	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		return AttackResult.of(this.getEntityState().attackResult(damageSource), amount);
	}
	
	public AttackResult tryHarm(Entity target, EpicFightDamageSource damagesource, float amount) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
		AttackResult result = (entitypatch != null) ? entitypatch.tryHurt(damagesource, amount) : AttackResult.success(amount);
		
		return result;
	}

	@Nullable
	public EpicFightDamageSource getEpicFightDamageSource() {
		return this.epicFightDamageSource;
	}
	
	/**
	 * Swap item and attributes of mainhand for offhand item and attributes
	 * You must call {@link LivingEntityPatch#recoverMainhandDamage} method again after finishing the damaging process.
	 */
	protected void setOffhandDamage(InteractionHand hand, ItemStack mainhandItemStack, ItemStack offhandItemStack, boolean offhandValid, Collection<AttributeModifier> mainhandAttributes, Collection<AttributeModifier> offhandAttributes) {
		if (hand == InteractionHand.MAIN_HAND) {
			return;
		}
		
		/**
		 * Swap hand items to decrease the durability of offhand item
		 */
		this.getOriginal().setItemInHand(InteractionHand.MAIN_HAND, offhandValid ? offhandItemStack : ItemStack.EMPTY);
		this.getOriginal().setItemInHand(InteractionHand.OFF_HAND, mainhandItemStack);
		
		/**
		 * Swap item's attributes before {@link LivingEntity#setItemInHand} called
		 */
		AttributeInstance damageAttributeInstance = this.original.getAttribute(Attributes.ATTACK_DAMAGE);
		mainhandAttributes.forEach(damageAttributeInstance::removeModifier);
		offhandAttributes.forEach(damageAttributeInstance::addTransientModifier);
	}
	
	/**
	 * Set mainhand item's attribute modifiers
	 */
	protected void recoverMainhandDamage(InteractionHand hand, ItemStack mainhandItemStack, ItemStack offhandItemStack, Collection<AttributeModifier> mainhandAttributes, Collection<AttributeModifier> offhandAttributes) {
		if (hand == InteractionHand.MAIN_HAND) {
			return;
		}
		
		this.getOriginal().setItemInHand(InteractionHand.MAIN_HAND, mainhandItemStack);
		this.getOriginal().setItemInHand(InteractionHand.OFF_HAND, offhandItemStack);
		
		AttributeInstance damageAttributeInstance = this.original.getAttribute(Attributes.ATTACK_DAMAGE);
		offhandAttributes.forEach(damageAttributeInstance::removeModifier);
		mainhandAttributes.forEach(damageAttributeInstance::addTransientModifier);
	}
	
	public void setLastAttackResult(AttackResult attackResult) {
		this.lastAttackResultType = attackResult.resultType;
		this.lastDealDamage = attackResult.damage;
	}

	public void setLastAttackEntity(Entity tryHurtEntity) {
		this.lastTryHurtEntity = tryHurtEntity;
	}

	protected boolean checkLastAttackSuccess(Entity target) {
		boolean success = target.is(this.lastTryHurtEntity);
		this.lastTryHurtEntity = null;
		
		if (success && !this.isLastAttackSuccess) {
			this.setLastAttackSuccess(true);
		}
		
		return success;
	}

	public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
		return this.checkLastAttackSuccess(target) ? new AttackResult(this.lastAttackResultType, this.lastDealDamage) : AttackResult.missed(0.0F);
	}
	
	public float getModifiedBaseDamage(float baseDamage) {
		return baseDamage;
	}
	
	public boolean onDrop(LivingDropsEvent event) {
		return false;
	}
	
	@Override
	public float getStunShield() {
		return this.original.getEntityData().get(STUN_SHIELD).floatValue();
	}
	
	@Override
	public void setStunShield(float value) {
		value = Math.max(value, 0);
		value = Math.min(value, this.getMaxStunShield());
		this.original.getEntityData().set(STUN_SHIELD, value);
	}
	
	public float getMaxStunShield() {
		return this.original.getEntityData().get(MAX_STUN_SHIELD).floatValue();
	}
	
	public void setMaxStunShield(float value) {
		value = Math.max(value, 0);
		this.original.getEntityData().set(MAX_STUN_SHIELD, value);
	}
	
	public int getExecutionResistance() {
		return this.original.getEntityData().get(EXECUTION_RESISTANCE).intValue();
	}
	
	public void setExecutionResistance(int value) {
		int maxExecutionResistance = (int)this.original.getAttributeValue(EpicFightAttributes.EXECUTION_RESISTANCE.get());
		value = Math.min(maxExecutionResistance, value);
		this.original.getEntityData().set(EXECUTION_RESISTANCE, value);
	}
	
	@Override
	public float getWeight() {
		return (float)this.original.getAttributeValue(EpicFightAttributes.WEIGHT.get());
	}
	
	public void rotateTo(float degree, float limit, boolean syncPrevRot) {
		LivingEntity entity = this.getOriginal();
		float yRot = Mth.wrapDegrees(entity.getYRot());
		float amount = Mth.clamp(Mth.wrapDegrees(degree - yRot), -limit, limit);
        float f1 = yRot + amount;
        
		if (syncPrevRot) {
			entity.yRotO = f1;
			entity.yHeadRotO = f1;
			entity.yBodyRotO = f1;
		}
		
		entity.setYRot(f1);
		entity.yHeadRot = f1;
		entity.yBodyRot = f1;
	}
	
	public void rotateTo(Entity target, float limit, boolean syncPrevRot) {
		Vec3 playerPosition = this.original.position();
		Vec3 targetPosition = target.position();
		float yaw = (float)MathUtils.getYRotOfVector(targetPosition.subtract(playerPosition));
    	this.rotateTo(yaw, limit, syncPrevRot);
	}
	
	public LivingEntity getTarget() {
		return this.original.getLastHurtMob();
	}
	
	public float getAttackDirectionPitch() {
		float partialTicks = EpicFightSharedConstants.isPhysicalClient() ? Minecraft.getInstance().getFrameTime() : 1.0F;
		float pitch = -this.getOriginal().getViewXRot(partialTicks);
		float correct = (pitch > 0) ? 0.03333F * (float)Math.pow(pitch, 2) : -0.03333F * (float)Math.pow(pitch, 2);
		
		return Mth.clamp(correct, -30.0F, 30.0F);
	}
	
	public float getCameraXRot() {
		return Mth.wrapDegrees(this.original.getXRot());
	}
	
	public float getCameraYRot() {
		return Mth.wrapDegrees(this.original.getYRot());
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		float yRotO;
		float yRot;
		float scale = this.original.isBaby() ? 0.5F : 1.0F;
		
		if (this.original.getVehicle() instanceof LivingEntity ridingEntity) {
			yRotO = ridingEntity.yBodyRotO;
			yRot = ridingEntity.yBodyRot;
		} else {
			yRotO = this.isLogicalClient() ? this.original.yBodyRotO : this.original.getYRot();
			yRot = this.isLogicalClient() ? this.original.yBodyRot : this.original.getYRot();
			
		}
		
		return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, yRotO, yRot, partialTicks, scale, scale, scale);
	}
	
	/**
	 * Play an animation after the current animation is finished
	 * @param animation
	 */
	public void reserveAnimation(AssetAccessor<? extends StaticAnimation> animation) {
		if (this.isLogicalClient()) {
			this.animator.reserveAnimation(animation);
		} else {
			this.handleAnimationPacket(AnimatorControlPacket.Action.RESERVE, animation, 0.0F, SPAnimatorControl::new);
		}
	}
	
	/**
	 * Play an animation after the current animation is finished
	 * @param animation
	 * @param packetProvider
	 */
	public void reserveAnimation(AssetAccessor<? extends StaticAnimation> animation, ServerAnimationPacketProvider packetProvider) {
		this.handleAnimationPacket(AnimatorControlPacket.Action.RESERVE, animation, 0.0F, packetProvider);
	}
	
	/**
	 * Play an animation without convert time
	 * @param animation
	 */
	public void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> animation) {
		if (this.isLogicalClient()) {
			this.animator.playAnimationInstantly(animation);
		} else {
			this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY_INSTANTLY, animation, 0.0F, SPAnimatorControl::new);
		}
	}
	
	/**
	 * Play an animation without convert time
	 * @param animation
	 * @param packetProvider
	 */
	public void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> animation, ServerAnimationPacketProvider packetProvider) {
		this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY_INSTANTLY, animation, 0.0F, packetProvider);
	}
	
	/**
	 * Play an animation
	 * @param animation
	 * @param transitionTimeModifier
	 */
	public void playAnimation(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		this.animator.playAnimation(animation, transitionTimeModifier);
	}
	
	/**
	 * Stop playing an animation
	 * @param animation
	 * @param transitionTimeModifier
	 */
	public void stopPlaying(AssetAccessor<? extends StaticAnimation> animation) {
		if (this.isLogicalClient()) {
			this.animator.stopPlaying(animation);
		} else {
			this.handleAnimationPacket(AnimatorControlPacket.Action.STOP, animation, -1.0F, SPAnimatorControl::new);
		}
	}
	
	/**
	 * Play an animation with custom packet
	 * @param animation
	 * @param transitionTimeModifier
	 * @param packetProvider
	 */
	public void playAnimation(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, ServerAnimationPacketProvider packetProvider) {
		this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, packetProvider);
	}
	
	/**
	 * Play an animation ensuring synchronization between client-server
	 * Plays animation when getting response from server if it called in client side.
	 * Do not call this in client side for non-player entities.
	 * 
	 * @param animation
	 * @param transitionTimeModifier
	 */
	public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		if (!this.isLogicalClient()) {
			this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, SPAnimatorControl::new);
		}
	}
	
	/**
	 * Play an animation ensuring synchronization between client-server
	 * Plays animation when getting response from server if it called in client side.
	 * Do not call this in client side for non-player entities.
	 * 
	 * @param animation
	 * @param transitionTimeModifier
	 */
	public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, ServerAnimationPacketProvider packetProvider) {
		this.handleAnimationPacket(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, packetProvider);
	}
	
	/**
	 * Play an animation only in client side, including all clients tracking this entity
	 * @param animation
	 * @param convertTimeModifier
	 */
	public void playAnimationInClientSide(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		if (this.isLogicalClient()) {
			this.animator.playAnimation(animation, transitionTimeModifier);
		} else {
			this.sendToAllPlayerTrackingMe(new SPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, this.original.getId(), transitionTimeModifier, false));
		}
	}
	
	/**
	 * Play a shooting animation to end aim pose
	 * Synchronized if the method is called in the server
	 */
	public void playShootingAnimation() {
		if (this.isLogicalClient()) {
			this.animator.playShootingAnimation();
		} else {
			this.sendToAllPlayerTrackingMe(new SPAnimatorControl(AnimatorControlPacket.Action.SHOT, -1, this.getOriginal().getId(), 0.0F, false));
		}
	}
	
	/**
	 * Play an animation with custom packet
	 * @param animation
	 * @param transitionTimeModifier
	 * @param packetProvider
	 */
	private void handleAnimationPacket(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, ServerAnimationPacketProvider packetProvider) {
		if (this.isLogicalClient()) {
			throw new IllegalStateException("Cannot send animation play packet in client side.");
		}
		
		switch (action) {
		case PLAY -> {
			this.animator.playAnimation(animation, transitionTimeModifier);
		}
		case PLAY_INSTANTLY -> {
			this.animator.playAnimationInstantly(animation);
		}
		case STOP -> {
			this.animator.stopPlaying(animation);
		}
		case RESERVE -> {
			this.animator.reserveAnimation(animation);
		}
		case SHOT -> {
			this.animator.playShootingAnimation();
		}
		default -> {
			throw new UnsupportedOperationException("Only PLAY, PLAY_INSTANTLY, STOP and RESERVE are allowed");
		}
		}
		
		this.sendToAllPlayerTrackingMe(packetProvider.get(action, animation, transitionTimeModifier, this));
	}
	
	/**
	 * Pause an animator until it receives a proper order
	 * @param action SOFT_PAUSE: resume when next animation plays
	 * 				 HARD_PAUSE: resume when hard pause is set false
	 * @param pause
	 **/
	public void pauseAnimator(AnimatorControlPacket.Action action, boolean pause) {
		switch (action) {
		case SOFT_PAUSE -> {
			this.animator.setSoftPause(pause);
		}
		case HARD_PAUSE -> {
			this.animator.setHardPause(pause);
		}
		default -> {
			throw new UnsupportedOperationException("Only SOFT_PAUSE and HARD_PAUSE are allowed");
		}
		}
		
		if (!this.isLogicalClient()) {
			this.sendToAllPlayerTrackingMe(new SPAnimatorControl(action, -1, this.original.getId(), 0.0F, pause));
		}
	}
	
	public void sendToAllPlayerTrackingMe(Object packet) {
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(packet, this.original);
	}
	
	@FunctionalInterface
	public interface ServerAnimationPacketProvider {
		SPAnimatorControl get(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float convertTimeModifier, LivingEntityPatch<?> entitypatch);
	}
	
	public void resetSize(EntityDimensions size) {
		EntityDimensions entitysize = this.original.dimensions;
		EntityDimensions entitysize1 = size;
		this.original.dimensions = entitysize1;
		
	    if (entitysize1.width < entitysize.width) {
	    	double d0 = (double)entitysize1.width / 2.0D;
	    	this.original.setBoundingBox(
	    		new AABB(
	    			  this.original.getX() - d0
	    			, this.original.getY()
	    			, this.original.getZ() - d0
	    			, this.original.getX() + d0
	    			, this.original.getY() + (double)entitysize1.height
	    			, this.original.getZ() + d0
	    		)
	    	);
	    } else {
	    	AABB axisalignedbb = this.original.getBoundingBox();
	    	this.original.setBoundingBox(
	    		new AABB(
	    			  axisalignedbb.minX
	    			, axisalignedbb.minY
	    			, axisalignedbb.minZ
	    			, axisalignedbb.minX + (double)entitysize1.width
	    			, axisalignedbb.minY + (double)entitysize1.height
	    			, axisalignedbb.minZ + (double)entitysize1.width
	    		)
	    	);
	    	
	    	if (entitysize1.width > entitysize.width && !this.original.level().isClientSide()) {
	    		float f = entitysize.width - entitysize1.width;
	        	this.original.move(MoverType.SELF, new Vec3(f, 0.0D, f));
	    	}
	    }
    }
	
	@Override
	public boolean applyStun(StunType stunType, float stunTime) {
		this.original.xxa = 0.0F;
		this.original.yya = 0.0F;
		this.original.zza = 0.0F;
		this.original.setDeltaMovement(0.0D, 0.0D, 0.0D);
		this.cancelKnockback = true;
		
		AssetAccessor<? extends StaticAnimation> hitAnimation = this.getHitAnimation(stunType);
		
		if (hitAnimation != null) {
			this.playAnimationSynchronized(hitAnimation, stunType.hasFixedStunTime() ? 0.0F : stunTime);
			return true;
		}
		
		return false;
	}
	
	public void beginAction(ActionAnimation animation) {
	}
	
	public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
	}
	
	public void updateArmor(CapabilityItem fromCap, CapabilityItem toCap, EquipmentSlot slotType) {
	}
	
	/**
	 * Fired when my attack is blocked
	 * @param damageSource
	 * @param blocker
	 */
	public void onAttackBlocked(DamageSource damageSource, LivingEntityPatch<?> blocker) {
	}
	
	public void onStrike(AttackAnimation animation, InteractionHand hand) {
		this.getAdvancedHoldingItemCapability(hand).onStrike(this, animation);
	}
	
	public void onMount(boolean isMountOrDismount, Entity ridingEntity) {
	}
	
	public void notifyGrapplingWarning() {
	}
	
	public void onDodgeSuccess(DamageSource damageSource) {
	}
	
	public void countHurtTime(float damageTaken) {
		this.original.lastHurt = damageTaken;
		this.original.invulnerableTime = 20;
		this.original.hurtDuration = 10;
		this.original.hurtTime = this.original.hurtDuration;
	}
	
	@Override
	public boolean isStunned() {
		return this.getEntityState().hurt();
	}
	
	@SuppressWarnings("unchecked")
	public <A extends Animator> A getAnimator() {
		return (A) this.animator;
	}
	
	@OnlyIn(Dist.CLIENT)
	public ClientAnimator getClientAnimator() {
		return this.getAnimator();
	}
	
	public ServerAnimator getServerAnimator() {
		return this.getAnimator();
	}
	
	public abstract AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType);
	public void aboutToDeath() {}
	
	public SoundEvent getWeaponHitSound(InteractionHand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getHitSound();
	}

	public SoundEvent getSwingSound(InteractionHand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getSmashingSound();
	}
	
	public HitParticleType getWeaponHitParticle(InteractionHand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getHitParticle();
	}

	public Collider getColliderMatching(InteractionHand hand) {
		return this.getAdvancedHoldingItemCapability(hand).getWeaponCollider();
	}

	public int getMaxStrikes(InteractionHand hand) {
		return (int) (hand == InteractionHand.MAIN_HAND ? this.original.getAttributeValue(EpicFightAttributes.MAX_STRIKES.get()) : 
			this.isOffhandItemValid() ? this.original.getAttributeValue(EpicFightAttributes.OFFHAND_MAX_STRIKES.get()) : this.original.getAttribute(EpicFightAttributes.MAX_STRIKES.get()).getBaseValue());
	}
	
	public float getArmorNegation(InteractionHand hand) {
		return (float) (hand == InteractionHand.MAIN_HAND ? this.original.getAttributeValue(EpicFightAttributes.ARMOR_NEGATION.get()) : 
			this.isOffhandItemValid() ? this.original.getAttributeValue(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get()) : this.original.getAttribute(EpicFightAttributes.ARMOR_NEGATION.get()).getBaseValue());
	}
	
	public float getImpact(InteractionHand hand) {
		float impact;
		int i = 0;
		
		if (hand == InteractionHand.MAIN_HAND) {
			impact = (float)this.original.getAttributeValue(EpicFightAttributes.IMPACT.get());
			i = this.getOriginal().getMainHandItem().getEnchantmentLevel(Enchantments.KNOCKBACK);
		} else {
			if (this.isOffhandItemValid()) {
				impact = (float)this.original.getAttributeValue(EpicFightAttributes.OFFHAND_IMPACT.get());
				i = this.getOriginal().getOffhandItem().getEnchantmentLevel(Enchantments.KNOCKBACK);
			} else {
				impact = (float)this.original.getAttribute(EpicFightAttributes.IMPACT.get()).getBaseValue();
			}
		}
		
		return impact * (1.0F + i * 0.12F);
	}
	
	public ItemStack getValidItemInHand(InteractionHand hand) {
		if (hand == InteractionHand.MAIN_HAND) {
			return this.original.getItemInHand(hand);
		} else {
			return this.isOffhandItemValid() ? this.original.getItemInHand(hand) : ItemStack.EMPTY;
		}
	}
	
	public boolean isOffhandItemValid() {
		return this.getHoldingItemCapability(InteractionHand.MAIN_HAND).checkOffhandValid(this);
	}
	
	public Joint getParentJointOfHand(InteractionHand hand) {
		return this.parentJointOfHands.getOrDefault(hand, this.armature.rootJoint);
	}
	
	public void setParentJointOfHand(InteractionHand hand, Joint joint) {
		this.parentJointOfHands.put(hand, joint);
	}
	
	public boolean isTargetInvulnerable(Entity target) {
		if (!target.isPickable() || target.isSpectator()) {
			return true;
		}
		
		if (this.original.getRootVehicle() == target.getRootVehicle() && !target.canRiderInteract()) {
			return true;
		}
		
		return this.original.isAlliedTo(target) && this.original.getTeam() != null && !this.original.getTeam().isAllowFriendlyFire();
	}
	
	public boolean canPush(Entity entity) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
		
		if (entitypatch != null) {
			EntityState state = entitypatch.getEntityState();
			
			if (state.inaction()) {
				return false;
			}
		}
		
		EntityState thisState = this.getEntityState();
		
		return !thisState.inaction() && !entity.is(this.grapplingTarget);
	}
	
	public LivingEntity getGrapplingTarget() {
		return this.grapplingTarget;
	}
	
	public void setGrapplingTarget(LivingEntity grapplingTarget) {
		this.grapplingTarget = grapplingTarget;
	}

	public Vec3 getLastAttackPosition() {
		return this.lastAttackPosition;
	}
	
	public void setLastAttackPosition() {
		this.lastAttackPosition = this.original.position();
	}
	
	public void setAirborneState(boolean airborne) {
		this.original.getEntityData().set(AIRBORNE, airborne);
	}
	
	public boolean isAirborneState() {
		return this.original.getEntityData().get(AIRBORNE);
	}
	
	public void setLastAttackSuccess(boolean setter) {
		this.isLastAttackSuccess = setter;
	}
	
	public boolean isLastAttackSuccess() {
		return this.isLastAttackSuccess;
	}
	
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		return !this.isLogicalClient();
	}
	
	public boolean isFirstPerson() {
		return false;
	}
	
	@Override
	public boolean overrideRender() {
		return true;
	}
	
	public boolean shouldBlockMoving() {
		return false;
	}
	
	/**
	 * Returns a value that the entity can trace a target in rotation by a tick
	 * @return
	 */
	public float getYRotLimit() {
		return 20.0F;
	}
	
	public double getXOld() {
		return this.original.xOld;
	}
	
	public double getYOld() {
		return this.original.yOld;
	}
	
	public double getZOld() {
		return this.original.zOld;
	}
	
	/**
	 * Use this instead of {@link Entity#getYRot()} to get the y rotation especiall player's turning is locked
	 * @return
	 */
	public float getYRot() {
		return this.original.getYRot();
	}
	
	public float getYRotO() {
		return this.original.yRotO;
	}
	
	public void setYRot(float yRot) {
		this.original.setYRot(yRot);
		
		if (this.isLogicalClient()) {
			this.original.yBodyRot = yRot;
			this.original.yHeadRot = yRot;
			this.original.lerpYRot = yRot;
		}
	}
	
	public void setYRotO(float yRot) {
		this.original.yRotO = yRot;
		
		if (this.isLogicalClient()) {
			this.original.yBodyRotO = yRot;
			this.original.yHeadRotO = yRot;
		}
	}
	
	@Override
	public EntityState getEntityState() {
		return this.state;
	}
	
	public InteractionHand getAttackingHand() {
		Pair<AnimationPlayer, AttackAnimation> layerInfo = this.getAnimator().findFor(AttackAnimation.class);
		
		if (layerInfo != null) {
			return layerInfo.getSecond().getPhaseByTime(layerInfo.getFirst().getElapsedTime()).hand;
		}		
		return null;
	}
	
	public Style getOldStyle() {
		return this.oStyle;
	}
	
	public LivingMotion getCurrentLivingMotion() {
		return this.currentLivingMotion;
	}
	
	public List<LivingEntity> getCurrenltyAttackedEntities() {
		return this.getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.HIT_ENTITIES);
	}

	public List<LivingEntity> getCurrenltyHurtEntities() {
		return this.getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.HURT_ENTITIES);
	}

	public void removeHurtEntities() {
		this.getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.HIT_ENTITIES).clear();
		this.getAnimator().getVariables().getOrDefaultSharedVariable(AttackAnimation.HURT_ENTITIES).clear();
	}
	
	public abstract Faction getFaction();
	
	@OnlyIn(Dist.CLIENT)
	public boolean flashTargetIndicator(LocalPlayerPatch playerpatch) {
		TargetIndicatorCheckEvent event = new TargetIndicatorCheckEvent(playerpatch, this);
		playerpatch.getEventListener().triggerEvents(EventType.TARGET_INDICATOR_ALERT_CHECK_EVENT, event);
		
		return event.isCanceled();
	}
}