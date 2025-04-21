package yesman.epicfight.client.world.capabilites.entitypatch.player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
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
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPModifyEntityModelYRot;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.network.client.CPSetStamina;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

@OnlyIn(Dist.CLIENT)
public class LocalPlayerPatch extends AbstractClientPlayerPatch<LocalPlayer> {
	private static final UUID ACTION_EVENT_UUID = UUID.fromString("d1a1e102-1621-11ed-861d-0242ac120002");
	private Minecraft minecraft;
	private LivingEntity rayTarget;
	private boolean targetLockedOn;
	private float prevStamina;
	private int prevChargingAmount;
	
	private float lockOnXRot;
	private float lockOnXRotO;
	private float lockOnYRot;
	private float lockOnYRotO;
	
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
			ClientEngine.getInstance().controllEngine.unlockHotkeys();
		});
	}
	
	public void onRespawnLocalPlayer(ClientPlayerNetworkEvent.Clone event) {
		this.onJoinWorld(event.getNewPlayer(), new EntityJoinLevelEvent(event.getNewPlayer(), event.getNewPlayer().level()));
	}
	
	@Override
	public void tick(LivingEvent.LivingTickEvent event) {
		this.prevStamina = this.getStamina();
		
		if (this.isChargingSkill()) {
			this.prevChargingAmount = this.getChargingSkill().getChargingAmount(this);
		} else {
			this.prevChargingAmount = 0;
		}
		
		super.tick(event);
	}

	@Override
	public void clientTick(LivingEvent.LivingTickEvent event) {
		this.prevStamina = this.getStamina();
		
		super.clientTick(event);
		
		// Handle targeting entity
		HitResult cameraHitResult = this.minecraft.hitResult;
		
		if (cameraHitResult != null && cameraHitResult.getType() == HitResult.Type.ENTITY) {
			Entity hit = ((EntityHitResult)cameraHitResult).getEntity();
			
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
		
		// Handle camera lock - on
		if (this.rayTarget != null) {
			if (this.targetLockedOn) {
				Vec3 playerPosition = this.original.getEyePosition();
				Vec3 targetPosition = this.rayTarget.getEyePosition();
				Vec3 toTarget = targetPosition.subtract(playerPosition);
				float yaw = (float)MathUtils.getYRotOfVector(toTarget);
				float pitch = (float)MathUtils.getXRotOfVector(toTarget);
				CameraType cameraType = this.minecraft.options.getCameraType();
				this.lockOnXRotO = this.lockOnXRot;
				this.lockOnYRotO = this.lockOnYRot;
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
					this.original.setXRot(lockOnXRotDst);
					this.original.setYRot(lockOnYRotDst);
				}
			} else {
				this.lockOnXRot = this.original.getXRot();
				this.lockOnYRot = this.original.getYRot();
				this.lockOnXRotO = this.lockOnXRot;
				this.lockOnYRotO = this.lockOnYRot;
			}
			
			if (this.rayTarget.isRemoved() || this.getOriginal().distanceToSqr(this.rayTarget) > 400.0D || (this.getAngleTo(this.rayTarget) > 100.0D && !this.targetLockedOn)) {
				this.rayTarget = null;
				EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
			}
		} else {
			this.lockOnXRot = this.original.getXRot();
			this.lockOnYRot = this.original.getYRot();
			this.targetLockedOn = false;
		}
		
		// Handle camera zoom in/out
		CapabilityItem itemCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		
		switch (itemCap.getZoomInType()) {
		case ALWAYS:
			ClientEngine.getInstance().renderEngine.zoomIn();
			break;
		case USE_TICK:
			if (this.original.getUseItemRemainingTicks() > 0) {
				ClientEngine.getInstance().renderEngine.zoomIn();
			} else {
				ClientEngine.getInstance().renderEngine.zoomOut(40);
			}
			
			break;
		case AIMING:
			if (this.getClientAnimator().isAiming()) {
				ClientEngine.getInstance().renderEngine.zoomIn();
			} else {
				ClientEngine.getInstance().renderEngine.zoomOut(40);
			}
			
			break;
		case CUSTOM:
			//Zoom manually handled
			break;
		default:
			ClientEngine.getInstance().renderEngine.zoomOut(0);
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
	public void updateHeldItem(CapabilityItem mainHandCap, CapabilityItem offHandCap) {
		super.updateHeldItem(mainHandCap, offHandCap);
		
		if (ClientConfig.battleModeSwitchingItems.contains(this.original.getMainHandItem().getItem())) {
			this.toBattleMode(true);
		} else if (ClientConfig.miningModeSwitchingItems.contains(this.original.getMainHandItem().getItem())) {
			this.toMiningMode(true);
		}
	}
	
	@Override
	public AttackResult tryHurt(DamageSource damageSource, float amount) {
		AttackResult result = super.tryHurt(damageSource, amount);
		
		if (ClientConfig.autoPreparation && result.resultType == AttackResult.ResultType.SUCCESS && !this.isBattleMode()) {
			this.toBattleMode(true);
		}
		
		return result;
	}
	
	@Override
	public LivingEntity getTarget() {
		return this.rayTarget;
	}
	
	@Override
	public void toMiningMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.MINING) {
			ClientEngine.getInstance().renderEngine.downSlideSkillUI();
			
			if (ClientConfig.authSwitchCamera) {
				this.minecraft.options.setCameraType(CameraType.FIRST_PERSON);
			}
			
			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.MINING));
			}
		}
		
		super.toMiningMode(synchronize);
	}
	
	@Override
	public void toBattleMode(boolean synchronize) {
		if (this.playerMode != PlayerMode.BATTLE) {
			ClientEngine.getInstance().renderEngine.upSlideSkillUI();
			
			if (ClientConfig.authSwitchCamera) {
				this.minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
			}
			
			if (synchronize) {
				EpicFightNetworkManager.sendToServer(new CPChangePlayerMode(PlayerMode.BATTLE));
			}
		}
		
		super.toBattleMode(synchronize);
	}
	
	@Override
	public boolean isFirstPerson() {
		return this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
	}
	
	@Override
	public boolean shouldBlockMoving() {
		return ControllEngine.isKeyDown(this.minecraft.options.keyDown);
	}
	
	@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		if (!this.isLogicalClient()) {
			return false;
		}
		
		return actionAnimation.shouldPlayerMove(this);
	}
	
	public float getPrevStamina() {
		return this.prevStamina;
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
		this.targetLockedOn = targetLockedOn;
	}
	
	public void toggleLockOn() {
		this.targetLockedOn = !this.targetLockedOn;
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
	
	@Override
	public void disableModelYRot(boolean sendPacket) {
		super.disableModelYRot(sendPacket);
		
		if (sendPacket) {
			EpicFightNetworkManager.sendToServer(new CPModifyEntityModelYRot());
		}
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
	
	@Override
	public void openSkillBook(ItemStack itemstack, InteractionHand hand) {
		if (itemstack.hasTag() && itemstack.getTag().contains("skill")) {
			Minecraft.getInstance().setScreen(new SkillBookScreen(this.original, itemstack, hand));
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
}