package yesman.epicfight.api.client.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.minecraftforge.entity.PartEntity;
import yesman.epicfight.api.client.animation.AnimationSubFileReader.PovSettings;
import yesman.epicfight.api.client.hook.EpicFightClientHooks;
import yesman.epicfight.api.client.hook.instances.BuildCameraTransform;
import yesman.epicfight.api.client.hook.instances.ItemUsedInDecoupledCamera;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.handlers.InputManager;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.ZoomInType;

/**
 * Provides access to Epic Fight's camera and third-person systems, including
 * lock-on functionality, zoom controls, and camera rotation.
 * <p>
 * This API can be used to integrate with Epic Fight's custom third-person
 * camera or by addons to extend its functionality and provide explicit support.
 */
public final class EpicFightCameraAPI {
	private static final EpicFightCameraAPI INSTANCE = new EpicFightCameraAPI();
	private static final int MAX_ZOOM_TICK = 8;
	
	public static EpicFightCameraAPI getInstance() {
		return INSTANCE;
	}
	
	private final Minecraft minecraft;
	private final Set<UseAnim> tpsItemAnimations = Set.of(UseAnim.BLOCK, UseAnim.BOW, UseAnim.SPEAR, UseAnim.CROSSBOW);
	
	// Camera zoom parameters to activate the cemera TPS position in third-person back
	private boolean zoomingIn;
	private int zoomTick = 0;
	private int zoomOutDelay = 0;
	
	// Camera rotations for TPS mode and lock-on
	private float cameraXRotO;
	private float cameraXRot;
	private float cameraYRotO;
	private float cameraYRot;
	private boolean couplingYRot;
	
	// First-person view limit
	private float fpvXRotO;
	private float fpvXRot;
	private float fpvYRotO;
	private float fpvYRot;
	private int fpvLerpTick;
	private int maxFpvLerpTick;
	
	/**
	 * Temporary storage for crosshair destination in TPS mode
	 * This replaces {@link Minecraft#hitResult} in each frame when TPS mode is activated
	 */
	@Nullable
	private HitResult crosshairHitResult;
	
	/**
	 * An entity targeted by the crosshair
	 * This doesn't replace {@link Minecraft#crosshairPickEntity} since their usages are disparate
	 */
	@Nullable
	private LivingEntity focusingEntity;
	
	// Camera lock-on
	private boolean lockingOnTarget;
	private double accumulatedX;
	private int quickShiftDelay;
	
	// Singleton
	private EpicFightCameraAPI() {
		this.minecraft = Minecraft.getInstance();
	}
	
	/**
	 * Returns if the camera is TPS mode
	 * When zooming ranged weapons or TPS mode is turned on by config
	 */
	public boolean isTPSMode() {
		return this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK && ClientConfig.cameraMode.shouldSwitch(this);
	}
	
	public boolean isFirstPerson() {
		return this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
	}
	
	public boolean isZooming() {
		return this.zoomTick > 0;
	}
	
	public int zoomCount() {
		return this.zoomTick;
	}
	
	public void zoomIn() {
		if (!this.zoomingIn) {
			this.zoomingIn = true;
			this.zoomTick = this.zoomTick == 0 ? 1 : this.zoomTick;
		}
	}
	
	public void zoomOut(int zoomOutTicks) {
		if (this.zoomingIn) {
			this.zoomingIn = false;
			this.zoomOutDelay = zoomOutTicks;
		}
	}
	
	public float getCameraXRotO() {
		return this.cameraXRotO;
	}
	
	public float getCameraYRotO() {
		return this.cameraYRotO;
	}
	
	public float getCameraXRot() {
		return this.cameraXRot;
	}
	
	public float getCameraYRot() {
		return this.cameraYRot;
	}
	
	public void setCameraXRot(float xRot) {
		this.cameraXRot = xRot;
	}
	
	public void setCameraYRot(float yRot) {
		this.cameraYRot = yRot;
	}
	
	public void setCameraRotations(float xRot, float yRot, boolean syncOld) {
		this.cameraXRot = xRot;
		this.cameraYRot = yRot;
		
		if (syncOld) {
			this.cameraXRotO = this.cameraXRot;
			this.cameraYRotO = this.cameraYRot;
		}
	}
	
	/**
	 * You can manually couple the player look vector into the camera's
	 * <p>
	 * This is a scoped state that developers have to call decoupling method again, or the player
	 * will look at the crosshair forever.
	 * <p>
	 * Alternatively, if you want to focus the player to crosshair for specific item use, you can
	 * consider registering {@link EpicFightClientHooks.Camera#ITEM_USED_WHEN_DECOUPLED} for better maintenance
	 * <p>
	 * @param flag 	whether the player should follow the camera view
	 */
	public void setCouplingState(boolean flag) {
		if (this.isTPSMode()) {
			this.couplingYRot = flag;
		} else {
			this.couplingYRot = true;
		}
	}
	
