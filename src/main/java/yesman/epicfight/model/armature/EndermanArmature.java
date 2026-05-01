package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;

public class EndermanArmature extends HumanoidArmature {
	public final Joint headTop;
	
	public EndermanArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.headTop = this.getOrLogException(jointMap, "Head_Top");
	}
}