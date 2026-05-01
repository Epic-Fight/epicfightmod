package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

public class CreeperArmature extends Armature {
	public final Joint torso;
	public final Joint head;
	public final Joint legRF;
	public final Joint legLF;
	public final Joint legRB;
	public final Joint legLB;
		
	public CreeperArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.torso = this.getOrLogException(jointMap, "Torso");
		this.head = this.getOrLogException(jointMap, "Head");
		this.legRF = this.getOrLogException(jointMap, "Leg_RF");
		this.legLF = this.getOrLogException(jointMap, "Leg_LF");
		this.legRB = this.getOrLogException(jointMap, "Leg_RB");
		this.legLB = this.getOrLogException(jointMap, "Leg_LB");
	}
}