package yesman.epicfight.api.animation.types;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.PlaybackSpeedModifier;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.property.MoveCoordFunctions.MoveCoordGetter;
import yesman.epicfight.api.animation.property.MoveCoordFunctions.MoveCoordSetter;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.JointMaskEntry;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class ActionAnimation extends MainFrameAnimation {
	public static final TransformSheet EMPTY_SHEET = new TransformSheet(List.of(new Keyframe(0.0F, JointTransform.empty()), new Keyframe(Float.MAX_VALUE, JointTransform.empty())));
	
	public ActionAnimation(float convertTime, String path, Armature armature) {
		this(convertTime, Float.MAX_VALUE, path, armature);
	}
	
	public ActionAnimation(float convertTime, String path, Armature armature, boolean noRegister) {
		this(convertTime, Float.MAX_VALUE, path, armature, noRegister);
	}
	
	public ActionAnimation(float convertTime, float postDelay, String path, Armature armature) {
		this(convertTime, postDelay, path, armature, false);
	}
	
	public ActionAnimation(float convertTime, float postDelay, String path, Armature armature, boolean noRegister) {
		super(convertTime, path, armature, noRegister);
		
		this.stateSpectrumBlueprint.clear()
			.newTimePair(0.0F, postDelay)
			.addState(EntityState.MOVEMENT_LOCKED, true)
			.addState(EntityState.UPDATE_LIVING_MOTION, false)
			.addState(EntityState.CAN_BASIC_ATTACK, false)
			.addState(EntityState.CAN_SKILL_EXECUTION, false)
			.addState(EntityState.TURNING_LOCKED, true)
			.newTimePair(0.0F, Float.MAX_VALUE)
			.addState(EntityState.INACTION, true);
		
		this.addProperty(StaticAnimationProperty.FIXED_HEAD_ROTATION, true);
	}
	
	public <V> ActionAnimation addProperty(ActionAnimationProperty<V> propertyType, V value) {
		this.properties.put(propertyType, value);
		
		return this;
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		
		entitypatch.cancelAnyAction();
		
		if (entitypatch.shouldMoveOnCurrentSide(this)) {
			entitypatch.correctRotation();
			
			if (this.getProperty(ActionAnimationProperty.STOP_MOVEMENT).orElse(false)) {
				entitypatch.getOriginal().setDeltaMovement(0.0D, entitypatch.getOriginal().getDeltaMovement().y, 0.0D);
				entitypatch.getOriginal().xxa = 0.0F;
				entitypatch.getOriginal().yya = 0.0F;
				entitypatch.getOriginal().zza = 0.0F;
			}
			
			MoveCoordSetter moveCoordSetter = this.getProperty(ActionAnimationProperty.COORD_SET_BEGIN).orElse(MoveCoordFunctions.RAW_COORD);
			moveCoordSetter.set(this, entitypatch, entitypatch.getArmature().getActionAnimationCoord());
		}
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		this.move(entitypatch, this);
	}
	
	@Override
	public void linkTick(LivingEntityPatch<?> entitypatch, DynamicAnimation linkAnimation) {
		this.move(entitypatch, linkAnimation);
	}

	protected void move(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		if (!this.validateMovement(entitypatch, animation)) {
			return;
		}
		
		if (this.getState(EntityState.INACTION, entitypatch, entitypatch.getAnimator().getPlayerFor(this).getElapsedTime())) {
			LivingEntity livingentity = entitypatch.getOriginal();
			Vec3 vec3 = this.getCoordVector(entitypatch, animation);
			livingentity.move(MoverType.SELF, vec3);
		}
	}
	
	protected boolean validateMovement(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		if (!entitypatch.shouldMoveOnCurrentSide(this)) {
			return false;
		}
		
		if (animation.isLinkAnimation()) {
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
		JointTransform jt = pose.getOrDefaultTransform("Root");
		Vec3f jointPosition = jt.translation();
		OpenMatrix4f toRootTransformApplied = entitypatch.getArmature().searchJointByName("Root").getLocalTrasnform().removeTranslation();
		OpenMatrix4f toOrigin = OpenMatrix4f.invert(toRootTransformApplied, null);
		Vec3f worldPosition = OpenMatrix4f.transform3v(toRootTransformApplied, jointPosition, null);
		worldPosition.x = 0.0F;
		worldPosition.y = (this.getProperty(ActionAnimationProperty.MOVE_VERTICAL).orElse(false) && worldPosition.y > 0.0F) ? 0.0F : worldPosition.y;
		worldPosition.z = 0.0F;
		OpenMatrix4f.transform3v(toOrigin, worldPosition, worldPosition);
		jointPosition.x = worldPosition.x;
		jointPosition.y = worldPosition.y;
		jointPosition.z = worldPosition.z;
		
		super.modifyPose(animation, pose, entitypatch, time, partialTicks);
	}
	
	@Override
	public void setLinkAnimation(DynamicAnimation fromAnimation, Pose startPose, boolean isOnSameLayer, float convertTimeModifier, LivingEntityPatch<?> entitypatch, LinkAnimation dest) {
		dest.resetNextStartTime();
		
		float playTime = this.getPlaySpeed(entitypatch, dest);
		PlaybackSpeedModifier playSpeedModifier = this.getRealAnimation().getProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER).orElse(null);
		
		if (playSpeedModifier != null) {
			playTime = playSpeedModifier.modify(this, entitypatch, playTime, 0.0F, playTime);
		}
		
		playTime = Math.abs(playTime);
		playTime *= EpicFightOptions.A_TICK;
		
		float linkTime = convertTimeModifier > 0.0F ? convertTimeModifier + this.convertTime : this.convertTime;
		float totalTime = playTime * (int)Math.ceil(linkTime / playTime);
		float nextStartTime = Math.max(0.0F, -convertTimeModifier);
		nextStartTime += totalTime - linkTime;
		
		dest.setNextStartTime(nextStartTime);
		dest.getTransfroms().clear();
		dest.setTotalTime(totalTime + 0.001F);
		dest.setConnectedAnimations(fromAnimation, this);
		
		Pose nextStartPose = this.getPoseByTime(entitypatch, nextStartTime, 1.0F);
		
		if (entitypatch.shouldMoveOnCurrentSide(this) && this.getProperty(ActionAnimationProperty.MOVE_ON_LINK).orElse(true)) {
			this.removeRootTranslation(entitypatch, nextStartPose, nextStartTime);
		}
		
		Map<String, JointTransform> data1 = startPose.getJointTransformData();
		Map<String, JointTransform> data2 = nextStartPose.getJointTransformData();
		Set<String> joint1 = new HashSet<> (isOnSameLayer ? data1.keySet() : Set.of());
		Set<String> joint2 = new HashSet<> (data2.keySet());
		
		if (entitypatch.isLogicalClient()) {
			JointMaskEntry entry = fromAnimation.getJointMaskEntry(entitypatch, false).orElse(null);
			JointMaskEntry entry2 = this.getJointMaskEntry(entitypatch, true).orElse(null);
			
			if (entry != null && entitypatch.isLogicalClient()) {
				joint1.removeIf((jointName) -> entry.isMasked(fromAnimation.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER) == Layer.LayerType.BASE_LAYER ?
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
				this.removeRootTranslation(entitypatch, pose, 0.0F);
			}
			
			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[3];
				keyframes[0] = new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty()));
				keyframes[1] = new Keyframe(linkTime, poseData.get(jointName));
				keyframes[2] = new Keyframe(totalTime, data2.get(jointName));
				
				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		} else {
			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[2];
				keyframes[0] = new Keyframe(0.0F, data1.getOrDefault(jointName, JointTransform.empty()));
				keyframes[1] = new Keyframe(totalTime, data2.get(jointName));
				
				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		}
	}
	
	public void removeRootTranslation(LivingEntityPatch<?> entitypatch, Pose pose, float poseTime) {
		JointTransform jt = pose.getOrDefaultTransform("Root");
		
		if (this.getProperty(ActionAnimationProperty.COORD).isEmpty()) {
			Vec3f withPosition = entitypatch.getArmature().getActionAnimationCoord().getInterpolatedTranslation(poseTime);
			jt.translation().set(withPosition);
		} else {
			TransformSheet coordTransform = this.getProperty(ActionAnimationProperty.COORD).get();
			Vec3f nextCoord = coordTransform.getKeyframes()[0].transform().translation();
			jt.translation().add(0.0F, 0.0F, nextCoord.z);
		}
	}
	
	protected Vec3 getCoordVector(LivingEntityPatch<?> entitypatch, DynamicAnimation animation) {
		AnimationPlayer player = entitypatch.getAnimator().getPlayerFor(animation);
		TimePairList coordUpdateTime = this.getProperty(ActionAnimationProperty.COORD_UPDATE_TIME).orElse(null);
		boolean isCoordUpdateTime = coordUpdateTime == null || coordUpdateTime.isTimeInPairs(player.getElapsedTime());

		MoveCoordSetter moveCoordsetter = isCoordUpdateTime ? this.getProperty(ActionAnimationProperty.COORD_SET_TICK).orElse(null) : MoveCoordFunctions.RAW_COORD;
		
		if (moveCoordsetter != null) {
			moveCoordsetter.set(animation, entitypatch, entitypatch.getArmature().getActionAnimationCoord());
		}
		
		boolean hasNoGravity = entitypatch.getOriginal().isNoGravity();
		boolean moveVertical = this.getProperty(ActionAnimationProperty.MOVE_VERTICAL).orElse(this.getProperty(ActionAnimationProperty.COORD).isPresent());
		MoveCoordGetter moveGetter = isCoordUpdateTime ? this.getProperty(ActionAnimationProperty.COORD_GET).orElse(MoveCoordFunctions.DIFF_FROM_PREV_COORD) : MoveCoordFunctions.DIFF_FROM_PREV_COORD;
		Vec3f move = moveGetter.get(animation, entitypatch, entitypatch.getArmature().getActionAnimationCoord());
		LivingEntity livingentity = entitypatch.getOriginal();
		Vec3 motion = livingentity.getDeltaMovement();
		
		this.getProperty(ActionAnimationProperty.NO_GRAVITY_TIME).ifPresentOrElse((noGravityTime) -> {
			if (noGravityTime.isTimeInPairs(animation.isLinkAnimation() ? 0.0F : player.getElapsedTime())) {
				livingentity.setDeltaMovement(motion.x, 0.0D, motion.z);
			} else {
				move.y = 0.0F;
			}
		}, () -> {
			if (moveVertical && move.y > 0.0F && !hasNoGravity) {
				double gravity = livingentity.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).getValue();
				livingentity.setDeltaMovement(motion.x, motion.y <= 0.0F ? (motion.y + gravity) : motion.y, motion.z);
			}
		});
		
		return move.toDoubleVector();
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean shouldPlayerMove(LocalPlayerPatch playerpatch) {
		return playerpatch.isLogicalClient();
	}
}