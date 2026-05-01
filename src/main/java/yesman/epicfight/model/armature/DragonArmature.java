package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

public class DragonArmature extends Armature {
	public final Joint torso;
	public final Joint head;
	public final Joint upperMouth;
	public final Joint lowerMouth;
	public final Joint legFrontR1;
	public final Joint legFrontR2;
	public final Joint legFrontR3;
	public final Joint legFrontL1;
	public final Joint legFrontL2;
	public final Joint legFrontL3;
	public final Joint legBackR1;
	public final Joint legBackR2;
	public final Joint legBackR3;
	public final Joint legBackL1;
	public final Joint legBackL2;
	public final Joint legBackL3;
	public final Joint wingL1;
	public final Joint wingL2;
	public final Joint wingR1;
	public final Joint wingR2;
	public final Joint neck1;
	public final Joint neck2;
	public final Joint neck3;
	public final Joint neck4;
	public final Joint neck5;
	public final Joint tail1;
	public final Joint tail2;
	public final Joint tail3;
	public final Joint tail4;
	public final Joint tail5;
	public final Joint tail6;
	public final Joint tail7;
	public final Joint tail8;
	public final Joint tail9;
	public final Joint tail10;
	public final Joint tail11;
	public final Joint tail12;
		
	public DragonArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.torso = this.getOrLogException(jointMap, "Torso");
		this.head = this.getOrLogException(jointMap, "Head");
		this.upperMouth = this.getOrLogException(jointMap, "Mouth_Upper");
		this.lowerMouth = this.getOrLogException(jointMap, "Mouth_Lower");
		this.legFrontR1 = this.getOrLogException(jointMap, "Leg_Front_R1");
		this.legFrontR2 = this.getOrLogException(jointMap, "Leg_Front_R2");
		this.legFrontR3 = this.getOrLogException(jointMap, "Leg_Front_R3");
		this.legFrontL1 = this.getOrLogException(jointMap, "Leg_Front_L1");
		this.legFrontL2 = this.getOrLogException(jointMap, "Leg_Front_L2");
		this.legFrontL3 = this.getOrLogException(jointMap, "Leg_Front_L3");
		this.legBackR1 = this.getOrLogException(jointMap, "Leg_Back_R1");
		this.legBackR2 = this.getOrLogException(jointMap, "Leg_Back_R2");
		this.legBackR3 = this.getOrLogException(jointMap, "Leg_Back_R3");
		this.legBackL1 = this.getOrLogException(jointMap, "Leg_Back_L1");
		this.legBackL2 = this.getOrLogException(jointMap, "Leg_Back_L2");
		this.legBackL3 = this.getOrLogException(jointMap, "Leg_Back_L3");
		this.wingL1 = this.getOrLogException(jointMap, "Wing_L1");
		this.wingL2 = this.getOrLogException(jointMap, "Wing_L2");
		this.wingR1 = this.getOrLogException(jointMap, "Wing_R1");
		this.wingR2 = this.getOrLogException(jointMap, "Wing_R2");
		this.neck1 = this.getOrLogException(jointMap, "Neck1");
		this.neck2 = this.getOrLogException(jointMap, "Neck2");
		this.neck3 = this.getOrLogException(jointMap, "Neck3");
		this.neck4 = this.getOrLogException(jointMap, "Neck4");
		this.neck5 = this.getOrLogException(jointMap, "Neck5");
		this.tail1 = this.getOrLogException(jointMap, "Tail1");
		this.tail2 = this.getOrLogException(jointMap, "Tail2");
		this.tail3 = this.getOrLogException(jointMap, "Tail3");
		this.tail4 = this.getOrLogException(jointMap, "Tail4");
		this.tail5 = this.getOrLogException(jointMap, "Tail5");
		this.tail6 = this.getOrLogException(jointMap, "Tail6");
		this.tail7 = this.getOrLogException(jointMap, "Tail7");
		this.tail8 = this.getOrLogException(jointMap, "Tail8");
		this.tail9 = this.getOrLogException(jointMap, "Tail9");
		this.tail10 = this.getOrLogException(jointMap, "Tail10");
		this.tail11 = this.getOrLogException(jointMap, "Tail11");
		this.tail12 = this.getOrLogException(jointMap, "Tail12");
	}
}