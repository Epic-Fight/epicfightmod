package yesman.epicfight.api.physics.ik;

import java.util.List;

import com.google.common.collect.Lists;

import org.joml.Matrix4f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.QuaternionUtils;

import org.joml.Vector3f;
import org.joml.Quaternionf;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.api.utils.math.joml.VectorUtils;

public class FABRIK {
	private final Armature armature;
	private final List<Chain> chains = Lists.newArrayList();
	private final Vector3f target = new Vector3f();
	private final Vector3f startPos = new Vector3f();
	private final Pose pose;
	
	public FABRIK(Pose pose, Armature armature, Joint startJoint, Joint endJoint) {
		this.armature = armature;
		this.pose = pose;
		this.addChain(pose, this.armature.searchJointByName(startJoint.getName()), this.armature.searchJointByName(endJoint.getName()));
	}
	
	public void addChain(Pose pose, Joint startJoint, Joint endJoint) {
		Matrix4f boundTransform = this.armature.getBoundTransformFor(pose, startJoint);
		Joint.HierarchicalJointAccessor jointAccessor = this.armature.searchPathIndex(startJoint, endJoint.getName());
		boundTransform.getTranslation(this.startPos);
		this.addChainRecursively(pose, boundTransform, startJoint, jointAccessor.createAccessTicket(startJoint));
	}
	
	private void addChainRecursively(Pose pose, Matrix4f parentTransform, Joint joint, Joint.AccessTicket accessTicket) {
		Joint nextJoint = accessTicket.next();
		JointTransform jt = pose.orElseEmpty(nextJoint.getName());
		Matrix4f result = jt.getAnimationBoundMatrix(nextJoint, parentTransform);
		this.chains.add(new Chain(joint.getName(), parentTransform.getTranslation(new Vector3f()), result.getTranslation(new Vector3f())));
		
		if (accessTicket.hasNext()) {
			this.addChainRecursively(pose, result, nextJoint, accessTicket);
		}
	}
	
	public void run(Vector3f target, int iteration) {
		this.target.set(target);
		
		for (int i = 0; i < iteration; i++) {
			this.backward();
			this.forward();
		}
		
		Quaternionf parentQuaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
		
		for (Chain chain : this.chains) {
			Vector3f tailToHeadM = chain.tailToHead;
			tailToHeadM.rotate(parentQuaternion);
            Vector3f tailToNewHead = new Vector3f(chain.head).sub(chain.tail);
			Vector3f axis = tailToNewHead.cross(tailToHeadM, new Vector3f()).normalize();
			float radian = (float) VectorUtils.getAngleBetween(tailToNewHead, tailToHeadM);
			Quaternionf rotationQuat = QuaternionUtils.rotation(axis, radian);
			parentQuaternion = QuaternionUtils.rotation(axis.mul(-1.0F), radian);
			
			JointTransform jt = this.pose.orElseEmpty(chain.jointName);
			jt.frontResult(JointTransform.rotation(rotationQuat), Matrix4fUtils::mulAsOriginInverse);
		}
	}
	
	private void forward() {
		int chainNum = this.chains.size();
		Vector3f newTailPos = new Vector3f();
		newTailPos.set(this.startPos);
		
		for (int i = 0; i < chainNum; i++) {
			Chain chain = this.chains.get(i);
			chain.forwardAlign(newTailPos);
			newTailPos.set(chain.head);
		}
	}
	
	private void backward() {
		int chainNum = this.chains.size();
		Vector3f newHeadPos = new Vector3f();
		newHeadPos.set(this.target);
		
		for (int i = chainNum - 1; i >= 0; i--) {
			Chain chain = this.chains.get(i);
			chain.backwardAlign(newHeadPos);
			newHeadPos.set(chain.tail);
		}
	}
	
	public List<Vector3f> getChainingPosition() {
		List<Vector3f> list = Lists.newArrayList();
		for (Chain chain : this.chains) {
			list.add(chain.tail);
		}
		
		list.add(this.chains.get(this.chains.size() - 1).head);
		return list;
	}
	
	class Chain {
		final String jointName;
		float length;
		Vector3f tail;
		Vector3f head;
		Vector3f tailToHead;
		
		Chain(String jointName, Vector3f tail, Vector3f head) {
			this.jointName = jointName;
			this.tail = tail;
			this.head = head;
			this.tailToHead = new Vector3f(head).sub(tail);
			this.length = (float)Math.sqrt(tail.distanceSquared(head));
		}
		
		public void forwardAlign(Vector3f newHeadPos) {
			this.correct(this.tail, this.head, newHeadPos);
		}
		
		public void backwardAlign(Vector3f newHeadPos) {
			this.correct(this.head, this.tail, newHeadPos);
		}
		
		private void correct(Vector3f start, Vector3f end, Vector3f newpos) {
			start.set(newpos);
			Vector3f startToEnd = end.sub(start);
			float newLength = startToEnd.length();
			float lengthRatio = this.length / newLength;
			Vector3f startToEndScaled = new Vector3f(startToEnd).mul(lengthRatio);
			end.set(new Vector3f(start).add(startToEndScaled));
		}
		
		public void init(Vector3f tail, Vector3f head) {
			this.tail.set(tail);
			this.head.set(head);
			this.tailToHead.set(new Vector3f(head).sub(tail));
			this.length = (float)Math.sqrt(tail.distanceSquared(head));
		}
	}
}