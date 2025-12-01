package yesman.epicfight.api.physics.ik;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.SimulationProvider;
import yesman.epicfight.api.utils.math.QuaternionUtils;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;

public interface InverseKinematicsProvider extends SimulationProvider<InverseKinematicsSimulatable, InverseKinematicsSimulator.InverseKinematicsObject, InverseKinematicsSimulator.InverseKinematicsBuilder, InverseKinematicsProvider> {
	default TransformSheet clipAnimation(TransformSheet transformSheet, InverseKinematicsSimulator.BakedInverseKinematicsDefinition ikDefinition) {
		if (ikDefinition.clipAnimation()) {
			return transformSheet.copy(ikDefinition.startFrame(), ikDefinition.endFrame());
		} else {
			return transformSheet.getFirstFrame();
		}
	}
	
	default void startPartAnimation(InverseKinematicsSimulator.BakedInverseKinematicsDefinition bakedIKDefinition, InverseKinematicsSimulator.InverseKinematicsObject ikObject, TransformSheet partAnimation, Vector3f targetpos) {
		Vector3f footpos = ikObject.getTipPosition(1.0F);
		Vector3f worldStartToEnd = new Vector3f(targetpos).sub(footpos);
		partAnimation.correctAnimationByNewPosition(bakedIKDefinition.startPosition(), bakedIKDefinition.startToEnd(), footpos, worldStartToEnd);
		ikObject.start(targetpos, partAnimation, 1.0F);
	}
	
	default void startSimple(InverseKinematicsSimulator.InverseKinematicsObject ikObject) {
		ikObject.start(new Vector3f(), ikObject.getAnimation(), 1.0F);
	}
	
	default Vector3f getRayCastedTipPosition(InverseKinematicsSimulatable ikSimulatable, Vector3f clipStart, Matrix4f toWorldCoord, float maxYDown, float leastHeight) {
		Vector3f clipStartWorld = Matrix4fUtils.transform3v(toWorldCoord, clipStart, null);
		BlockHitResult clipResult = ikSimulatable.toEntity().level().clip(
			new ClipContext(
				  new Vec3(clipStartWorld.x, clipStartWorld.y, clipStartWorld.z)
				, new Vec3(clipStartWorld.x, clipStartWorld.y - maxYDown, clipStartWorld.z)
				, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, ikSimulatable.toEntity()
			)
		);

		float dy = (clipResult.getType() != HitResult.Type.MISS) ? clipStartWorld.y - clipResult.getBlockPos().getY() - 1 : maxYDown;

		return new Vector3f(clipStartWorld.x, clipStartWorld.y - dy + leastHeight, clipStartWorld.z);
	}
	
	default void correctRootRotation(JointTransform rootTransform, EnderDragonPatch enderdragonpatch, float partialTicks) {
		float xRoot = enderdragonpatch.getRootXRotO() + (enderdragonpatch.getRootXRot() - enderdragonpatch.getRootXRotO()) * partialTicks;
		float zRoot = enderdragonpatch.getRootZRotO() + (enderdragonpatch.getRootZRot() - enderdragonpatch.getRootZRotO()) * partialTicks;
		Quaternionf quat = QuaternionUtils.ZP.rotationDegrees(zRoot);
		quat.mul(QuaternionUtils.XP.rotationDegrees(-xRoot));

		rootTransform.frontResult(JointTransform.rotation(quat), Matrix4fUtils::mulAsOriginInverse);
	}
	
	default void applyFabrikToJoint(Vector3f recalculatedPosition, Pose pose, Armature armature, Joint startJoint, Joint endJoint, Quaternionf tipRotation) {
		FABRIK fabrik = new FABRIK(pose, armature, startJoint, endJoint);
    	fabrik.run(recalculatedPosition, 10);
    	Matrix4f tipRotationMatrix = new Matrix4f().rotation(tipRotation);
    	Matrix4f animRotation = armature.getBoundTransformFor(pose, endJoint).setTranslation(0, 0, 0);
    	Matrix4f animToTipRotation = Matrix4fUtils.mulBoth(animRotation.invert(new Matrix4f()), tipRotationMatrix, new Matrix4f());
    	pose.orElseEmpty(endJoint.getName()).overwriteRotation(JointTransform.fromMatrixWithoutScale(animToTipRotation));
	}
}
