package yesman.epicfight.model.armature;

import java.util.Map;

import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.model.Armature;

public class SpiderArmature extends Armature {
	public final Joint stomach;
	public final Joint head;
	public final Joint LL1;
	public final Joint LL2;
	public final Joint LL3;
	public final Joint LL4;
	public final Joint RL1;
	public final Joint RL2;
	public final Joint RL3;
	public final Joint RL4;
		
	public SpiderArmature(String name, int jointNumber, Joint rootJoint, Map<String, Joint> jointMap) {
		super(name, jointNumber, rootJoint, jointMap);
		
		this.stomach = this.getOrLogException(jointMap, "Stomach");
		this.head = this.getOrLogException(jointMap, "Head");
		this.LL1 = this.getOrLogException(jointMap, "LL1");
		this.LL2 = this.getOrLogException(jointMap, "LL2");
		this.LL3 = this.getOrLogException(jointMap, "LL3");
		this.LL4 = this.getOrLogException(jointMap, "LL4");
		this.RL1 = this.getOrLogException(jointMap, "RL1");
		this.RL2 = this.getOrLogException(jointMap, "RL2");
		this.RL3 = this.getOrLogException(jointMap, "RL3");
		this.RL4 = this.getOrLogException(jointMap, "RL4");
	}
}