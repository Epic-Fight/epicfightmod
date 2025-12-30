package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

public class RavagerArmature extends Armature {
	public final Joint torso;
	public final Joint headRoot;
	public final Joint head;
	public final Joint earL;
	public final Joint earR;
	public final Joint chin;
	public final Joint legRF;
	public final Joint footRF;
	public final Joint legRF_F;
	public final Joint legLF;
	public final Joint footLF;
	public final Joint legLF_F;
	public final Joint legRB;
	public final Joint footRB;
	public final Joint legRB_F;
	public final Joint legLB;
	public final Joint footLB;
	public final Joint legLB_F;
	
	public RavagerArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.torso = this.getOrLogException(jointMap, "Torso");
		this.headRoot = this.getOrLogException(jointMap, "Head_Root");
		this.head = this.getOrLogException(jointMap, "Head");
		this.earL = this.getOrLogException(jointMap, "Ear_L");
		this.earR = this.getOrLogException(jointMap, "Ear_R");
		this.chin = this.getOrLogException(jointMap, "Chin");
		this.legRF = this.getOrLogException(jointMap, "Leg_RF");
		this.footRF = this.getOrLogException(jointMap, "Foot_RF");
		this.legRF_F = this.getOrLogException(jointMap, "Leg_RF_F");
		this.legLF = this.getOrLogException(jointMap, "Leg_LF");
		this.footLF = this.getOrLogException(jointMap, "Foot_LF");
		this.legLF_F = this.getOrLogException(jointMap, "Leg_LF_F");
		this.legRB = this.getOrLogException(jointMap, "Leg_RB");
		this.footRB = this.getOrLogException(jointMap, "Foot_RB");
		this.legRB_F = this.getOrLogException(jointMap, "Leg_RB_F");
		this.legLB = this.getOrLogException(jointMap, "Leg_LB");
		this.footLB = this.getOrLogException(jointMap, "Foot_LB");
		this.legLB_F = this.getOrLogException(jointMap, "Leg_LB_F");
	}
}