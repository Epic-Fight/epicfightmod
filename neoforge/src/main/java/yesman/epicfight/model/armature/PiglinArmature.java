package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;

public class PiglinArmature extends HumanoidArmature {
	public final Joint earL;
	public final Joint earR;
	
	public PiglinArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.earL = this.getOrLogException(jointMap, "Ear_L");
		this.earR = this.getOrLogException(jointMap, "Ear_R");
	}
}