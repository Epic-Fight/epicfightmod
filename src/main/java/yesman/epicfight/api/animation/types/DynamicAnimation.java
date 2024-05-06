package yesman.epicfight.api.animation.types;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationClip;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.AnimationProperty.PlaybackSpeedModifier;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.EntityState.StateFactor;
import yesman.epicfight.api.client.animation.property.JointMask.BindModifier;
import yesman.epicfight.api.utils.TypeFlexibleHashMap;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class DynamicAnimation {
	protected final boolean isRepeat;
	protected final float convertTime;
	
	public DynamicAnimation() {
		this(EpicFightOptions.GENERAL_ANIMATION_CONVERT_TIME, false);
	}
	
	public DynamicAnimation(float convertTime, boolean isRepeat) {
		this.isRepeat = isRepeat;
		this.convertTime = convertTime;
	}
	
	public final Pose getRawPose(float time) {
		return this.getAnimationClip().getPoseInTime(time);
	}
	
	public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		Pose pose = this.getRawPose(time);
		this.modifyPose(this, pose, entitypatch, time, partialTicks);
		
		return pose;
	}
	
	/** Modify the pose both this and link animation. **/
	public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
	}
	
	public void setLinkAnimation(final DynamicAnimation fromAnimation, Pose pose, boolean isOnSameLayer, float convertTimeModifier, LivingEntityPatch<?> entitypatch, LinkAnimation dest) {
		if (!entitypatch.isLogicalClient()) {
			pose = Animations.DUMMY_ANIMATION.getPoseByTime(entitypatch, 0.0F, 1.0F);
		}
		
		boolean skipFirstPose = convertTimeModifier < 0.0F;
		float totalTime = convertTimeModifier >= 0.0F ? convertTimeModifier + this.convertTime : this.convertTime;
		float playbackSpeed = this.getPlaySpeed(entitypatch);
		PlaybackSpeedModifier playSpeedModifier = this.getRealAnimation().getProperty(StaticAnimationProperty.PLAY_SPEED_MODIFIER).orElse(null);
		
		if (playSpeedModifier != null) {
			playbackSpeed = playSpeedModifier.modify(this, entitypatch, playbackSpeed, 0.0F, playbackSpeed);
		}
		
		float progressionPerTick = playbackSpeed * EpicFightOptions.A_TICK;
		float previousTotalTime = 0.0F;
		boolean insertFirstPose = false;
		
		if (totalTime <= progressionPerTick) {
			convertTimeModifier -= (progressionPerTick - totalTime);
			
			if (!skipFirstPose) {
				previousTotalTime = totalTime;
				insertFirstPose = true;
			}
			
			totalTime = progressionPerTick + 0.001F;
		}
		
		float nextStart = 0.0F;
		
		if (convertTimeModifier < 0.0F) {
			nextStart -= convertTimeModifier;
			dest.startsAt = nextStart;
		}
		
		dest.getTransfroms().clear();
		dest.setTotalTime(totalTime);
		dest.setConnectedAnimations(fromAnimation, this);
		
		Map<String, JointTransform> data1 = pose.getJointTransformData();
		Map<String, JointTransform> data2 = this.getPoseByTime(entitypatch, nextStart, 1.0F).getJointTransformData();
		
		Set<String> joint1 = new HashSet<> (isOnSameLayer ? data1.keySet() : Set.of());
		joint1.removeIf((jointName) -> !fromAnimation.isJointEnabled(entitypatch, jointName));
		Set<String> joint2 = new HashSet<> (data2.keySet());
		joint2.removeIf((jointName) -> !this.isJointEnabled(entitypatch, jointName));
		joint1.addAll(joint2);
		
		if (insertFirstPose) {
			Map<String, JointTransform> firstPose = this.getPoseByTime(entitypatch, 0.0F, 0.0F).getJointTransformData();
			
			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[3];
				keyframes[0] = new Keyframe(0.0F, data1.get(jointName));
				keyframes[1] = new Keyframe(previousTotalTime, firstPose.get(jointName));
				keyframes[2] = new Keyframe(totalTime, data2.get(jointName));
				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		} else {
			for (String jointName : joint1) {
				Keyframe[] keyframes = new Keyframe[2];
				keyframes[0] = new Keyframe(0.0F, data1.get(jointName));
				keyframes[1] = new Keyframe(totalTime, data2.get(jointName));
				TransformSheet sheet = new TransformSheet(keyframes);
				dest.getAnimationClip().addJointTransform(jointName, sheet);
			}
		}
	}
	
	public void putOnPlayer(AnimationPlayer player) {
		player.setPlayAnimation(this);
	}
	
	public void begin(LivingEntityPatch<?> entitypatch) {}
	public void tick(LivingEntityPatch<?> entitypatch) {}
	public void end(LivingEntityPatch<?> entitypatch, DynamicAnimation nextAnimation, boolean isEnd) {}
	public void linkTick(LivingEntityPatch<?> entitypatch, DynamicAnimation linkAnimation) {};
	
	public boolean isJointEnabled(LivingEntityPatch<?> entitypatch, String joint) {
		return this.getTransfroms().containsKey(joint);
	}
	
	@OnlyIn(Dist.CLIENT)
	public BindModifier getBindModifier(LivingEntityPatch<?> entitypatch, String joint) {
		return null;
	}
	
	public EntityState getState(LivingEntityPatch<?> entitypatch, float time) {
		return EntityState.DEFAULT_STATE;
	}
	
	public TypeFlexibleHashMap<StateFactor<?>> getStatesMap(LivingEntityPatch<?> entitypatch, float time) {
		return new TypeFlexibleHashMap<> (false);
	}

	public <T> T getState(StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, float time) {
		return stateFactor.defaultValue();
	}
	
	public abstract AnimationClip getAnimationClip();
	
	public Map<String, TransformSheet> getTransfroms() {
		return this.getAnimationClip().getJointTransforms();
	}
	
	public float getPlaySpeed(LivingEntityPatch<?> entitypatch) {
		return 1.0F;
	}
	
	public TransformSheet getCoord() {
		return this.getTransfroms().get("Root");
	}
	
	public DynamicAnimation getRealAnimation() {
		return this;
	}
	
	public void setTotalTime(float totalTime) {
		this.getAnimationClip().setClipTime(totalTime);
	}
	
	public float getTotalTime() {
		return this.getAnimationClip().getClipTime();
	}
	
	public float getConvertTime() {
		return this.convertTime;
	}
	
	public boolean isRepeat() {
		return this.isRepeat;
	}
	
	public boolean canBePlayedReverse() {
		return false;
	}
	
	public ResourceLocation getRegistryName() {
		return new ResourceLocation(EpicFightMod.MODID, "");
	}
	
	public int getId() {
		return -1;
	}
	
	public <V> Optional<V> getProperty(AnimationProperty<V> propertyType) {
		return Optional.empty();
	}
	
	public boolean isBasicAttackAnimation() {
		return false;
	}

	public boolean isMainFrameAnimation() {
		return false;
	}
	
	public boolean isReboundAnimation() {
		return false;
	}
	
	public boolean isMetaAnimation() {
		return false;
	}
	
	public boolean isClientAnimation() {
		return false;
	}
	
	public boolean isStaticAnimation() {
		return false;
	}
	
	public boolean doesHeadRotFollowEntityHead() {
		return false;
	}
	
	public DynamicAnimation getThis() {
		return this;
	}

	@OnlyIn(Dist.CLIENT)
	public void renderDebugging(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, float playTime, float partialTicks) {
	}
}