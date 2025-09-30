package yesman.epicfight.api.physics.ik;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.physics.AbstractSimulator;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.SimulationObject;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator.InverseKinematicsBuilder;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;

public class InverseKinematicsSimulator extends AbstractSimulator<Joint, InverseKinematicsBuilder, InverseKinematicsProvider, InverseKinematicsSimulatable, InverseKinematicsSimulator.InverseKinematicsObject> {
	public static class InverseKinematicsObject implements SimulationObject<InverseKinematicsBuilder, InverseKinematicsProvider, InverseKinematicsSimulatable> {
		private final TransformSheet animation;
		private final BakedInverseKinematicsDefinition ikDefinition;
		private Vec3f destination;
		private float time;
		private float startTime;
		private float totalTime;
		private float dt;
		private boolean isWorking;
		private boolean isTouchingGround;
		
		public InverseKinematicsObject(InverseKinematicsSimulator.InverseKinematicsBuilder builder) {
			this.destination = builder.initPos;
			this.animation = builder.transformSheet;
			this.ikDefinition = builder.ikDefinition;
			this.time = 0.0F;
		}
		

		public boolean isOnWorking() {
			return this.isWorking;
		}
		
		public float getTime(float partialTicks) {
			float curTime = this.time - this.dt * (1.0F - (this.isWorking ? partialTicks : 1.0F));
			return curTime * (this.totalTime - this.startTime) + this.startTime;
		}
		
		public BakedInverseKinematicsDefinition getIKDefinition() {
			return this.ikDefinition;
		}
		
		public void start(Vec3f targetpos, TransformSheet animation, float speed) {
			this.isWorking = true;
			this.time = 0.0F;
			this.destination = targetpos;
			Keyframe[] keyframes = animation.getKeyframes();
			this.startTime = keyframes[0].time();
			this.totalTime = keyframes[keyframes.length - 1].time();
			this.dt = (1.0F / (this.totalTime - this.startTime) * 0.05F) * speed;
			
			if (this.animation != animation) { 
				this.animation.readFrom(animation);
			}
		}
		
		public void newTargetPosition(Vec3f targetpos) {
			Vec3f dv = targetpos.copy().sub(this.destination);
			this.destination = targetpos;
			Keyframe[] keyframes = this.animation.getKeyframes();
			float curTime = this.getTime(1.0F);
			int startFrame = 0;
			
			while (keyframes[startFrame].time() < curTime) {
				startFrame++;
			}
			
			for (int i = startFrame; i < keyframes.length; i++) {
				keyframes[i].transform().translation().add(dv.copy());
			}
		}
		
		public void tick() {
			this.time += this.dt;
			
			if (this.time >= 1.0F) {
				this.isWorking = false;
				this.time = 1.0F;
			}
			
			Keyframe[] keyframes = this.animation.getKeyframes();
			float curTime = this.getTime(1.0F);
			int startFrame = 0;
			
			while (keyframes[startFrame].time() < curTime) {
				startFrame++;
			}
			
			boolean[] touchGround = this.ikDefinition.touchingGround();
			
			if (startFrame >= touchGround.length) {
				this.isTouchingGround = touchGround[touchGround.length - 1];
			} else if (startFrame == 0) {
				this.isTouchingGround = touchGround[0];
			} else {
				this.isTouchingGround = touchGround[startFrame - 1] && touchGround[startFrame];
			}
		}
		
		public Vec3f getTipPosition(float partialTicks) {
			return this.animation.getInterpolatedTranslation(this.getTime(partialTicks));
		}
		
		public JointTransform getTipTransform(float partialTicks) {
			return this.animation.getInterpolatedTransform(this.getTime(partialTicks));
		}
		
		public Vec3f getDestination() {
			return this.destination;
		}
		
		public TransformSheet getAnimation() {
			return this.animation;
		}
		
		public boolean isTouchingGround() {
			return this.isTouchingGround;
		}
	}
	
	public static class InverseKinematicsBuilder extends SimulationObject.SimulationObjectBuilder {
		private Vec3f initPos;
		private TransformSheet transformSheet;
		private BakedInverseKinematicsDefinition ikDefinition;
		
		private InverseKinematicsBuilder(Vec3f initPos, TransformSheet transformSheet, BakedInverseKinematicsDefinition ikDefinition) {
			this.initPos = initPos;
			this.transformSheet = transformSheet;
			this.ikDefinition = ikDefinition;
		}
		
		public static InverseKinematicsBuilder create(Vec3f initpos, TransformSheet transformSheet, BakedInverseKinematicsDefinition ikDefinition) {
			return new InverseKinematicsBuilder(initpos, transformSheet, ikDefinition);
		}
	}
	
