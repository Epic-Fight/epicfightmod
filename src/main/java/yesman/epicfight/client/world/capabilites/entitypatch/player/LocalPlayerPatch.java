package yesman.epicfight.client.world.capabilites.entitypatch.player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.DirectStaticAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings.ViewLimit;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPAnimatorControl;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPModifyEntityModelYRot;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.network.client.CPSetStamina;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.ZoomInType;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

@OnlyIn(Dist.CLIENT)
public class LocalPlayerPatch extends AbstractClientPlayerPatch<LocalPlayer> {
	private static final UUID ACTION_EVENT_UUID = UUID.fromString("d1a1e102-1621-11ed-861d-0242ac120002");
	private Minecraft minecraft;
	private LivingEntity rayTarget;
	private boolean targetLockedOn;
	private float staminaO;
	private int prevChargingAmount;
	
	private float lockOnXRot;
	private float lockOnXRotO;
	private float lockOnYRot;
	private float lockOnYRotO;
	
	private float fpvXRotO;
	private float fpvXRot;
	private float fpvYRotO;
	private float fpvYRot;
	private int fpvLerpTick;
	
	private FirstPersonLayer firstPersonLayer = new FirstPersonLayer();
	private AnimationSubFileReader.PovSettings povSettings;
	
	@Override
	public void onConstructed(LocalPlayer entity) {
		super.onConstructed(entity);
		this.minecraft = Minecraft.getInstance();
	}
	
	@Override
	public void onJoinWorld(LocalPlayer player, EntityJoinLevelEvent event) {
		super.onJoinWorld(player, event);
		
		this.eventListeners.addEventListener(EventType.ACTION_EVENT_CLIENT, ACTION_EVENT_UUID, (playerEvent) -> {
			ClientEngine.getInstance().controlEngine.unlockHotkeys();
		});
	}
	
	public void onRespawnLocalPlayer(ClientPlayerNetworkEvent.Clone event) {
		this.onJoinWorld(event.getNewPlayer(), new EntityJoinLevelEvent(event.getNewPlayer(), event.getNewPlayer().level()));
	}
	
	@Override
	public void tick(LivingEvent.LivingTickEvent event) {
		this.staminaO = this.getStamina();
		
		if (this.isHoldingAny() && this.getHoldingSkill() instanceof ChargeableSkill) {
			this.prevChargingAmount = this.getChargingAmount();
		} else {
			this.prevChargingAmount = 0;
		}
		
		super.tick(event);
	}
	