	/**
	 * Returns the player-camera coupling state
	 */
	public boolean getCouplingState() {
		return this.couplingYRot;
	}
	
	/**
	 * Returns the x rotation of the forward vector of the camera
	 */
	public float getForwardXRot() {
		return this.isTPSMode() ? this.cameraXRot : this.minecraft.player.getXRot();
	}
	
	/**
	 * Returns the y rotation of the forward vector of the camera
	 */
	public float getForwardYRot() {
		return this.isTPSMode() ? this.cameraYRot : this.minecraft.player.getYRot();
	}
	
	/**
	 * Fixes the first-person rotation to destination with a given lerping time
	 * Normally used to center the first-person angle defined in {@link PovSettings.ViewLimit}
	 */
	public void fixFpvRotation(float xRot, float yRot, int lerpTick) {
		if (this.minecraft.player == null) return;
		
		this.fpvXRotO = Mth.wrapDegrees(this.minecraft.player.getXRot());
		this.fpvXRot = Mth.wrapDegrees(xRot);
		this.fpvYRotO = Mth.wrapDegrees(this.minecraft.player.getYRot());
		this.fpvYRot = Mth.wrapDegrees(yRot);
		this.fpvLerpTick = lerpTick;
		this.maxFpvLerpTick = lerpTick;
	}
	
	public float getLerpedFpvXRot(float partialTick) {
		float delta = ((this.fpvLerpTick / (float)this.maxFpvLerpTick) + (1.0F - partialTick) * (1.0F / 5.0F));
		return Mth.rotLerp(delta, this.fpvXRot, this.fpvXRotO);
	}
	
	public float getLerpedFpvYRot(float partialTick) {
		float delta = ((this.fpvLerpTick / (float)this.maxFpvLerpTick) + (1.0F - partialTick) * (1.0F / 5.0F));
		return Mth.rotLerp(delta, this.fpvYRot, this.fpvYRotO);
	}
	
	public boolean isLerpingFpv() {
		return this.fpvLerpTick > -1;
	}
	
	@Nullable
	public HitResult getCrosshairHitResult() {
		return this.crosshairHitResult;
	}
	
	public LivingEntity getFocusingEntity() {
		return this.focusingEntity;
	}
	
	/**
	 * Activates or deactivates camera lock-on to the entity that is focused by crosshair scan.
	 */
	public void setLockOn(boolean flag) {
		if (this.lockingOnTarget == flag) {
			return;
		}
		
		if (flag && this.focusingEntity == null) {
			this.lockingOnTarget = false;
		} else {
			this.lockingOnTarget = flag;
			
			// Sycn the camera rotation according to the camera mode
			if (!this.isTPSMode()) {
				if (!flag) {
					this.minecraft.player.setXRot(this.cameraXRot);
				} else {
					this.setCameraRotations(this.minecraft.player.getXRot(), this.minecraft.player.getYRot(), true);
				}
			}
		}
	}
	
	public void toggleLockOn() {
		this.setLockOn(!this.lockingOnTarget);
	}
	
	public boolean isLockingOnTarget() {
		return this.lockingOnTarget;
	}
	
	public int getFocusingEntityPickRange() {
		if (this.minecraft.player == null) return 0;
		
		return ClientConfig.lockOnRange;
	}
	
