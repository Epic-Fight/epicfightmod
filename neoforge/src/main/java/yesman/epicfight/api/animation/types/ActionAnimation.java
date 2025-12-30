package yesman.epicfight.api.animation.types;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.AnimationVariables.IndependentVariableKey;
import yesman.epicfight.api.animation.AnimationVariables.SharedVariableKey;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.PlaybackSpeedModifier;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.property.MoveCoordFunctions.MoveCoordGetter;
import yesman.epicfight.api.animation.property.MoveCoordFunctions.MoveCoordSetter;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.BiDirectionalSyncAnimationPositionPacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActionAnimation extends MainFrameAnimation {
	public static final SharedVariableKey<TransformSheet> ACTION_ANIMATION_COORD = AnimationVariables.unsynchShared(animator -> new TransformSheet(), false);
	public static final IndependentVariableKey<Vec3> BEGINNING_LOCATION = AnimationVariables.unsyncIndependent(animator -> animator.getEntityPatch().getOriginal().position(), true);
	public static final IndependentVariableKey<Float> INITIAL_LOOK_VEC_DOT = AnimationVariables.unsyncIndependent(animator -> 1.0F, true);
	
	public ActionAnimation(float transitionTime, AnimationAccessor<? extends ActionAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		this(transitionTime, Float.MAX_VALUE, accessor, armature);
	}
	
	public ActionAnimation(float transitionTime, float postDelay, AnimationAccessor<? extends ActionAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		super(transitionTime, accessor, armature);
		
		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, postDelay)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.UPDATE_LIVING_MOTION, false)
			.addState(EntityState.COMBO_ATTACKS_DOABLE, false)
			.addState(EntityState.SKILL_EXECUTABLE, false)
			.addState(EntityState.TURNING_LOCKED, true)
			.newTimePair(0.0F, Float.MAX_VALUE)
			.addState(EntityState.INACTION, true);
		
		this.addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
	}
	
	/**
	 * For resourcepack animations
	 */
	public ActionAnimation(float transitionTime, float postDelay, String path, AssetAccessor<? extends Armature> armature) {
		super(transitionTime, path, armature);
		
		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, postDelay)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.UPDATE_LIVING_MOTION, false)
			.addState(EntityState.COMBO_ATTACKS_DOABLE, false)
			.addState(EntityState.SKILL_EXECUTABLE, false)
			.addState(EntityState.TURNING_LOCKED, true)
			.newTimePair(0.0F, Float.MAX_VALUE)
			.addState(EntityState.INACTION, true);
		
		this.addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
	}
	
	@Override
	public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
		if (entitypatch.shouldMoveOnCurrentSide(this)) {
			MoveCoordSetter moveCoordSetter = this.getProperty(ActionAnimationProperty.COORD_SET_BEGIN).orElse(MoveCoordFunctions.RAW_COORD);
			moveCoordSetter.set(this, entitypatch, entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(ACTION_ANIMATION_COORD));
		}
		
		super.putOnPlayer(animationPlayer, entitypatch);
	}
	
	protected void initCoordVariables(LivingEntityPatch<?> entitypatch) {
		Vec3 start = entitypatch.getOriginal().position();
		
		if (entitypatch.getTarget() != null) {
			Vec3 targetTracePosition = entitypatch.getTarget().position();
			Vec3 toDestWorld = targetTracePosition.subtract(start);
			float dot = Mth.clamp((float)toDestWorld.normalize().dot(MathUtils.getVectorForRotation(0.0F, entitypatch.getYRot())), 0.0F, 1.0F);
			entitypatch.getAnimator().getVariables().put(INITIAL_LOOK_VEC_DOT, this.getAccessor(), dot);
		}
		
		entitypatch.getAnimator().getVariables().put(BEGINNING_LOCATION, this.getAccessor(), start);
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		entitypatch.cancelItemUse();
		
		super.begin(entitypatch);
		
		if (entitypatch.shouldMoveOnCurrentSide(this)) {
			entitypatch.beginAction(this);
			
			this.initCoordVariables(entitypatch);
			
			if (this.getProperty(ActionAnimationProperty.STOP_MOVEMENT).orElse(false)) {
				entitypatch.getOriginal().setDeltaMovement(0.0D, entitypatch.getOriginal().getDeltaMovement().y, 0.0D);
				entitypatch.getOriginal().xxa = 0.0F;
				entitypatch.getOriginal().yya = 0.0F;
				entitypatch.getOriginal().zza = 0.0F;
			}
		}
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		
		if (this.getProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT).orElse(false)) {
			double gravity = this.getProperty(ActionAnimationProperty.MOVE_VERTICAL).orElse(false) ? 0.0D : entitypatch.getOriginal().getDeltaMovement().y;
			entitypatch.getOriginal().setDeltaMovement(0.0D, gravity, 0.0D);
		}
		
		this.move(entitypatch, this.getAccessor());
	}
	
	@Override
	public void linkTick(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> linkAnimation) {
		if (this.getProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT).orElse(false)) {
			double gravity = this.getProperty(ActionAnimationProperty.MOVE_VERTICAL).orElse(false) ? 0.0D : entitypatch.getOriginal().getDeltaMovement().y;
			entitypatch.getOriginal().setDeltaMovement(0.0D, gravity, 0.0D);
		}
		
		this.move(entitypatch, linkAnimation);
	}

	protected void move(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
		if (!this.validateMovement(entitypatch, animation)) {
			return;
		}
		
		float elapsedTime = entitypatch.getAnimator().getPlayerFor(this.getAccessor()).getElapsedTime();
		
		if (this.getState(EntityState.INACTION, entitypatch, elapsedTime)) {
			LivingEntity livingentity = entitypatch.getOriginal();
			Vec3 vec3 = this.getCoordVector(entitypatch, animation);
			livingentity.move(MoverType.SELF, vec3);
			
			if (entitypatch.isLogicalClient()) {
				EpicFightNetworkManager.sendToServer(new BiDirectionalSyncAnimationPositionPacket(livingentity.getId(), elapsedTime, livingentity.position(), animation.get().isLinkAnimation() ? 2 : 1));
			} else {
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new BiDirectionalSyncAnimationPositionPacket(livingentity.getId(), elapsedTime, livingentity.position(), animation.get().isLinkAnimation() ? 2 : 1), livingentity);
			}
		}
	}
	
	protected boolean validateMovement(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
		if (!entitypatch.shouldMoveOnCurrentSide(this)) {
			return false;
		}
		
		if (animation.get().isLinkAnimation()) {
			if (!this.getProperty(ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
				return false;
			} else {
				return this.shouldMove(0.0F);
			}
		} else {
			return this.shouldMove(entitypatch.getAnimator().getPlayerFor(animation).getElapsedTime());
		}
	}
	
	protected boolean shouldMove(float currentTime) {
		if (this.properties.containsKey(ActionAnimationProperty.MOVE_TIME)) {
			TimePairList moveTimes = this.getProperty(ActionAnimationProperty.MOVE_TIME).get();
			return moveTimes.isTimeInPairs(currentTime);
		} else {
			return true;
		}
	}
	
	@Override
	public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		if (this.getProperty(ActionAnimationProperty.COORD).isEmpty()) {
			this.correctRootJoint(animation, pose, entitypatch, time, partialTicks);
		}
		
		super.modifyPose(animation, pose, entitypatch, time, partialTicks);
	}
	
	public void correctRootJoint(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		JointTransform jt = pose.orElseEmpty("Root");
		Vec3f jointPosition = jt.translation();
		OpenMatrix4f toRootTransformApplied = entitypatch.getArmature().searchJointByName("Root").getLocalTransform().removeTranslation();
		OpenMatrix4f toOrigin = OpenMatrix4f.invert(toRootTransformApplied, null);
		Vec3f worldPosition = OpenMatrix4f.transform3v(toRootTransformApplied, jointPosition, null);
		worldPosition.x = 0.0F;
		worldPosition.y = (this.getProperty(ActionAnimationProperty.MOVE_VERTICAL).orElse(false) && worldPosition.y > 0.0F) ? 0.0F : worldPosition.y;
		worldPosition.z = 0.0F;
		OpenMatrix4f.transform3v(toOrigin, worldPosition, worldPosition);
		jointPosition.x = worldPosition.x;
		jointPosition.y = worldPosition.y;
		jointPosition.z = worldPosition.z;
	}
	
	@Override
	public void setLinkAnimation(AssetAccessor<? extends DynamicAnimation> fromAnimation, Pose startPose, boolean isOnSameLayer, float transitionTimeModifier, LivingEntityPatch<?> entitypatch, LinkAnimation dest) {
		dest.resetNextStartTime();
		float playTime = this.getPlaySpeed(entitypatch, dest);
		PlaybackSpeedModifier playSpeedModifier = this.getRealAnimation().get().getProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER).orElse(null);
		
		if (playSpeedModifier != null) {
			playTime = playSpeedModifier.modify(this, entitypatch, playTime, 0.0F, playTime);
		}
		
		playTime = Math.abs(playTime) * EpicFightSharedConstants.A_TICK;
		
		float linkTime = (transitionTimeModifier > 0.0F) ? transitionTimeModifier + this.transitionTime : this.transitionTime;
		float totalTime = playTime * (int)Math.ceil(linkTime / playTime);
		float nextStartTime = Math.max(0.0F, -transitionTimeModifier);
		nextStartTime += totalTime - linkTime;
		
		dest.setNextStartTime(nextStartTime);
		dest.getAnimationClip().reset();
		dest.setTotalTime(totalTime);
		dest.setConnectedAnimations(fromAnimation, this.getAccessor());
		
		Pose nextStartPose = this.getPoseByTime(entitypatch, nextStartTime, 1.0F);
		
		if (entitypatch.shouldMoveOnCurrentSide(this) && this.getProperty(ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
			this.correctRawZCoord(entitypatch, nextStartPose, nextStartTime);
		}
		
		Map<String, JointTransform> data1 = startPose.getJointTransformData();
		Map<String, JointTransform> data2 = nextStartPose.getJointTransformData();
		Set<String> joint1 = new HashSet<> (isOnSameLayer ? data1.keySet() : Set.of());
		Set<String> joint2 = new HashSet<> (data2.keySet());
		
		if (entitypatch.isLogicalClient()) {
			JointMaskEntry entry = fromAnimation.get().getJointMaskEntry(entitypatch, false).orElse(null);
			JointMaskEntry entry2 = this.getJointMaskEntry(entitypatch, true).orElse(null);
			
			if (entry != null && entitypatch.isLogicalClient()) {
				joint1.removeIf((jointName) -> entry.isMasked(fromAnimation.get().getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER ?
						entitypatch.getClientAnimator().currentMotion() : entitypatch.getClientAnimator().currentCompositeMotion(), jointName));
			}
			
			if (entry2 != null && entitypatch.isLogicalClient()) {
				joint2.removeIf((jointName) -> entry2.isMasked(this.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER ?
						entitypatch.getCurrentLivingMotion() : entitypatch.currentCompositeMotion, jointName));
			}
		}
		
		joint1.addAll(joint2);
		
		if (linkTime != totalTime) {
			Pose pose = this.getPoseByTime(entitypatch, 0.0F, 0.0F);
			Map<String, JointTransform> poseData = pose.getJointTransformData();
			
			if (entitypatch.shouldMoveOnCurrentSide(this) && this.getProperty(ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
				this.correctRawZCoord(entitypatch, pose, 0.0F);
			}
			
			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[3];
				keyframes[0] = new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty()));
				keyframes[1] = new Keyframe(linkTime, poseData.getOrDefault(jointName, JointTransform.empty()));
				keyframes[2] = new Keyframe(totalTime, data2.getOrDefault(jointName, JointTransform.empty()));
				
				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		} else {
			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[2];
				keyframes[0] = new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty()));
				keyframes[1] = new Keyframe(totalTime, data2.getOrDefault(jointName, JointTransform.empty()));
				
				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		}
		
		dest.loadCoord(null);
		
		this.getProperty(ActionAnimationProperty.COORD).ifPresent((coord) -> {
			Keyframe[] keyframes = new Keyframe[2];
			keyframes[0] = new Keyframe(0.0F, JointTransform.empty());
			keyframes[1] = new Keyframe(totalTime, coord.getKeyframes()[0].transform());
			
			TransformSheet sheet = new TransformSheet(keyframes);
			dest.loadCoord(sheet);
		});
		
		if (entitypatch.shouldMoveOnCurrentSide(this)) {
			MoveCoordSetter moveCoordSetter = this.getProperty(ActionAnimationProperty.COORD_SET_BEGIN).orElse(MoveCoordFunctions.RAW_COORD);
			moveCoordSetter.set(dest, entitypatch, entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(ACTION_ANIMATION_COORD));
		}
	}
	
	public void correctRawZCoord(LivingEntityPatch<?> entitypatch, Pose pose, float poseTime) {
		JointTransform jt = pose.orElseEmpty("Root");
		
		if (this.getProperty(ActionAnimationProperty.COORD).isEmpty()) {
			TransformSheet coordTransform = this.getTransfroms().get("Root");
			jt.translation().add(0.0F, 0.0F, coordTransform.getInterpolatedTranslation(poseTime).z);
		}
	}
	
	/**
	 * Get an expected movement vector in specific time
	 * 
	 * @param entitypatch
	 * @param elapseTime
	 * @return
	 */
	public Vec3 getExpectedMovement(LivingEntityPatch<?> entitypatch, float elapseTime) {
		this.initCoordVariables(entitypatch);
		
		TransformSheet coordTransform = new TransformSheet();
		MoveCoordSetter moveCoordSetter = this.getProperty(ActionAnimationProperty.COORD_SET_BEGIN).orElse(MoveCoordFunctions.RAW_COORD);
		moveCoordSetter.set(this, entitypatch, coordTransform);
		
		MoveCoordGetter moveGetter = this.getProperty(ActionAnimationProperty.COORD_GET).orElse(MoveCoordFunctions.MODEL_COORD);
		Vec3f move = moveGetter.get(this, entitypatch, coordTransform, 0.0F, elapseTime);
		
		return move.toDoubleVector();
	}
	
	protected Vec3 getCoordVector(LivingEntityPatch<?> entitypatch, AssetAccessor<? extends DynamicAnimation> animation) {
		AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(animation);
		TimePairList coordUpdateTime = this.getProperty(ActionAnimationProperty.COORD_UPDATE_TIME).orElse(null);
		boolean inUpdateTime = coordUpdateTime == null || coordUpdateTime.isTimeInPairs(player.getElapsedTime());
		boolean getRawCoord = this.getProperty(AttackAnimationProperty.FIXED_MOVE_DISTANCE).orElse(!inUpdateTime);
		TransformSheet transformSheet = entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(ACTION_ANIMATION_COORD);
		MoveCoordSetter moveCoordsetter = getRawCoord ? MoveCoordFunctions.RAW_COORD : this.getProperty(ActionAnimationProperty.COORD_SET_TICK).orElse(null);
		
		if (moveCoordsetter != null) {
			moveCoordsetter.set(animation.get(), entitypatch, transformSheet);
		}
		
		boolean hasNoGravity = entitypatch.getOriginal().isNoGravity();
		boolean moveVertical = this.getProperty(ActionAnimationProperty.MOVE_VERTICAL).orElse(this.getProperty(ActionAnimationProperty.COORD).isPresent());
		MoveCoordGetter moveGetter = getRawCoord ? MoveCoordFunctions.MODEL_COORD : this.getProperty(ActionAnimationProperty.COORD_GET).orElse(MoveCoordFunctions.MODEL_COORD);
		
		Vec3f move = moveGetter.get(animation.get(), entitypatch, transformSheet, player.getPrevElapsedTime(), player.getElapsedTime());
		LivingEntity livingentity = entitypatch.getOriginal();
		Vec3 motion = livingentity.getDeltaMovement();
		
		this.getProperty(ActionAnimationProperty.NO_GRAVITY_TIME).ifPresentOrElse((noGravityTime) -> {
			if (noGravityTime.isTimeInPairs(animation.get().isLinkAnimation() ? 0.0F : player.getElapsedTime())) {
				livingentity.setDeltaMovement(motion.x, 0.0D, motion.z);
			} else {
				move.y = 0.0F;
			}
		}, () -> {
			if (moveVertical && move.y > 0.0F && !hasNoGravity) {
				double gravity = livingentity.getGravity();
				livingentity.setDeltaMovement(motion.x, motion.y < 0.0D ? motion.y + gravity : 0.0D, motion.z);
			}
		});
		
		if (!moveVertical) {
			move.y = 0.0F;
		}
		
		if (inUpdateTime) {
			this.getProperty(ActionAnimationProperty.ENTITY_YROT_PROVIDER).ifPresent((entityYRotProvider) -> {
				float yRot = entityYRotProvider.get(animation.get(), entitypatch);
				entitypatch.setYRot(yRot);
			});
		}
		
		return move.toDoubleVector();
	}

    @ClientOnly
	public boolean shouldPlayerMove(LocalPlayerPatch playerpatch) {
		return playerpatch.isLogicalClient();
	}
}