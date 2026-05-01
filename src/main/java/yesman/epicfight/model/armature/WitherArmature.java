package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

public class WitherArmature extends Armature {
	public final Joint headM;
	public final Joint headR;
	public final Joint headL;
	public final Joint torso;
	public final Joint tail;
	
	public WitherArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.headM = this.getOrLogException(jointMap, "Head_M");
		this.headR = this.getOrLogException(jointMap, "Head_R");
		this.headL = this.getOrLogException(jointMap, "Head_L");
		this.torso = this.getOrLogException(jointMap, "Torso");
		this.tail = this.getOrLogException(jointMap, "Tail");
	}
}