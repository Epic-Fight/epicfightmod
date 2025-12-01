package yesman.epicfight.api.utils.math;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import org.joml.Matrix4f;
import yesman.epicfight.api.animation.JointTransform;

public class AnimationTransformEntry {
	private static final String[] BINDING_PRIORITY = {JointTransform.PARENT, JointTransform.JOINT_LOCAL_TRANSFORM, JointTransform.ANIMATION_TRANSFORM, JointTransform.RESULT1, JointTransform.RESULT2};
	private final Map<String, Pair<Matrix4f, MatrixOperation>> matrices = Maps.newHashMap();
	
	public void put(String entryPosition, Matrix4f matrix) {
		this.put(entryPosition, matrix, Matrix4f::mul);
	}
	
	public void put(String entryPosition, Matrix4f matrix, MatrixOperation operation) {
		if (this.matrices.containsKey(entryPosition)) {
			Pair<Matrix4f, MatrixOperation> appliedTransform = this.matrices.get(entryPosition);
			Matrix4f result = appliedTransform.getSecond().mul(appliedTransform.getFirst(), matrix, new Matrix4f());
			this.matrices.put(entryPosition, Pair.of(result, operation));
		} else {
			this.matrices.put(entryPosition, Pair.of(new Matrix4f(matrix), operation));
		}
	}
	
	public Matrix4f getResult() {
		Matrix4f result = new Matrix4f();
		
		for (String entryName : BINDING_PRIORITY) {
			if (this.matrices.containsKey(entryName)) {
				Pair<Matrix4f, MatrixOperation> pair = this.matrices.get(entryName);
				pair.getSecond().mul(result, pair.getFirst(), result);
			}
		}
		
		return result;
	}
}