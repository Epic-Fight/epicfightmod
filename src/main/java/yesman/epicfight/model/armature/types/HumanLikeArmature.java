package yesman.epicfight.model.armature.types;

import yesman.epicfight.api.animation.Joint;

/**
 * This class is not being used by Epic Fight, but is left to meet various purposes of developers
 * Also presents developers which joints are necessary when an armature would be Human-like
 */
public interface HumanLikeArmature extends ToolHolderArmature {
	public Joint leftHandJoint();
	public Joint rightHandJoint();
	public Joint leftArmJoint();
	public Joint rightArmJoint();
	public Joint leftLegJoint();
	public Joint rightLegJoint();
	public Joint leftThighJoint();
	public Joint rightThighJoint();
	public Joint headJoint();
}
