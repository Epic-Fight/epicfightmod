package yesman.epicfight.api.client.animation.property;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class JointMask {
	@OnlyIn(Dist.CLIENT)
	@FunctionalInterface
	public interface BindModifier {
		public void modify(LivingEntityPatch<?> entitypatch, Pose baseLayerPose, Pose resultPose, LivingMotion livingMotion, JointMaskEntry wholeEntry, Layer.Priority priority, Joint joint, Map<Layer.Priority, Pair<AssetAccessor<? extends DynamicAnimation>, Pose>> poses);
	}
	
	public static final BindModifier KEEP_CHILD_LOCROT = (entitypatch, baseLayerPose, result, livingMotion, wholeEntry, priority, joint, poses) -> {
		Pose currentPose = poses.get(priority).getSecond();
		JointTransform lowestTransform = baseLayerPose.orElseEmpty(joint.getName());
		JointTransform currentTransform = currentPose.orElseEmpty(joint.getName());
		result.orElseEmpty(joint.getName()).translation().y = lowestTransform.translation().y;

		Matrix4f lowestMatrix = lowestTransform.toMatrix();
		Matrix4f currentMatrix = currentTransform.toMatrix();
		Matrix4f currentToLowest = currentMatrix.invert(new Matrix4f()).mul(lowestMatrix);
		
		for (Joint subJoint : joint.getSubJoints()) {
			if (wholeEntry.isMasked(livingMotion, subJoint.getName())) {
				Matrix4f lowestLocalTransform = joint.getLocalTransform().mul(lowestMatrix, new Matrix4f());
				Matrix4f currentLocalTransform = joint.getLocalTransform().mul(currentMatrix, new Matrix4f());
				Matrix4f childTransform = subJoint.getLocalTransform().mul(result.orElseEmpty(subJoint.getName()).toMatrix(), new Matrix4f());
				Matrix4f lowestFinal = lowestLocalTransform.mul(childTransform, new Matrix4f());
				Matrix4f currentFinal = currentLocalTransform.mul(lowestFinal, new Matrix4f());
				Vector3f vec = new Vector3f((currentFinal.m30() - lowestFinal.m30()) * 0.5F, currentFinal.m31() - lowestFinal.m31(), currentFinal.m32() - lowestFinal.m32());
				JointTransform jt = result.orElseEmpty(subJoint.getName());
				jt.parent(JointTransform.translation(vec), Matrix4f::mul);
				jt.jointLocal(JointTransform.fromMatrixWithoutScale(currentToLowest), Matrix4fUtils::mulBoth);
			}
		}
	};
	
	public static JointMask of(String jointName, BindModifier bindModifier) {
		return new JointMask(jointName, bindModifier);
	}
	
	public static JointMask of(String jointName) {
		return new JointMask(jointName, null);
	}
	
	private final String jointName;
	private final BindModifier bindModifier;
	
	private JointMask(String jointName, BindModifier bindModifier) {
		this.jointName = jointName;
		this.bindModifier = bindModifier;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class JointMaskSet {
		final Map<String, BindModifier> masks = Maps.newHashMap();
		
		public boolean contains(String name) {
			return this.masks.containsKey(name);
		}
		
		public BindModifier getBindModifier(String jointName) {
			return this.masks.get(jointName);
		}
		
		public static JointMaskSet of(JointMask... masks) {
			JointMaskSet jointMaskSet = new JointMaskSet();
			
			for (JointMask jointMask : masks) {
				jointMaskSet.masks.put(jointMask.jointName, jointMask.bindModifier);
			}
			
			return jointMaskSet;
		}
		
		public static JointMaskSet of(Set<JointMask> jointMasks) {
			JointMaskSet jointMaskSet = new JointMaskSet();
			
			for (JointMask jointMask : jointMasks) {
				jointMaskSet.masks.put(jointMask.jointName, jointMask.bindModifier);
			}
			
			return jointMaskSet;
		}
	}
}