	public static record BakedInverseKinematicsDefinition(
		  Joint startJoint
		, Joint endJoint
		, Joint opponentJoint
		, boolean clipAnimation
		, int startFrame
		, int endFrame
		, int initialPoseFrame
		, float rayLeastHeight
		, boolean[] touchingGround
		, List<String> pathToEndJoint
		, Vec3f startPosition
		, Vec3f endPosition
		, Vec3f startToEnd
		, TransformSheet terminalBoneTransform
	) {
	}
	
	public static record InverseKinematicsDefinition(
		  Joint startJoint
		, Joint endJoint
		, Joint opponentJoint
		, boolean clipAnimation
		, int startFrame
		, int endFrame
		, int initialPoseFrame
		, float rayLeastHeight
		, boolean[] touchingGround
	) {
		private InverseKinematicsDefinition(Joint startJoint, Joint endJoint, Joint opponentJoint, IntIntPair clipFrame, float rayLeastHeight, int ikFrame, boolean[] touchingGround) {
			this(
				  startJoint
				, endJoint
				, opponentJoint
				, clipFrame != null
				, clipFrame != null ? clipFrame.firstInt() : -1
				, clipFrame != null ? clipFrame.secondInt() : -1
				, ikFrame
				, rayLeastHeight
				, touchingGround
			);
		}
		
		public static InverseKinematicsDefinition create(Joint startJoint, Joint endJoint, Joint opponentJoint, IntIntPair clipFrame, float rayLeastHeight, int ikFrame, boolean[] touchingGround) {
			return new InverseKinematicsDefinition(startJoint, endJoint, opponentJoint, clipFrame, rayLeastHeight, ikFrame, touchingGround);
		}
		
		public BakedInverseKinematicsDefinition bake(AssetAccessor<? extends Armature> armature, Map<String, TransformSheet> animationClip, boolean correctY, boolean correctZ) {
			Joint joint = armature.get().searchJointByName(this.startJoint.getName());
			Joint.AccessTicket accessor = armature.get().searchPathIndex(joint, this.endJoint.getName()).createAccessTicket(joint);
			List<String> pathToTerminal = Lists.newArrayList();
			pathToTerminal.add(joint.getName());
			
			while (accessor.hasNext()) {
				joint = accessor.next();
				pathToTerminal.add(joint.getName());
			}
			
			Keyframe[] keyframes = animationClip.get(this.endJoint.getName()).getKeyframes();
			Keyframe[] boundTransformKeyframes = new Keyframe[keyframes.length];
			int keyframeLength = animationClip.get(this.endJoint.getName()).getKeyframes().length;
			
			for (int i = 0; i < keyframeLength; i++) {
				Keyframe kf = keyframes[i];
				Pose pose = new Pose();
				
				for (String jointName : animationClip.keySet()) {
					pose.putJointData(jointName, animationClip.get(jointName).getInterpolatedTransform(kf.time()));
				}
				
				OpenMatrix4f boundPoseMatrix = armature.get().getBoundTransformFor(pose, this.endJoint);
				JointTransform boundJointTransform = JointTransform.fromMatrixWithoutScale(boundPoseMatrix);
				boundTransformKeyframes[i] = new Keyframe(kf);
				JointTransform tipTransform = boundTransformKeyframes[i].transform();
				tipTransform.copyFrom(boundJointTransform);
				
				if (correctY || correctZ) {
					JointTransform rootTransform = animationClip.get("Root").getInterpolatedTransform(kf.time());
					Vec3f rootPos = rootTransform.translation();
					float yCorrection = correctY ? -rootPos.z : 0.0F;
					float zCorrection = correctZ ? rootPos.y : 0.0F;
					tipTransform.translation().add(0.0F, yCorrection, zCorrection);
				}
			}
			
			TransformSheet terminalBoneTransform = new TransformSheet(boundTransformKeyframes);
			Vec3f startPos;
			Vec3f endPos;
			
			if (this.clipAnimation) {
				TransformSheet part = terminalBoneTransform.copy(this.startFrame, this.endFrame);
				Keyframe[] partKeyframes = part.getKeyframes();
				startPos = partKeyframes[0].transform().translation();
				endPos = partKeyframes[partKeyframes.length - 1].transform().translation();
			} else {
				startPos = terminalBoneTransform.getKeyframes()[0].transform().translation();
				endPos = startPos;
			}
			
			Vec3f startToEnd = Vec3f.sub(endPos, startPos, null).multiply(-1.0F, 1.0F, -1.0F);
			
			return new BakedInverseKinematicsDefinition(
				  this.startJoint
				, this.endJoint
				, this.opponentJoint
				, this.clipAnimation
				, this.startFrame
				, this.endFrame
				, this.initialPoseFrame
				, this.rayLeastHeight
				, this.touchingGround
				, ImmutableList.copyOf(pathToTerminal)
				, startPos
				, endPos
				, startToEnd
				, terminalBoneTransform
			);
		}
	}
}
