package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.model.armature.types.HumanLikeArmature;

public class HumanoidArmature extends Armature implements HumanLikeArmature {
	public final Joint thighR;
	public final Joint legR;
	public final Joint kneeR;
	public final Joint thighL;
	public final Joint legL;
	public final Joint kneeL;
	public final Joint torso;
	public final Joint chest;
	public final Joint head;
	public final Joint shoulderR;
	public final Joint armR;
	public final Joint handR;
	public final Joint toolR;
	public final Joint elbowR;
	public final Joint shoulderL;
	public final Joint armL;
	public final Joint handL;
	public final Joint toolL;
	public final Joint elbowL;
	
	public HumanoidArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.thighR = this.getOrLogException(jointMap, "Thigh_R");
		this.legR = this.getOrLogException(jointMap, "Leg_R");
		this.kneeR = this.getOrLogException(jointMap, "Knee_R");
		this.thighL = this.getOrLogException(jointMap, "Thigh_L");
		this.legL = this.getOrLogException(jointMap, "Leg_L");
		this.kneeL = this.getOrLogException(jointMap, "Knee_L");
		this.torso = this.getOrLogException(jointMap, "Torso");
		this.chest = this.getOrLogException(jointMap, "Chest");
		this.head = this.getOrLogException(jointMap, "Head");
		this.shoulderR = this.getOrLogException(jointMap, "Shoulder_R");
		this.armR = this.getOrLogException(jointMap, "Arm_R");
		this.handR = this.getOrLogException(jointMap, "Hand_R");
		this.toolR = this.getOrLogException(jointMap, "Tool_R");
		this.elbowR = this.getOrLogException(jointMap, "Elbow_R");
		this.shoulderL = this.getOrLogException(jointMap, "Shoulder_L");
		this.armL = this.getOrLogException(jointMap, "Arm_L");
		this.handL = this.getOrLogException(jointMap, "Hand_L");
		this.toolL = this.getOrLogException(jointMap, "Tool_L");
		this.elbowL = this.getOrLogException(jointMap, "Elbow_L");
	}

	@Override
	public Joint leftToolJoint() {
		return this.toolL;
	}

	@Override
	public Joint rightToolJoint() {
		return this.toolR;
	}

	@Override
	public Joint backToolJoint() {
		return this.chest;
	}

	@Override
	public Joint leftHandJoint() {
		return this.handL;
	}

	@Override
	public Joint rightHandJoint() {
		return this.handR;
	}

	@Override
	public Joint leftArmJoint() {
		return this.armL;
	}

	@Override
	public Joint rightArmJoint() {
		return this.armR;
	}

	@Override
	public Joint leftLegJoint() {
		return this.legL;
	}

	@Override
	public Joint rightLegJoint() {
		return this.legR;
	}

	@Override
	public Joint leftThighJoint() {
		return this.thighL;
	}

	@Override
	public Joint rightThighJoint() {
		return this.thighR;
	}

	@Override
	public Joint headJoint() {
		return this.head;
	}
}