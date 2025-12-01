package yesman.epicfight.api.animation;

import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.common.collect.Maps;

import net.minecraft.util.Mth;
import yesman.epicfight.api.utils.math.AnimationTransformEntry;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.MatrixOperation;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.api.utils.math.joml.VectorUtils;


public class JointTransform {
	public static final String ANIMATION_TRANSFORM = "animation_transform";
	public static final String JOINT_LOCAL_TRANSFORM = "joint_local_transform";
	public static final String PARENT = "parent";
	public static final String RESULT1 = "front_result";
	public static final String RESULT2 = "overwrite_rotation";
	
	public static class TransformEntry {
		public final MatrixOperation multiplyFunction;
		public final JointTransform transform;
		
		public TransformEntry(MatrixOperation multiplyFunction, JointTransform transform) {
			this.multiplyFunction = multiplyFunction;
			this.transform = transform;
		}
	}
	
	private final Map<String, TransformEntry> entries = Maps.newHashMap();
	private final Vector3f translation;
	private final Vector3f scale;
	private final Quaternionf rotation;

	public JointTransform(Vector3f translation, Quaternionf rotation, Vector3f scale) {
		this.translation = translation;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	public Vector3f translation() {
		return this.translation;
	}

	public Quaternionf rotation() {
		return this.rotation;
	}
	
	public Vector3f scale() {
		return this.scale;
	}
	
	public void clearTransform() {
		this.translation.set(0.0F, 0.0F, 0.0F);
		this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
		this.scale.set(1.0F, 1.0F, 1.0F);
	}
	
	public JointTransform copy() {
		return JointTransform.empty().copyFrom(this);
	}
	
	public JointTransform copyFrom(JointTransform jt) {
		Vector3f newV = jt.translation();
		Quaternionf newQ = jt.rotation();
        this.translation.set(newV);
		this.rotation.set(newQ);
		this.scale.set(jt.scale);
		this.entries.putAll(jt.entries);
		
		return this;
	}
	
	public void jointLocal(JointTransform transform, MatrixOperation multiplyFunction) {
		this.entries.put(JOINT_LOCAL_TRANSFORM, new TransformEntry(multiplyFunction, this.mergeIfExist(JOINT_LOCAL_TRANSFORM, transform)));
	}
	
	public void parent(JointTransform transform, MatrixOperation multiplyFunction) {
		this.entries.put(PARENT, new TransformEntry(multiplyFunction, this.mergeIfExist(PARENT, transform)));
	}
	
	public void animationTransform(JointTransform transform, MatrixOperation multiplyFunction) {
		this.entries.put(ANIMATION_TRANSFORM, new TransformEntry(multiplyFunction, this.mergeIfExist(ANIMATION_TRANSFORM, transform)));
	}
	
	public void frontResult(JointTransform transform, MatrixOperation multiplyFunction) {
		this.entries.put(RESULT1, new TransformEntry(multiplyFunction, this.mergeIfExist(RESULT1, transform)));
	}
	
	public void overwriteRotation(JointTransform transform) {
		this.entries.put(RESULT2, new TransformEntry(Matrix4f::mul, this.mergeIfExist(RESULT2, transform)));
	}
	
	public JointTransform mergeIfExist(String entryName, JointTransform transform) {
		if (this.entries.containsKey(entryName)) {
			TransformEntry transformEntry = this.entries.get(entryName);
			return JointTransform.mul(transform, transformEntry.transform, transformEntry.multiplyFunction);
		}
		
		return transform;
	}
	
	public Matrix4f getAnimationBoundMatrix(Joint joint, Matrix4f parentTransform) {
		AnimationTransformEntry animationTransformEntry = new AnimationTransformEntry();
		
		for (Map.Entry<String, TransformEntry> entry : this.entries.entrySet()) {
			animationTransformEntry.put(entry.getKey(), entry.getValue().transform.toMatrix(), entry.getValue().multiplyFunction);
		}
		
		animationTransformEntry.put(ANIMATION_TRANSFORM, this.toMatrix(), Matrix4fUtils::mulBoth);
		animationTransformEntry.put(JOINT_LOCAL_TRANSFORM, joint.getLocalTransform());
		animationTransformEntry.put(PARENT, parentTransform);
		
		return animationTransformEntry.getResult();
	}
	
	public Matrix4f toMatrix() {
		return new Matrix4f().translate(this.translation).mul(new Matrix4f().rotate(this.rotation)).scale(this.scale);
	}
	
	@Override
	public String toString() {
		return String.format("translation:%s, rotation:%s, scale:%s %d entries ", this.translation, this.rotation, this.scale, this.entries.size());
	}
	
	public static JointTransform interpolateTransform(JointTransform prev, JointTransform next, float progression, JointTransform dest) {
		if (dest == null) {
			dest = JointTransform.empty();
		}
		
		VectorUtils.lerp(prev.translation, next.translation, progression, dest.translation);
		MathUtils.lerpQuaternion(prev.rotation, next.rotation, progression, dest.rotation);
		VectorUtils.lerp(prev.scale, next.scale, progression, dest.scale);
		
		return dest;
	}
	
	public static JointTransform interpolate(JointTransform prev, JointTransform next, float progression) {
		return interpolate(prev, next, progression, null);
	}
	
	public static JointTransform interpolate(JointTransform prev, JointTransform next, float progression, JointTransform dest) {
		if (dest == null) {
			dest = JointTransform.empty();
		}
		
		if (prev == null || next == null) {
			dest.clearTransform();
			return dest;
		}
		
		progression = Mth.clamp(progression, 0.0F, 1.0F);
		interpolateTransform(prev, next, progression, dest);
		dest.entries.clear();
		
		for (Map.Entry<String, TransformEntry> entry : prev.entries.entrySet()) {
			JointTransform transform = next.entries.containsKey(entry.getKey()) ? next.entries.get(entry.getKey()).transform : JointTransform.empty();
			dest.entries.put(entry.getKey(), new TransformEntry(entry.getValue().multiplyFunction, interpolateTransform(entry.getValue().transform, transform, progression, null)));
		}
		
		for (Map.Entry<String, TransformEntry> entry : next.entries.entrySet()) {
			if (!dest.entries.containsKey(entry.getKey())) {
				dest.entries.put(entry.getKey(), new TransformEntry(entry.getValue().multiplyFunction, interpolateTransform(JointTransform.empty(), entry.getValue().transform, progression, null)));
			}
		}
		
		return dest;
	}
	
	public static JointTransform fromMatrixWithoutScale(Matrix4f matrix) {
		return new JointTransform(matrix.getTranslation(new Vector3f()), matrix.getNormalizedRotation(new Quaternionf()), new Vector3f(1.0F, 1.0F, 1.0F));
	}
	
	public static JointTransform translation(Vector3f vec) {
		return JointTransform.translationRotation(vec, new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F));
	}
	
