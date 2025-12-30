package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.model.armature.types.ToolHolderArmature;

public class VexArmature extends Armature implements ToolHolderArmature {
	public final Joint tail;
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
	public final Joint wingL;
	public final Joint wingR;
	
	public VexArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.tail = this.getOrLogException(jointMap, "Tail");
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
		this.wingL = this.getOrLogException(jointMap, "Wing_L");
		this.wingR = this.getOrLogException(jointMap, "Wing_R");
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
}