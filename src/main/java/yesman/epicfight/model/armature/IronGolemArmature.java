package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

public class IronGolemArmature extends Armature {
	public final Joint chest;
	public final Joint head;
	public final Joint LA1;
	public final Joint LA2;
	public final Joint LA3;
	public final Joint LA4;
	public final Joint RA1;
	public final Joint RA2;
	public final Joint RA3;
	public final Joint RA4;
	public final Joint LR1;
	public final Joint LR2;
	public final Joint RR1;
	public final Joint RR2;
	
	public IronGolemArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.chest = this.getOrLogException(jointMap, "Chest");
		this.head = this.getOrLogException(jointMap, "Head");
		this.LA1 = this.getOrLogException(jointMap, "LA1");
		this.LA2 = this.getOrLogException(jointMap, "LA2");
		this.LA3 = this.getOrLogException(jointMap, "LA3");
		this.LA4 = this.getOrLogException(jointMap, "LA4");
		this.RA1 = this.getOrLogException(jointMap, "RA1");
		this.RA2 = this.getOrLogException(jointMap, "RA2");
		this.RA3 = this.getOrLogException(jointMap, "RA3");
		this.RA4 = this.getOrLogException(jointMap, "RA4");
		this.LR1 = this.getOrLogException(jointMap, "LR1");
		this.LR2 = this.getOrLogException(jointMap, "LR2");
		this.RR1 = this.getOrLogException(jointMap, "RR1");
		this.RR2 = this.getOrLogException(jointMap, "RR2");
	}
}