package yesman.epicfight.api.utils.math;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import yesman.epicfight.api.animation.JointTransform;

public class AnimationTransformEntry {
	private static final String[] BINDING_PRIORITY = {JointTransform.PARENT, JointTransform.JOINT_LOCAL_TRANSFORM, JointTransform.ANIMATION_TRANSFORM, JointTransform.RESULT1, JointTransform.RESULT2};
	private final Map<String, Pair<OpenMatrix4f, MatrixOperation>> matrices = Maps.newHashMap();
	
	public void put(String entryPosition, OpenMatrix4f matrix) {
		this.put(entryPosition, matrix, OpenMatrix4f::mul);
	}
	
	public void put(String entryPosition, OpenMatrix4f matrix, MatrixOperation operation) {
		if (this.matrices.containsKey(entryPosition)) {
			Pair<OpenMatrix4f, MatrixOperation> appliedTransform = this.matrices.get(entryPosition);
			OpenMatrix4f result = appliedTransform.getSecond().mul(appliedTransform.getFirst(), matrix, null);
			this.matrices.put(entryPosition, Pair.of(result, operation));
		} else {
			this.matrices.put(entryPosition, Pair.of(new OpenMatrix4f(matrix), operation));
		}
	}
	
	public OpenMatrix4f getResult() {
		OpenMatrix4f result = new OpenMatrix4f();
		
		for (String entryName : BINDING_PRIORITY) {
			if (this.matrices.containsKey(entryName)) {
				Pair<OpenMatrix4f, MatrixOperation> pair = this.matrices.get(entryName);
				pair.getSecond().mul(result, pair.getFirst(), result);
			}
		}
		
		return result;
	}
}