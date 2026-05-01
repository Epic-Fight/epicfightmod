package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

public class HoglinArmature extends Armature {
	public final Joint head;
	public final Joint thighRF;
	public final Joint legRF;
	public final Joint kneeRF;
	public final Joint thighLF;
	public final Joint legLF;
	public final Joint kneeLF;
	public final Joint thighRB;
	public final Joint legRB;
	public final Joint kneeRB;
	public final Joint thighLB;
	public final Joint legLB;
	public final Joint kneeLB;
	
	public HoglinArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.head = this.getOrLogException(jointMap, "Head");
		this.thighRF = this.getOrLogException(jointMap, "Thigh_RF");
		this.legRF = this.getOrLogException(jointMap, "Leg_RF");
		this.kneeRF = this.getOrLogException(jointMap, "Knee_RF");
		this.thighLF = this.getOrLogException(jointMap, "Thigh_LF");
		this.legLF = this.getOrLogException(jointMap, "Leg_LF");
		this.kneeLF = this.getOrLogException(jointMap, "Knee_LF");
		this.thighRB = this.getOrLogException(jointMap, "Thigh_RB");
		this.legRB = this.getOrLogException(jointMap, "Leg_RB");
		this.kneeRB = this.getOrLogException(jointMap, "Knee_RB");
		this.thighLB = this.getOrLogException(jointMap, "Thigh_LB");
		this.legLB = this.getOrLogException(jointMap, "Leg_LB");
		this.kneeLB = this.getOrLogException(jointMap, "Knee_LB");
	}
}