	private EntityHitResult pickEntity() {
		double distance = this.original.getBlockReach() * 2.0D;
		double entityReach = this.original.getEntityReach() * 2.0D;
		double pickRange = Math.max(distance, entityReach);
		Vec3 vec3 = this.original.getEyePosition(1.0F);
		Vec3 vec31 = this.original.getViewVector(1.0F);
		Vec3 vec32 = vec3.add(vec31.x * pickRange, vec31.y * pickRange, vec31.z * pickRange);
		BlockHitResult blockHitResulst = this.getOriginal().level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.original));
		
		if (blockHitResulst.getType() != HitResult.Type.MISS) {
			pickRange = blockHitResulst.getLocation().distanceToSqr(vec3);
		}
		
        AABB aabb = this.original.getBoundingBox().expandTowards(vec31.scale(distance)).inflate(1.0D, 1.0D, 1.0D);
        
        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(this.original, vec3, vec32, aabb, (p_234237_) -> {
           return !p_234237_.isSpectator() && p_234237_.isPickable();
        }, pickRange);
        
        return entityhitresult;
	}
	
	@Override
	public void clientTick(LivingEvent.LivingTickEvent event) {
		this.staminaO = this.getStamina();
		
		super.clientTick(event);
		
		// Handle targeting entity
		EntityHitResult cameraHitResult = this.pickEntity();
		
		if (cameraHitResult != null) {
			Entity hit = cameraHitResult.getEntity();
			
			if (hit != this.rayTarget && hit != this.original) {
				if (hit instanceof LivingEntity livingentity) {
					if (!(hit instanceof ArmorStand) && !this.targetLockedOn) {
						this.rayTarget = livingentity;
					}
				} else if (hit instanceof PartEntity<?> partEntity) {
					Entity parent = partEntity.getParent();
					
					if (parent instanceof LivingEntity parentLivingEntity && !this.targetLockedOn) {
						this.rayTarget = parentLivingEntity;
					}
				} else {
					this.rayTarget = null;
				}
				
				if (this.rayTarget != null) {
					EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(this.getTarget().getId()));
				}
			}
		}
		
		this.lockOnXRotO = this.lockOnXRot;
		this.lockOnYRotO = this.lockOnYRot;
		
		// Handle camera lock-on
		if (this.rayTarget != null) {
			if (this.targetLockedOn && !this.isLerpingFpv()) {
				Vec3 playerPosition = this.original.getEyePosition();
				Vec3 targetPosition = this.rayTarget.getEyePosition();
				Vec3 toTarget = targetPosition.subtract(playerPosition);
				float yaw = (float)MathUtils.getYRotOfVector(toTarget);
				float pitch = (float)MathUtils.getXRotOfVector(toTarget);
				CameraType cameraType = this.minecraft.options.getCameraType();
				
				float lockOnXRotDst = pitch + (cameraType.isFirstPerson() ? 0.0F : 30.0F);
				lockOnXRotDst = Mth.clamp(lockOnXRotDst, 0.0F, 60.0F);
				
				if (cameraType.isMirrored()) {
					lockOnXRotDst = -lockOnXRotDst;
				}
				
				float lockOnYRotDst = yaw + (cameraType.isMirrored() ? 180.0F : 0.0F);
				float xDiff = Mth.wrapDegrees(lockOnXRotDst - this.lockOnXRotO);
				float yDiff = Mth.wrapDegrees(lockOnYRotDst - this.lockOnYRotO);
				float xLerp = Mth.clamp(xDiff * 0.4F, -30.0F, 30.0F);
				float yLerp = Mth.clamp(yDiff * 0.4F, -30.0F, 30.0F);
				this.lockOnXRot = this.lockOnXRotO + xLerp;
				this.lockOnYRot = this.lockOnYRotO + yLerp;
				
				if (!this.getEntityState().turningLocked() || this.getEntityState().lockonRotate()) {
					Vec3 playerEye = this.original.getEyePosition();
					Vec3 targetEye = this.rayTarget.getEyePosition();
					double eyeYDiff = Math.abs(playerEye.y - targetEye.y);
					double dist = playerEye.distanceTo(targetEye);
					double xDegree = Math.tan(eyeYDiff / dist) * (180.0F / Math.PI);
					xDegree = Mth.clamp(xDegree, -60.0F, 60.0F);
					
					this.original.setXRot((float)xDegree);
					this.original.setYRot(lockOnYRotDst);
				}
			} else {
				this.lockOnXRot = this.original.getXRot();
				this.lockOnYRot = this.original.getYRot();
			}
			
			if (this.rayTarget.isRemoved() || this.rayTarget.isInvisibleTo(this.original) || this.getOriginal().distanceToSqr(this.rayTarget) > 400.0D || (this.getAngleTo(this.rayTarget) > 80.0D && !this.targetLockedOn)) {
				if (this.targetLockedOn) {
					this.original.setXRot(this.lockOnXRot);
					this.original.setYRot(this.lockOnYRot);
					this.targetLockedOn = false;
				}
				
				this.rayTarget = null;
				EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
			}
		} else {
			this.lockOnXRot = this.original.getXRot();
			this.lockOnYRot = this.original.getYRot();
			this.targetLockedOn = false;
		}
		
		// Handle camera zoom in/out
		CapabilityItem mainhandItemCap = this.getAdvancedHoldingItemCapability(InteractionHand.MAIN_HAND);
		CapabilityItem offhandItemCap = this.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
		CapabilityItem.ZoomInType rangeWeaponZoomInType =
			mainhandItemCap.isEmpty() || mainhandItemCap.getZoomInType() == ZoomInType.NONE
				? offhandItemCap.getZoomInType() : mainhandItemCap.getZoomInType();
		
		switch (rangeWeaponZoomInType) {
		case ALWAYS -> {
			ClientEngine.getInstance().renderEngine.zoomIn();
		}
		case USE_TICK -> {
			if (this.original.getUseItemRemainingTicks() > 0) {
				ClientEngine.getInstance().renderEngine.zoomIn();
			} else {
				ClientEngine.getInstance().renderEngine.zoomOut(40);
			}
		}
		case AIMING -> {
			if (this.getClientAnimator().isAiming()) {
				ClientEngine.getInstance().renderEngine.zoomIn();
			} else {
				ClientEngine.getInstance().renderEngine.zoomOut(40);
			}
		}
		case CUSTOM -> {} //Zoom manually handled
		default -> {
			ClientEngine.getInstance().renderEngine.zoomOut(0);
		}
		}
		
		// Handle first person animation
		final AssetAccessor<? extends StaticAnimation> currentPlaying = this.firstPersonLayer.animationPlayer.getRealAnimation();
		
		boolean noPovAnimation = this.getClientAnimator().iterVisibleLayersUntilFalse(layer -> {
			if (layer.isOff()) {
				return true;
			}
			
			Optional<DirectStaticAnimation> optPovAnimation = layer.animationPlayer.getRealAnimation().get().getProperty(ClientAnimationProperties.POV_ANIMATION);
			Optional<PovSettings> optPovSettings = layer.animationPlayer.getRealAnimation().get().getProperty(ClientAnimationProperties.POV_SETTINGS);
			
			optPovAnimation.ifPresent(povAnimation -> {
				if (!povAnimation.equals(currentPlaying.get())) {
					this.firstPersonLayer.playAnimation(povAnimation, layer.animationPlayer.getRealAnimation(), this, 0.0F);
					this.povSettings = optPovSettings.get();
				}
			});
			
			return !optPovAnimation.isPresent();
		});
		
		if (noPovAnimation && !currentPlaying.equals(Animations.EMPTY_ANIMATION)) {
			this.firstPersonLayer.off();
		}
		
		this.firstPersonLayer.update(this);
		
		if (this.firstPersonLayer.animationPlayer.getAnimation().equals(Animations.EMPTY_ANIMATION)) {
			this.povSettings = null;
		}
		
		boolean isLerping = this.isLerpingFpv();
		
		if (isLerping) this.fpvLerpTick--;
		if (isLerping && !this.isLerpingFpv()) {
			this.original.setXRot(this.fpvXRot);
			this.original.setYRot(this.fpvYRot);
		}
	}
	
	@Override
	public boolean overrideRender() {
		// Disable rendering the player when animated first person model disabled
		if (this.original.is(this.minecraft.player)) {
			if (this.minecraft.options.getCameraType().isFirstPerson() && !ClientConfig.enableAnimatedFirstPersonModel) {
				return false;
			}
		}
		
		return super.overrideRender();
	}
	
	@Override
	public LivingEntity getTarget() {
		return this.rayTarget;
	}
	
	@Override
	public void toVanillaMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.VANILLA) {
			ClientEngine.getInstance().renderEngine.downSlideSkillUI();
			
			if (ClientConfig.authSwitchCamera) {
				this.minecraft.options.setCameraType(CameraType.FIRST_PERSON);
			}
			
			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.VANILLA));
			}
		}
		
		super.toVanillaMode(synchronize);
	}
	
	@Override
	public void toEpicFightMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.EPICFIGHT) {
			ClientEngine.getInstance().renderEngine.upSlideSkillUI();
			
			if (ClientConfig.authSwitchCamera) {
				this.minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			}
			
			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.EPICFIGHT));
			}
		}
		
		super.toEpicFightMode(synchronize);
	}
	
	@Override
	public boolean isFirstPerson() {
		return this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
	}
	
	@Override
	public boolean shouldBlockMoving() {
		return ControlEngine.isKeyDown(this.minecraft.options.keyDown);
	}
	
	@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		if (!this.isLogicalClient()) {
			return false;
		}
		
		return actionAnimation.shouldPlayerMove(this);
	}
	
	public float getStaminaO() {
		return this.staminaO;
	}
	
	public int getPrevChargingAmount() {
		return this.prevChargingAmount;
	}

	public float getLerpedLockOnX(double partial) {
		return Mth.rotLerp((float)partial, this.lockOnXRotO, this.lockOnXRot);
	}
	
	public float getLerpedLockOnY(double partial) {
		return Mth.rotLerp((float)partial, this.lockOnYRotO, this.lockOnYRot);
	}
	
	public boolean isTargetLockedOn() {
		return this.targetLockedOn;
	}
	
	public void setLockOn(boolean targetLockedOn) {
		if (this.targetLockedOn && this.rayTarget != null) {
			this.original.setXRot(this.lockOnXRot);
			this.original.setYRot(this.lockOnYRot);
		}
		
		this.targetLockedOn = targetLockedOn;
	}
	
	public void toggleLockOn() {
		this.setLockOn(!this.targetLockedOn);
	}
	
	public FirstPersonLayer getFirstPersonLayer() {
		return this.firstPersonLayer;
	}
	
	public AnimationSubFileReader.PovSettings getPovSettings() {
		return this.povSettings;
	}
	
	public boolean hasCameraAnimation() {
		return this.povSettings != null && this.povSettings.cameraTransform() != null;
	}
	
	@Override
	public void setStamina(float value) {
		EpicFightNetworkManager.sendToServer(new CPSetStamina(value, true));
	}
	
	@Override
	public void onDeath(LivingDeathEvent event) {
		super.onDeath(event);
		
		this.original.setXRot(this.lockOnXRot);
		this.original.setYRot(this.lockOnYRot);
	}
	
	@Override
	public void setModelYRot(float amount, boolean sendPacket) {
		super.setModelYRot(amount, sendPacket);
		
		if (sendPacket) {
			EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot(amount));
		}
	}
	
	public float getModelYRot() {
		return this.modelYRot;
	}
	
	public void setModelYRotInGui(float rotDeg) {
		this.useModelYRot = true;
		this.modelYRot = rotDeg;
	}
	
	public void disableModelYRotInGui(float originalDeg) {
		this.useModelYRot = false;
		this.modelYRot = originalDeg;
	}
	
	public void fixFpvRotation(float xRot, float yRot) {
		this.fpvXRot = Mth.wrapDegrees(xRot);
		this.fpvXRotO = Mth.wrapDegrees(this.original.getXRot());
		this.fpvYRot = Mth.wrapDegrees(yRot);
		this.fpvYRotO = Mth.wrapDegrees(this.original.getYRot());
		this.fpvLerpTick = 5;
	}
	
	public float getLerpedFpvXRot(float partialTicks) {
		float delta = ((this.fpvLerpTick) / 5.0F + (1.0F - partialTicks) * (1.0F / 5.0F));
		return this.isLerpingFpv() ? Mth.rotLerp(delta, this.fpvXRot, this.fpvXRotO) : this.original.getXRot();
	}
	
	public float getLerpedFpvYRot(float partialTicks) {
		float delta = ((this.fpvLerpTick) / 5.0F + (1.0F - partialTicks) * (1.0F / 5.0F));
		return this.isLerpingFpv() ? Mth.rotLerp(delta, this.fpvYRot, this.fpvYRotO) : this.original.getYRot();
	}
	
	public boolean isLerpingFpv() {
		return this.fpvLerpTick > -1;
	}
	
	@Override
	public void disableModelYRot(boolean sendPacket) {
		super.disableModelYRot(sendPacket);
		
		if (sendPacket) {
			EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot());
		}
	}
	
	@Override
	public double checkXTurn(double xRot) {
		if (xRot == 0.0D) {
			return xRot;
		}
		
		if (ClientConfig.enablePovAction && this.minecraft.options.getCameraType().isFirstPerson() && this.isEpicFightMode() && !this.getFirstPersonLayer().isOff()) {
			ViewLimit viewLimit = this.getPovSettings().viewLimit();
			
			if (viewLimit != null) {
				float xRotDest = this.original.getXRot() + (float)xRot * 0.15F;
				
				if (xRotDest <= viewLimit.xRotMin() || xRotDest >= viewLimit.xRotMax()) {
					return 0.0D;
				}
			}
		}
		
		return xRot;
	}
	
	@Override
	public double checkYTurn(double yRot) {
		if (yRot == 0.0D) {
			return yRot;
		}
		
		if (ClientConfig.enablePovAction && this.minecraft.options.getCameraType().isFirstPerson() && this.isEpicFightMode() && !this.getFirstPersonLayer().isOff()) {
			ViewLimit viewLimit = this.getPovSettings().viewLimit();
			
			if (viewLimit != null) {
				float yRotDest = this.original.getYRot() + (float)yRot * 0.15F;
				float yRotClamped = Mth.clamp(yRotDest, this.getYRot() + viewLimit.yRotMin(), this.getYRot() + viewLimit.yRotMax());
				
				if (yRotDest != yRotClamped) {
					return 0.0D;
				}
			}
		}
		
		return yRot;
	}
	
	@Override
	public void beginAction(ActionAnimation animation) {
		if (!this.useModelYRot || animation.getProperty(ActionAnimationProperty.SYNC_CAMERA).orElse(false)) {
			this.modelYRot = this.getOriginal().getYRot();
		}
		
		if (this.targetLockedOn) {
			if (this.rayTarget != null && !this.rayTarget.isRemoved()) {
				Vec3 playerPosition = this.original.position();
				Vec3 targetPosition = this.rayTarget.position();
				Vec3 toTarget = targetPosition.subtract(playerPosition);
				float yaw = (float)MathUtils.getYRotOfVector(toTarget); 
				float pitch = (float)MathUtils.getXRotOfVector(toTarget);
				this.original.setYRot(yaw);
				this.original.setXRot(pitch);
			} else {
				this.original.setYRot(this.lockOnYRot);
				this.original.setXRot(this.lockOnXRot);
			}
		}
	}
	
	/**
	 * Play an animation after the current animation is finished
	 * @param animation
	 */
	@Override
	public void reserveAnimation(AssetAccessor<? extends StaticAnimation> animation) {
		this.animator.reserveAnimation(animation);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.RESERVE, animation, 0.0F, false, false, false));
	}
	
	/**
	 * Play an animation without convert time
	 * @param animation
	 */
	@Override
	public void playAnimationInstantly(AssetAccessor<? extends StaticAnimation> animation) {
		this.animator.playAnimationInstantly(animation);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY_INSTANTLY, animation, 0.0F, false, false, false));
	}
	
	/**
	 * Play a shooting animation to end aim pose
	 * This method doesn't send packet from client to server
	 */
	@Override
	public void playShootingAnimation() {
		this.animator.playShootingAnimation();
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.SHOT, -1, 0.0F, false, true, false));
	}
	
	/**
	 * Stop playing an animation
	 * @param animation
	 * @param transitionTimeModifier
	 */
	@Override
	public void stopPlaying(AssetAccessor<? extends StaticAnimation> animation) {
		this.animator.stopPlaying(animation);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.STOP, animation, -1.0F, false, false, false));
	}
	
	/**
	 * Play an animation ensuring synchronization between client-server
	 * Plays animation when getting response from server if it called in client side.
	 * Do not call this in client side for non-player entities.
	 * 
	 * @param animation
	 * @param transitionTimeModifier
	 */
	@Override
	public void playAnimationSynchronized(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, false, false, true));
	}
	
	/**
	 * Play an animation only in client side, including all clients tracking this entity
	 * @param animation
	 * @param convertTimeModifier
	 */
	@Override
	public void playAnimationInClientSide(AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier) {
		this.animator.playAnimation(animation, transitionTimeModifier);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(AnimatorControlPacket.Action.PLAY, animation, transitionTimeModifier, false, true, false));
	}
	
	/**
	 * Pause an animator until it receives a proper order
	 * @param action SOFT_PAUSE: resume when next animation plays
	 * 				 HARD_PAUSE: resume when hard pause is set false
	 * @param pause
	 **/
	@Override
	public void pauseAnimator(AnimatorControlPacket.Action action, boolean pause) {
		super.pauseAnimator(action, pause);
		EpicFightNetworkManager.sendToServer(new CPAnimatorControl(action, -1, 0.0F, pause, false, false));
	}
	
	@Override
	public void openSkillBook(ItemStack itemstack, InteractionHand hand) {
		if (itemstack.hasTag() && itemstack.getTag().contains("skill")) {
			Minecraft.getInstance().setScreen(new SkillBookScreen(this.original, itemstack, hand));
		}
	}
	
	@Override
	public void resetHolding() {
		if (this.holdingSkill != null) {
			ClientEngine.getInstance().controlEngine.releaseAllServedKeys();
		}
		
		super.resetHolding();
	}
	
	@OnlyIn(Dist.CLIENT)
	public class FirstPersonLayer extends Layer {
		private TransformSheet linkCameraTransform = new TransformSheet(List.of(new Keyframe(0.0F, JointTransform.empty()), new Keyframe(Float.MAX_VALUE, JointTransform.empty())));
		
		public FirstPersonLayer() {
			super(null);
		}
		
		public void playAnimation(AssetAccessor<? extends StaticAnimation> nextFirstPersonAnimation, AssetAccessor<? extends StaticAnimation> originalAnimation, LivingEntityPatch<?> entitypatch, float transitionTimeModifier) {
			Optional<PovSettings> povSettings = originalAnimation.get().getProperty(ClientAnimationProperties.POV_SETTINGS);
			
			boolean hasPrevCameraAnimation = LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null;
			boolean hasNextCameraAnimation = povSettings.isPresent() && povSettings.get().cameraTransform() != null;
			
			// Activate pov animation
			if (hasPrevCameraAnimation || hasNextCameraAnimation) {
				if (hasPrevCameraAnimation) {
					this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
				} else {
					this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(JointTransform.empty());
				}
				
				if (hasNextCameraAnimation) {
					this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(povSettings.get().cameraTransform().getKeyframes()[0].transform());
				} else {
					this.linkCameraTransform.getKeyframes()[1].transform().clearTransform();
				}
				
				this.linkCameraTransform.getKeyframes()[1].setTime(nextFirstPersonAnimation.get().getTransitionTime());
			}
			
			super.playAnimation(nextFirstPersonAnimation, entitypatch, transitionTimeModifier);
		}
		
		public void off() {
			// Off camera animation
			if (LocalPlayerPatch.this.povSettings != null && LocalPlayerPatch.this.povSettings.cameraTransform() != null) {
				this.linkCameraTransform.getKeyframes()[0].transform().copyFrom(LocalPlayerPatch.this.povSettings.cameraTransform().getInterpolatedTransform(this.animationPlayer.getElapsedTime()));
				this.linkCameraTransform.getKeyframes()[1].transform().copyFrom(JointTransform.empty());
				this.linkCameraTransform.getKeyframes()[1].setTime(EpicFightSharedConstants.GENERAL_ANIMATION_TRANSITION_TIME);
			}
			
			super.off(LocalPlayerPatch.this);
		}
		
		@Override
		protected Pose getCurrentPose(LivingEntityPatch<?> entitypatch) {
			return this.animationPlayer.isEmpty() ? super.getCurrentPose(entitypatch) : this.animationPlayer.getCurrentPose(entitypatch, 0.0F);
		}
		
		public TransformSheet getLinkCameraTransform() {
			return this.linkCameraTransform;
		}
	}
}