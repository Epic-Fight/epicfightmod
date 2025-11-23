package yesman.epicfight.client.world.capabilites.entitypatch.player;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DirectStaticAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings.ViewLimit;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPAnimatorControl;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPModifyEntityModelYRot;
import yesman.epicfight.network.client.CPSetStamina;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class LocalPlayerPatch extends AbstractClientPlayerPatch<LocalPlayer> {
	
	private static final UUID ACTION_EVENT_UUID = UUID.fromString("d1a1e102-1621-11ed-861d-0242ac120002");
	private Minecraft minecraft;
	private float staminaO;
	private int prevChargingAmount;
	
	private AnimationSubFileReader.PovSettings povSettings;
	private FirstPersonLayer firstPersonLayer = new FirstPersonLayer();
	
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
	
	@Override
	public void clientTick(LivingEvent.LivingTickEvent event) {
		this.staminaO = this.getStamina();
		
		super.clientTick(event);
		
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
		return EpicFightCameraAPI.getInstance().getFocusingEntity();
	}
	
	@Override
	public void toVanillaMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.VANILLA) {
			ClientEngine.getInstance().renderEngine.downSlideSkillUI();
			
			if (ClientConfig.autoSwitchCamera) {
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
			
			if (ClientConfig.autoSwitchCamera) {
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
		return InputManager.isActionActive(EpicFightInputAction.MOVE_BACKWARD) || InputManager.isActionActive(EpicFightInputAction.SNEAK);
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
				float yCamera = Mth.wrapDegrees(this.original.getYRot());
				float yBody = MathUtils.findNearestRotation(yCamera, this.getYRot());
				float yRotDest = yCamera + (float)yRot * 0.15F;
				float yRotClamped = Mth.clamp(yRotDest, yBody + viewLimit.yRotMin(), yBody + viewLimit.yRotMax());
				
				if (yRotDest != yRotClamped) {
					return 0.0D;
				}
			}
		}
		
		return yRot;
	}
	
	@Override
	public void beginAction(ActionAnimation animation) {
		EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
		
		if (cameraApi.isTPSMode()) {
			if (cameraApi.getFocusingEntity() != null && animation instanceof AttackAnimation) {
				cameraApi.alignPlayerLookToCrosshair(false);
			} else {
				cameraApi.alignPlayerLookToCameraRotation(false);
			}
		}
		
		if (!this.useModelYRot || animation.getProperty(ActionAnimationProperty.SYNC_CAMERA).orElse(false)) {
			this.modelYRot = this.original.getYRot();
		}
		
		if (cameraApi.getFocusingEntity() != null && cameraApi.isLockingOnTarget() && !cameraApi.getFocusingEntity().isRemoved()) {
			Vec3 playerPosition = this.original.position();
			Vec3 targetPosition = cameraApi.getFocusingEntity().position();
			Vec3 toTarget = targetPosition.subtract(playerPosition);
			this.original.setYRot((float)MathUtils.getYRotOfVector(toTarget));
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
	
	@Override
	public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
		super.updateHeldItem(mainHandCap, offHandCap);
		
		if (!ClientConfig.preferenceWork.checkHitResult()) {
			if (ClientConfig.combatPreferredItems.contains(this.original.getMainHandItem().getItem())) {
				this.toEpicFightMode(true); 
			} else if (ClientConfig.miningPreferredItems.contains(this.original.getMainHandItem().getItem())) {
				this.toVanillaMode(true);
			}
		}
	}
	
	/**
	 * Judge the next behavior depending on player's item preference and where he's looking at
	 * @return true if the next action is swing a weapon, false if the next action is breaking a block
	 */
	public boolean canPlayAttackAnimation() {
		if (this.isVanillaMode()) {
			return false;
		}
		
		EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
		
		HitResult hitResult = 
			(EpicFightCameraAPI.getInstance().isTPSMode() && cameraApi.getCrosshairHitResult() != null && cameraApi.getCrosshairHitResult().getLocation().distanceToSqr(this.original.getEyePosition()) < this.original.getBlockReach() * this.original.getBlockReach())
				? cameraApi.getCrosshairHitResult() : this.minecraft.hitResult;
		
		if (hitResult == null) {
			return true;
		}
		
		if (RenderEngine.hitResultEquals(this.minecraft.hitResult, HitResult.Type.ENTITY)) {
			Entity hitEntity = ((EntityHitResult)hitResult).getEntity();
			
			if (!(hitEntity instanceof LivingEntity) && !(hitEntity instanceof PartEntity)) {
				return false;
			}
		}
		
		if (EpicFightCameraAPI.getInstance().isLockingOnTarget()) {
			return true;
		}
		
		if (ClientConfig.preferenceWork.checkHitResult()) {
			if (ClientConfig.combatPreferredItems.contains(this.original.getMainHandItem().getItem())) {
				if (RenderEngine.hitResultEquals(this.minecraft.hitResult, HitResult.Type.BLOCK) && this.minecraft.level != null) {
					BlockPos bp = ((BlockHitResult) this.minecraft.hitResult).getBlockPos();
					BlockState bs = this.minecraft.level.getBlockState(bp);
					return !this.original.getMainHandItem().getItem().canAttackBlock(bs, this.original.level(), bp, this.original) || !this.original.getMainHandItem().isCorrectToolForDrops(bs);
				}
			} else {
				return RenderEngine.hitResultNotEquals(this.minecraft.hitResult, HitResult.Type.BLOCK);
			}
			
			return true;
		} else {
			return this.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT;
		}
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

    /**
     * @deprecated Use {@link EpicFightCameraAPI#isLockingOnTarget()} instead
     */
    @Deprecated(forRemoval = true)
    public boolean isTargetLockedOn() {
        return EpicFightCameraAPI.getInstance().isLockingOnTarget();
    }

    /**
     * @deprecated Use {@link EpicFightCameraAPI#setLockOn(boolean)} instead
     */
    @Deprecated(forRemoval = true)
    public void setLockOn(boolean targetLockedOn) {
        EpicFightCameraAPI.getInstance().setLockOn(targetLockedOn);
    }

    /**
     * @deprecated Use {@link EpicFightCameraAPI#toggleLockOn()} instead
     */
    @Deprecated(forRemoval = true)
    public void toggleLockOn() {
        this.setLockOn(!EpicFightCameraAPI.getInstance().isLockingOnTarget());
    }
}