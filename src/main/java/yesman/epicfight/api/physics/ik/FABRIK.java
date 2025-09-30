package yesman.epicfight.api.physics.ik;

import java.util.List;

import com.google.common.collect.Lists;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.Vec3f;

import org.joml.Vector3f;
import org.joml.Quaternionf;

public class FABRIK {
	private final Armature armature;
	private final List<Chain> chains = Lists.newArrayList();
	private final Vec3f target = new Vec3f();
	private final Vec3f startPos = new Vec3f();
	private final Pose pose;
	
	public FABRIK(Pose pose, Armature armature, Joint startJoint, Joint endJoint) {
		this.armature = armature;
		this.pose = pose;
		this.addChain(pose, this.armature.searchJointByName(startJoint.getName()), this.armature.searchJointByName(endJoint.getName()));
	}
	
	public void addChain(Pose pose, Joint startJoint, Joint endJoint) {
		OpenMatrix4f boundTransform = this.armature.getBoundTransformFor(pose, startJoint);
		Joint.HierarchicalJointAccessor jointAccessor = this.armature.searchPathIndex(startJoint, endJoint.getName());
		OpenMatrix4f.toTranslationVector(boundTransform, this.startPos);
		this.addChainRecursively(pose, boundTransform, startJoint, jointAccessor.createAccessTicket(startJoint));
	}
	
	private void addChainRecursively(Pose pose, OpenMatrix4f parentTransform, Joint joint, Joint.AccessTicket accessTicket) {
		Joint nextJoint = accessTicket.next();
		JointTransform jt = pose.orElseEmpty(nextJoint.getName());
		OpenMatrix4f result = jt.getAnimationBoundMatrix(nextJoint, parentTransform);
		this.chains.add(new Chain(joint.getName(), parentTransform.toTranslationVector(), result.toTranslationVector()));
		
		if (accessTicket.hasNext()) {
			this.addChainRecursively(pose, result, nextJoint, accessTicket);
		}
	}
	
	public void run(Vec3f target, int iteration) {
		this.target.set(target);
		
		for (int i = 0; i < iteration; i++) {
			this.backward();
			this.forward();
		}
		
		Quaternionf parentQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
		
		for (Chain chain : this.chains) {
			Vector3f tailToHeadM = chain.tailToHead.toMojangVector();
			tailToHeadM.rotate(parentQuaternion);
			Vec3f tailToHead = Vec3f.fromMojangVector(tailToHeadM);
			Vec3f tailToNewHead = chain.head.copy().sub(chain.tail);
			Vec3f axis = Vec3f.cross(tailToNewHead, tailToHead, null).normalize();
			float radian = Vec3f.getAngleBetween(tailToNewHead, tailToHead);
			Quaternionf rotationQuat = QuaternionUtils.rotation(axis.toMojangVector(), radian);
			parentQuaternion = QuaternionUtils.rotation(axis.scale(-1.0F).toMojangVector(), radian);
			
			JointTransform jt = this.pose.orElseEmpty(chain.jointName);
			jt.frontResult(JointTransform.rotation(rotationQuat), OpenMatrix4f::mulAsOriginInverse);
		}
	}
	
	private void forward() {
		int chainNum = this.chains.size();
		Vec3f newTailPos = new Vec3f();
		newTailPos.set(this.startPos);
		
		for (int i = 0; i < chainNum; i++) {
			Chain chain = this.chains.get(i);
			chain.forwardAlign(newTailPos);
			newTailPos.set(chain.head);
		}
	}
	
	private void backward() {
		int chainNum = this.chains.size();
		Vec3f newHeadPos = new Vec3f();
		newHeadPos.set(this.target);
		
		for (int i = chainNum - 1; i >= 0; i--) {
			Chain chain = this.chains.get(i);
			chain.backwardAlign(newHeadPos);
			newHeadPos.set(chain.tail);
		}
	}
	
	public List<Vec3f> getChainingPosition() {
		List<Vec3f> list = Lists.newArrayList();
		for (Chain chain : this.chains) {
			list.add(chain.tail);
		}
		
		list.add(this.chains.get(this.chains.size() - 1).head);
		return list;
	}
	
	class Chain {
		final String jointName;
		float length;
		Vec3f tail;
		Vec3f head;
		Vec3f tailToHead;
		
		Chain(String jointName, Vec3f tail, Vec3f head) {
			this.jointName = jointName;
			this.tail = tail;
			this.head = head;
			this.tailToHead = head.copy().sub(tail);
			this.length = (float)Math.sqrt(tail.distanceSqr(head));
		}
		
		public void forwardAlign(Vec3f newHeadPos) {
			this.correct(this.tail, this.head, newHeadPos);
		}
		
		public void backwardAlign(Vec3f newHeadPos) {
			this.correct(this.head, this.tail, newHeadPos);
		}
		
		private void correct(Vec3f start, Vec3f end, Vec3f newpos) {
			start.set(newpos);
			Vec3f startToEnd = end.sub(start);
			float newLength = startToEnd.length();
			float lengthRatio = this.length / newLength;
			Vec3f startToEndScaled = startToEnd.copy().scale(lengthRatio);
			end.set(start.copy().add(startToEndScaled));
		}
		
		public void init(Vec3f tail, Vec3f head) {
			this.tail.set(tail);
			this.head.set(head);
			this.tailToHead.set(head.copy().sub(tail));
			this.length = (float)Math.sqrt(tail.distanceSqr(head));
		}
	}
}