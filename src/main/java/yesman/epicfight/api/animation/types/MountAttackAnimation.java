package yesman.epicfight.api.animation.types;

import org.joml.Vector3f;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class MountAttackAnimation extends AttackAnimation {
	public MountAttackAnimation(float convertTime, float antic, float preDelay, float contact, float recovery, Collider collider, Joint colliderJoint, AnimationAccessor<? extends MountAttackAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		super(convertTime, antic, preDelay, contact, recovery, collider, colliderJoint, accessor, armature);
	}
	
	protected Vector3f getCoordVector(LivingEntityPatch<?> entitypatch) {
		return new Vector3f(0, 0, 0);
	}
}