	public static JointTransform rotation(Quaternionf quat) {
		return JointTransform.translationRotation(new Vector3f(0.0F, 0.0F, 0.0F), quat);
	}
	
	public static JointTransform scale(Vector3f vec) {
		return new JointTransform(new Vector3f(0.0F, 0.0F, 0.0F), new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F), vec);
	}
	
	public static JointTransform fromMatrix(Matrix4f matrix) {
		return new JointTransform(matrix.getTranslation(new Vector3f()), matrix.getNormalizedRotation(new Quaternionf()), matrix.getScale(new Vector3f()));
	}
	
	public static JointTransform translationRotation(Vector3f vec, Quaternionf quat) {
		return new JointTransform(vec, quat, new Vector3f(1.0F, 1.0F, 1.0F));
	}
	
	public static JointTransform mul(JointTransform left, JointTransform right, MatrixOperation operation) {
		return JointTransform.fromMatrix(operation.mul(left.toMatrix(), right.toMatrix(), new Matrix4f()));
	}
	
	public static JointTransform fromPrimitives(float locX, float locY, float locZ, float quatX, float quatY, float quatZ, float quatW, float scaX, float scaY, float scaZ) {
		return new JointTransform(new Vector3f(locX, locY, locZ), new Quaternionf(quatX, quatY, quatZ, quatW), new Vector3f(scaX, scaY, scaZ));
	}
	
	public static JointTransform empty() {
		return new JointTransform(new Vector3f(0.0F, 0.0F, 0.0F), new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F), new Vector3f(1.0F, 1.0F, 1.0F));
	}
}