	/**
	 * Find a new target on the screen based on the direction
	 * <p>
	 * @param direction 	determines which direction it will start to find a new target
	 * 							-1: right
	 * 							 1: left
	 * 							 0: not considering a direction
	 * <p>
	 * @return 				true when found new lock-on target, else false
	 */
	public boolean setNextLockOnTarget(int direction) {
		// terminates when not locking-on
		if (!this.lockingOnTarget) {
			return false;
		}
		
		List<Entity> entitiesInLevel = new ArrayList<> ();
		this.minecraft.level.entitiesForRendering().forEach(entitiesInLevel::add);
		Vec3 cameraLocation = this.minecraft.gameRenderer.getMainCamera().getPosition();
		
		// Create a compact projection matrix without view, hurt bob
		PoseStack posestack = new PoseStack();
		double fov = this.minecraft.gameRenderer.getFov(this.minecraft.gameRenderer.getMainCamera(), 1.0F, true);
		double lockOnRange = this.getFocusingEntityPickRange();
		posestack.mulPoseMatrix(this.minecraft.gameRenderer.getProjectionMatrix(fov));
		Matrix4f compactProjection = posestack.last().pose();
		
		// Select the nearest target on the screen from the given direction
		Optional<Pair<LivingEntity, Float>> next = entitiesInLevel.stream()
			.filter(entity ->
				this.predicateFocusableEntity(entity) &&
				!entity.is(this.focusingEntity) &&
				MathUtils.canBeSeen(entity, this.minecraft.player, lockOnRange) &&
				(
					this.minecraft.getEntityRenderDispatcher().shouldRender(entity, this.minecraft.levelRenderer.getFrustum(), cameraLocation.x(), cameraLocation.y(), cameraLocation.z()) || // Excludes entities out of the view frustum
					entity.hasIndirectPassenger(this.minecraft.player)	// Excludes riding entities
				) &&
				entity.distanceToSqr(this.minecraft.player) < lockOnRange * lockOnRange
			)
			.map(entity -> Pair.of((LivingEntity)entity, MathUtils.worldToScreenCoord(compactProjection, this.minecraft.gameRenderer.getMainCamera(), entity.getBoundingBox().getCenter()).x))
			.filter(pair -> pair.getSecond() >= -1.0F && pair.getSecond() <= 1.0F && (direction == 0 || MathUtils.getSign(pair.getSecond()) == MathUtils.getSign(direction)))
			.min((p1, p2) -> Float.compare(Math.abs(p1.getSecond()), Math.abs(p2.getSecond())));
		
		next.ifPresent(pair -> {
			this.focusingEntity = pair.getFirst();
		});
		
		return next.isPresent();
	}
	
	/**
	 * Aligns the player's look to have same rotations as camera
	 * @param noInterpolation	resets old rotation values when true
	 */
	public void alignPlayerLookToCameraRotation(boolean noInterpolation) {
		if (this.minecraft.player == null) return;
		
		this.minecraft.player.setXRot(this.cameraXRot);
        this.minecraft.player.setYRot(this.cameraYRot);
        this.minecraft.player.setYHeadRot(this.cameraYRot);
        
        if (noInterpolation) {
        	this.minecraft.player.xRotO = this.cameraXRot;
        	this.minecraft.player.yRotO = this.cameraYRot;
        	this.minecraft.player.yHeadRotO = this.cameraYRot;
        }
	}
	
	/**
	 * Sets the player to look at the crosshair's destination
	 * @param noInterpolation	resets old rotation values when true
	 */
	public void alignPlayerLookToCrosshair(boolean noInterpolation) {
		if (this.minecraft.player == null) return;
		
		// If crosshairHitResult is null, aligns to camera rotation
		if (this.crosshairHitResult == null) this.alignPlayerLookToCameraRotation(noInterpolation);
		
		Vec3 fromEyeToDest = this.crosshairHitResult.getLocation().subtract(this.minecraft.player.getEyePosition());
		float xRot = (float)MathUtils.getXRotOfVector(fromEyeToDest);
		float yRot = (float)MathUtils.getYRotOfVector(fromEyeToDest);
		
		this.minecraft.player.setXRot(xRot);
        this.minecraft.player.setYRot(yRot);
        this.minecraft.player.setYHeadRot(yRot);
        
        if (noInterpolation) {
        	this.minecraft.player.xRotO = xRot;
        	this.minecraft.player.yRotO = yRot;
        	this.minecraft.player.yHeadRotO = yRot;
        }
	}
	
	/**
	 * Returns a rotated movement vector for @param relative, scaled by @param magnitude
	 */
	public Vec3 getRelativeMove(Vec3 relative, float magnitude) {
		return Entity.getInputVector(relative, magnitude, this.isTPSMode() && !this.lockingOnTarget ? this.cameraYRot : this.minecraft.player.getYRot());
	}
	
	/**
	 * Returns whether apply the entity outliner for current target & player's next behavior
	 * Appearing outline means the player will do Epic Fight attack instead of vanilla swings to hit entities or break blocks
	 */
	public boolean shouldHighlightTarget(@NotNull Entity entity) {
		// Checks the giant rule for target entity outline: config option, in-game state, and focusing entity
		if (!ClientConfig.enableTargetEntityGuide || this.minecraft.player == null || !entity.is(this.focusingEntity)) return false;
		
		/// When the outline is disabled by {@link EntityPatch#isOutlineVisible}
		if (!EpicFightCapabilities.getUnparameterizedEntityPatch(entity, EntityPatch.class).map(entitypatch -> entitypatch.isOutlineVisible(this.minecraft.player)).orElse(false)) {
			return false;
		}
		
		// When lock-on is activated, always apply the outliner
		if (this.lockingOnTarget) {
			return true;
		}
		
		if (ClientConfig.combatPreferredItems.contains(this.minecraft.player.getMainHandItem().getItem())) {
			// For the combat preferred items, checks if the holding item is the fastest tool to dig the block (e.g. sword <=> cobweb block)
			if (RenderEngine.hitResultEquals(this.minecraft.hitResult, HitResult.Type.BLOCK)) {
				BlockPos bp = ((BlockHitResult)this.minecraft.hitResult).getBlockPos();
				BlockState bs = this.minecraft.level.getBlockState(bp);
				return !this.minecraft.player.getMainHandItem().getItem().canAttackBlock(bs, this.minecraft.player.level(), bp, this.minecraft.player) || !this.minecraft.player.getMainHandItem().isCorrectToolForDrops(bs);
			}
			
			return true;
		} else {
			// if hit result is block, 
			return !RenderEngine.hitResultEquals(this.minecraft.hitResult, HitResult.Type.BLOCK);
		}
	}
	
	/**
	 * This method called when camera turns both in first-person and thrid-person.
	 * @return whether cancel the classic turn that rotates player head and camera at the same time
	 */
	@ApiStatus.Internal
	public boolean turnCamera(double dy, double dx) {
		MutableBoolean cancel = new MutableBoolean(false);
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
			cancel.setValue(this.isTPSMode() || this.lockingOnTarget);
			
			if (cancel.booleanValue()) {
				float modifier = !this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY) ? 0.15F : (ClientConfig.lockOnQuickShift ? 0.005F : 0.0F);
				this.setCameraRotations(Mth.clamp(this.cameraXRot + (float)dx * modifier, -90.0F, 90.0F), this.cameraYRot + (float)dy * modifier, false);
				
				if (ClientConfig.lockOnQuickShift && this.quickShiftDelay <= 0) {
					this.accumulatedX += -dy * 0.15F;
					
					if (Math.abs(this.accumulatedX) > 20.0D && this.lockingOnTarget) {
						this.setNextLockOnTarget(Mth.sign(this.accumulatedX));
						this.accumulatedX = 0.0D;
						this.quickShiftDelay = 4;
					}
				}
				
				this.accumulatedX *= 0.98D;
			}
		});
		
		return cancel.booleanValue();
	}
	
	/**
	 * An update task that is conducted before the client tick starts
	 */
	@ApiStatus.Internal
	public void preClientTick() {
		this.cameraXRotO = this.cameraXRot;
		this.cameraYRotO = this.cameraYRot;
		
		// Process zoom tick for ranged weapons to locate camera at TPS position
		if (this.zoomTick > 0) {
			// Removes zoom out standby tick first
			if (this.zoomOutDelay > 0) {
				this.zoomOutDelay--;
			} else {
				this.zoomTick += this.zoomingIn ? 1 : - 1;
				this.zoomTick = Math.min(MAX_ZOOM_TICK, this.zoomTick);
			}
		}
		
		if (this.quickShiftDelay > 0) --this.quickShiftDelay;
	}
	
	/**
	 * An update task that is conducted after the client tick ends, where all the player states are updated
	 */
	@ApiStatus.Internal
	public void postClientTick() {
		if (this.minecraft.isPaused() || this.minecraft.player == null) return;
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
			// Handle camera zoom in/out
			CapabilityItem mainhandItemCap = playerpatch.getAdvancedHoldingItemCapability(InteractionHand.MAIN_HAND);
			CapabilityItem offhandItemCap = playerpatch.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
			CapabilityItem.ZoomInType rangeWeaponZoomInType =
				mainhandItemCap.isEmpty() || mainhandItemCap.getZoomInType() == ZoomInType.NONE
					? offhandItemCap.getZoomInType() : mainhandItemCap.getZoomInType();
			
			switch (rangeWeaponZoomInType) {
			case ALWAYS -> {
				this.zoomIn();
			}
			case USE_TICK -> {
				if (playerpatch.getOriginal().getUseItemRemainingTicks() > 0) {
					this.zoomIn();
				} else {
					this.zoomOut(8);
				}
			}
			case AIMING -> {
				if (playerpatch.getClientAnimator().isAiming()) {
					this.zoomIn();
				} else {
					this.zoomOut(8);
				}
			}
			case CUSTOM -> {} //Zoom manually handled
			default -> {
				this.zoomOut(1);
			}
			}
		});
		
		// Calculate camera based ray trace result
		double pickRange = this.minecraft.options.renderDistance().get() * 16.0D;
		Vec3 cameraPos = this.minecraft.gameRenderer.getMainCamera().getPosition();
		Vec3 lookVec = new Vec3(this.minecraft.gameRenderer.getMainCamera().getLookVector());
		Vec3 rayEed = cameraPos.add(lookVec.x * pickRange, lookVec.y * pickRange, lookVec.z * pickRange);
		LocalPlayer localPlayer = this.minecraft.player;
		this.crosshairHitResult = localPlayer.level().clip(new ClipContext(cameraPos, rayEed, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, localPlayer));
		
		int focusingRange = this.getFocusingEntityPickRange();
		double entityPickRange = Math.min(this.crosshairHitResult.getLocation().distanceToSqr(cameraPos), focusingRange * focusingRange);
		AABB aabb = localPlayer.getBoundingBox().move(cameraPos.subtract(localPlayer.getEyePosition(1.0F))).expandTowards(lookVec.scale(entityPickRange)).inflate(1.0D, 1.0D, 1.0D);
        
		EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(localPlayer, cameraPos, rayEed, aabb, this::predicateFocusableEntity, entityPickRange);
		
		if (entityHitResult != null) {
			this.crosshairHitResult = entityHitResult;
			
			if (!entityHitResult.getEntity().is(this.focusingEntity)) {
				if (entityHitResult.getEntity() instanceof LivingEntity livingentity) {
					if (!(entityHitResult.getEntity() instanceof ArmorStand) && (!this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY))) {
						this.focusingEntity = livingentity;
					}
				} else if (entityHitResult.getEntity() instanceof PartEntity<?> partEntity) {
					Entity parent = partEntity.getParent();
					
					if (parent instanceof LivingEntity parentLivingEntity && (!this.lockingOnTarget || InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY))) {
						this.focusingEntity = parentLivingEntity;
					}
				} else {
					this.setLockOn(false);
					this.focusingEntity = null;
				}
				
				if (this.focusingEntity != null) {
					EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(this.focusingEntity.getId()));
				}
			}
		}
		
		boolean tpsMode = this.isTPSMode();
		
		if (tpsMode) {
			Vec3 view = new Vec3(this.minecraft.gameRenderer.getMainCamera().getLookVector());
			
			// If the hit result is in front of the player based on to camera, set missed.
			if (view.dot(this.crosshairHitResult.getLocation().subtract(localPlayer.getEyePosition()).normalize()) < -0.1D) {
				this.crosshairHitResult = BlockHitResult.miss(cameraPos.add(lookVec.x * 50.0D, lookVec.y * 50.0D, lookVec.z * 50.0D), Direction.UP, BlockPos.ZERO);
				
				if (!this.lockingOnTarget) {
					this.focusingEntity = null;
				}
			}
			
			// If the ray target is in front of the player based on to camera, set missed.
			if (this.focusingEntity != null) {
				double dot = view.dot(this.focusingEntity.getEyePosition().subtract(localPlayer.getEyePosition()));
				
				if (dot < -0.1D) {
					if (!this.lockingOnTarget) {
						this.focusingEntity = null;
					}
				}
			}
		}
		
		// Tick the target entity
		if (this.focusingEntity != null) {
			if (this.lockingOnTarget && !this.focusingEntity.isAlive()) {
				boolean releaseLockOn = !ClientConfig.lockOnQuickShift || !this.setNextLockOnTarget(0);

                // Searches a new lock-on target when current target is dead
                if (releaseLockOn) {
                    this.setLockOn(false);
                }
			} else {
				double distance = cameraPos.distanceToSqr(this.focusingEntity.position());
				double maxLockOnDistance = focusingRange * focusingRange;
				
				if (
					// Target is invisible
					this.focusingEntity.isInvisibleTo(localPlayer) ||
					// Distance too far
					distance > maxLockOnDistance ||
					// Has no line of sight
					!MathUtils.canBeSeen(this.focusingEntity, this.minecraft.player, maxLockOnDistance) ||
					// Angle between look vec and to target too wide
					!this.lockingOnTarget &&
						this.focusingEntity.getBoundingBox().getCenter().subtract(this.minecraft.gameRenderer.getMainCamera().getPosition()).normalize()
							.dot(new Vec3(this.minecraft.gameRenderer.getMainCamera().getLookVector())) < Mth.clampedLerp(0.0D, 0.99D, Mth.inverseLerp(Mth.clamp(distance, 1.0D, 3.5D), 1.0D, 3.5D))
				) {
					if (this.lockingOnTarget) {
						this.setLockOn(false);
					}
					
					this.focusingEntity = null;
					EpicFightNetworkManager.sendToServer(new CPSetPlayerTarget(-1));
				}
			}
		}
		
		if (this.isFirstPerson() && this.isLerpingFpv()) {
			this.fpvLerpTick--;
			
			// When lerping ends, sync the player rotation to lerping destination
			if (!this.isLerpingFpv()) {
				this.minecraft.player.setXRot(this.fpvXRot);
				this.minecraft.player.setYRot(this.fpvYRot);
			}
		} else if (!this.isTPSMode() && !this.lockingOnTarget) {
			// Sync camera rotation when camera coupled to player's view
			this.cameraXRot = this.minecraft.player.getXRot();
			this.cameraYRot = this.minecraft.player.getYRot();
		} else {
			/**
			 * We do assume playerpatch is never null, but check the null for the crash resistancy
			 */
			@Nullable
			LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(localPlayer, LocalPlayerPatch.class);
			float clamp = 30.0F;
			float desiredXRot = 0.0F;
			float desiredYRot = 0.0F;
			
			// Handle camera lock-on
			if (this.focusingEntity != null && this.lockingOnTarget && !this.isLerpingFpv() && !InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
				Vec3 povPos = tpsMode ? cameraPos : localPlayer.getEyePosition();
				Vec3 targetPos = tpsMode ? this.focusingEntity.getBoundingBox().getCenter() : this.focusingEntity.getEyePosition();
				Vec3 toTarget = targetPos.subtract(povPos);
				float xRot = (float)MathUtils.getXRotOfVector(toTarget);
				float yRot = (float)MathUtils.getYRotOfVector(toTarget);
				
				CameraType cameraType = this.minecraft.options.getCameraType();
				if (!cameraType.isFirstPerson()) xRot = Mth.clamp(xRot, -clamp, clamp);
				xRot += (cameraType.isFirstPerson() || tpsMode ? 0.0F : 30.0F + xRot * 0.5F);
				
				float xLerp = Mth.clamp(Mth.wrapDegrees(xRot - this.cameraXRot) * 0.4F, -clamp, clamp);
				float yLerp = Mth.clamp(Mth.wrapDegrees(yRot - this.cameraYRot) * 0.4F, -clamp, clamp);
				Vec3 playerToTarget = targetPos.subtract(localPlayer.getEyePosition());
				
				// Limit angle difference in tps mode
				if (tpsMode) {
					double angle = MathUtils.getAngleBetween(playerToTarget, toTarget);
					if (angle < 30.0D) this.setCameraRotations(this.cameraXRot + xLerp, this.cameraYRot + yLerp, false);
				} else {
					this.setCameraRotations(this.cameraXRot + xLerp, this.cameraYRot + yLerp, false);
				}
				
				desiredXRot = (float)MathUtils.getXRotOfVector(playerToTarget);
				desiredYRot = (float)MathUtils.getYRotOfVector(playerToTarget);
			} else if (this.lockingOnTarget && InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
				desiredXRot = this.cameraXRot;
				desiredYRot = this.cameraYRot;
			} else if (tpsMode) { // Handle camera tps rotation
				boolean shouldCoupling = this.predicateCouplingPlayer();
				
				// The player follows the camera look when the head rotation is not clamped by body rotation
				// This gives a slight control of the player's head within the allowed angles
				if (Mth.abs(Mth.wrapDegrees(this.cameraYRot - localPlayer.yBodyRot)) <= 51.0F || shouldCoupling) {
					Vec3 toHitResult;
					
					if (this.lockingOnTarget) {
						toHitResult = this.focusingEntity.getEyePosition();
					} else if (this.crosshairHitResult.getType() == HitResult.Type.MISS) {
						// Determines lookscale based on x rotation for parabola-trajectory projectiles
						double delta = Mth.clamp(localPlayer.getXRot(), -30.0F, 0.0F) / -30.0F;
						double lookVecScale = Mth.clampedLerp(30.0D, 75.0D, delta);
						toHitResult = cameraPos.add(lookVec.scale(lookVecScale));
					} else {
						toHitResult = this.crosshairHitResult.getLocation();
					}
					
					toHitResult = toHitResult.subtract(localPlayer.getEyePosition());
					desiredXRot = (float)MathUtils.getXRotOfVector(toHitResult);
					desiredYRot = shouldCoupling ? (Math.abs(this.cameraXRot) > 80.0F ? this.cameraYRot : (float)MathUtils.getYRotOfVector(toHitResult)) : this.cameraYRot;
				} else {
					desiredXRot = 0.0F;
					desiredYRot = localPlayer.yBodyRot;
					clamp = 15.0F;
				}
			}
			
			// Turns the player to desired rotation, based on the player state and camera setup
			if (
				(playerpatch == null || !playerpatch.getEntityState().turningLocked() || playerpatch.getEntityState().lockonRotate()) &&
				(tpsMode || this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK && this.lockingOnTarget)
			) {
				float xDelta = Mth.clamp(Mth.wrapDegrees(desiredXRot - localPlayer.getXRot()), -clamp, clamp);
				float yDelta = Mth.clamp(Mth.wrapDegrees(desiredYRot - localPlayer.getYRot()), -clamp, clamp);
				localPlayer.setXRot(localPlayer.getXRot() + xDelta);
				localPlayer.setYRot(localPlayer.getYRot() + yDelta);
			}
		}
	}
	
	/**
	 * Sets up the camera transform before {@link ComputeCameraAngles} is called, so that Minecraft doesn't calculate the transform twice
	 * 
	 * @return true when vanilla camera calculation shouldn't done
	 */
	@ApiStatus.Internal
	public boolean setupCamera(Camera camera, float partialTick) {
		if (!camera.getEntity().is(this.minecraft.player)) {
			return false;
		}
		
		BuildCameraTransform.Pre hook = new BuildCameraTransform.Pre(this, camera, partialTick);
		EpicFightClientHooks.Camera.BUILD_TRANSFORM_PRE.post(hook);
		
		if (hook.hasCanceled()) {
			return true;
		}
		
		if (this.isTPSMode()) {
			float partialZoomTick = this.zoomTick == 0 ? 0.0F : Math.min(this.zoomTick + (this.zoomingIn ? partialTick : -partialTick), MAX_ZOOM_TICK - 1);
			float delta = ClientConfig.cameraMode == ClientConfig.TPSType.WHEN_AIMING ? partialZoomTick / (float)(MAX_ZOOM_TICK - 1) : 1.0F;
			float xRot = Mth.rotLerp(delta, this.minecraft.player.getXRot(), Mth.rotLerp(partialTick, this.cameraXRotO, this.cameraXRot));
			float yRot = Mth.rotLerp(delta, this.minecraft.player.getYRot(), Mth.rotLerp(partialTick, this.cameraYRotO, this.cameraYRot));
			camera.setRotation(yRot, xRot);
			
			Vec3 playerPos = new Vec3(
				Mth.lerp((double)partialTick, camera.getEntity().xo, camera.getEntity().getX()),
				Mth.lerp((double)partialTick, camera.getEntity().yo, camera.getEntity().getY()) + Mth.lerp((double)partialTick, camera.eyeHeightOld, camera.eyeHeight),
				Mth.lerp((double)partialTick, camera.getEntity().zo, camera.getEntity().getZ())
			);
			
			Vec3f relocation = new Vec3f(ClientConfig.cameraHorizontalLocation * 0.2F, ClientConfig.cameraVerticalLocation * 0.2F, 0.0F);
			OpenMatrix4f.transform3v(OpenMatrix4f.createRotatorDeg(-yRot, Vec3f.Y_AXIS), relocation, relocation);
			double cameraZoom = ClientConfig.cameraZoom * 0.5D - (partialZoomTick * 0.1D);
			double hitDistance = 1.0D;
			
			for (int i = 0; i < 8; ++i) {
				float f = (float)((i & 1) * 2 - 1);
				float f1 = (float)((i >> 1 & 1) * 2 - 1);
				float f2 = (float)((i >> 2 & 1) * 2 - 1);
				f *= 0.1F;
				f1 *= 0.1F;
				f2 *= 0.1F;
				
				Vec3 vec3 = playerPos.add((double)f, (double)f1, (double)f2);
				Vec3 vec31 = new Vec3(
					playerPos.x + (relocation.x - (double)camera.getLookVector().x() * cameraZoom) + (double)f,
					playerPos.y + (relocation.y - (double)camera.getLookVector().y() * cameraZoom) + (double)f1,
					playerPos.z + (relocation.z - (double)camera.getLookVector().z() * cameraZoom) + (double)f2
				);
				double length = vec3.distanceTo(vec31);
				HitResult hitresult = this.minecraft.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, camera.getEntity()));
				
				if (hitresult.getType() != HitResult.Type.MISS) {
					double d0 = hitresult.getLocation().distanceTo(playerPos) / length;
					
					if (d0 < hitDistance) {
						hitDistance = d0;
					}
				}
			}
			
			double nearestX = playerPos.x + (relocation.x - (double)camera.getLookVector().x() * cameraZoom) * hitDistance;
			double nearestY = playerPos.y + (relocation.y - (double)camera.getLookVector().y() * cameraZoom) * hitDistance;
			double nearestZ = playerPos.z + (relocation.z - (double)camera.getLookVector().z() * cameraZoom) * hitDistance;
			
			if (Float.compare(1.0F, delta) == 0) {
				camera.setPosition(nearestX, nearestY, nearestZ);
			} else {
				// Setup vanilla location first
				camera.setRotation(this.minecraft.player.getViewYRot(partialTick), this.minecraft.player.getViewXRot(partialTick));
				camera.setPosition(
					Mth.lerp((double) partialTick, this.minecraft.player.xo, this.minecraft.player.getX()),
					Mth.lerp((double) partialTick, this.minecraft.player.yo, this.minecraft.player.getY()) + (double) Mth.lerp(partialTick, camera.eyeHeightOld, camera.eyeHeight),
					Mth.lerp((double) partialTick, this.minecraft.player.zo, this.minecraft.player.getZ())
				);
				camera.move(-camera.getMaxZoom(4.0D), 0.0D, 0.0D);
				
				// Restore rotation
				camera.setRotation(yRot, xRot);
				camera.setPosition(
					camera.getPosition().x() + (nearestX - camera.getPosition().x()) * delta,
					camera.getPosition().y() + (nearestY - camera.getPosition().y()) * delta,
					camera.getPosition().z() + (nearestZ - camera.getPosition().z()) * delta
				);
			}
			
			return true;
		} else if (this.lockingOnTarget && this.focusingEntity != null) {
			if (this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
				float xRot = Mth.rotLerp(partialTick, this.cameraXRotO, this.cameraXRot);
				float yRot = Mth.rotLerp(partialTick, this.cameraYRotO, this.cameraYRot);
				
				camera.setRotation(yRot, xRot);
				camera.setPosition(
					Mth.lerp((double)partialTick, camera.getEntity().xo, camera.getEntity().getX()),
					Mth.lerp((double)partialTick, camera.getEntity().yo, camera.getEntity().getY()) + (double)Mth.lerp(partialTick, camera.eyeHeightOld, camera.eyeHeight),
					Mth.lerp((double)partialTick, camera.getEntity().zo, camera.getEntity().getZ())
				);
				
				if (camera.isDetached()) {
					camera.move(-camera.getMaxZoom(4.0D), 0.0D, 0.0D);
				} else if (camera.getEntity() instanceof LivingEntity livingEntity && livingEntity.isSleeping()) {
					Direction direction = ((LivingEntity)camera.getEntity()).getBedOrientation();
					camera.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
					camera.move(0.0D, 0.3D, 0.0D);
				}
				return true;
			} else if (this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON) {
				if (!InputManager.isActionActive(EpicFightInputAction.LOCK_ON_SHIFT_FREELY)) {
					camera.getEntity().setXRot(Mth.rotLerp(partialTick, this.cameraXRotO, this.cameraXRot));
					camera.getEntity().setYRot(Mth.rotLerp(partialTick, this.cameraYRotO, this.cameraYRot));
				} else {
					this.cameraXRot = camera.getEntity().getXRot();
					this.cameraYRot = camera.getEntity().getYRot();
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Called after {@link #setupCamera} to apply a simple transform
	 */
	@ApiStatus.Internal
	public void fireCameraBuildPost(Camera camera, float partialTick) {
		EpicFightClientHooks.Camera.BUILD_TRANSFORM_POST.post(new BuildCameraTransform.Post(this, camera, partialTick));
	}
	
	/**
	 * REturns a new basis for {@link LivingEntity#yRotHead} instead of coupling it to {@link Entity#getYRot}
	 */
	@ApiStatus.Internal
	public float getYRotForHead(LocalPlayer localPlayer) {
		return (this.isTPSMode() && (Mth.abs(Mth.wrapDegrees(this.cameraYRot - localPlayer.yBodyRot)) <= 51.0F || this.predicateCouplingPlayer())) ? this.cameraYRot : localPlayer.getYRot();
	}
	
	private boolean predicateFocusableEntity(Entity entity) {
        return entity instanceof LivingEntity livingEntity && !entity.isSpectator() && entity.isPickable() && entity.isAlive() && !entity.is(this.minecraft.player) && this.minecraft.player.canAttack(livingEntity);
    }
	
	private boolean predicateCouplingPlayer() {
		/**
		 * We do assume playerpatch is never null, but check the null for the crash resistancy
		 */
		@Nullable
		LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(this.minecraft.player, LocalPlayerPatch.class);
		
		return InputManager.getInputState(this.minecraft.player.input).getMoveVector().lengthSquared() > 0.0F ||								// When moving
				this.minecraft.options.keyAttack.isDown() ||																					// When pressing left button
				this.minecraft.player.isUsingItem() && tpsItemAnimations.contains(this.minecraft.player.getUseItem().getUseAnimation()) ||		// When using an item with pre-defined use animations
				this.isZooming() ||																												// when zooming
				(playerpatch == null || playerpatch.isHoldingAny()) ||																			// When holding a skill
				this.couplingYRot;																												// When the player rotation is manually coupled
	}
	
	@ApiStatus.Internal
	public void onItemUseHook(Player player, PlayerPatch<?> playerpatch, ItemStack itemstack, InteractionHand hand) {
		if (this.isTPSMode()) EpicFightClientHooks.Camera.ITEM_USED_WHEN_DECOUPLED.post(new ItemUsedInDecoupledCamera(this, player, playerpatch, itemstack, hand));
	}
}
