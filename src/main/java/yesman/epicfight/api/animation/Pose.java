package yesman.epicfight.api.animation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class Pose {
	public static final Pose EMPTY_POSE = new Pose();
	
	public static Pose interpolatePose(Pose pose1, Pose pose2, float pregression) {
		Pose pose = new Pose();
		
		Set<String> mergedSet = new HashSet<>(pose1.jointTransformData.keySet());
		mergedSet.addAll(pose2.jointTransformData.keySet());
		
		for (String jointName : mergedSet) {
			pose.putJointData(jointName, JointTransform.interpolate(pose1.orElseEmpty(jointName), pose2.orElseEmpty(jointName), pregression));
		}
		
		return pose;
	}
	
	protected final Map<String, JointTransform> jointTransformData;
	
	public Pose() {
		this(Maps.newHashMap());
	}
	
	public Pose(Map<String, JointTransform> jointTransforms) {
		this.jointTransformData = jointTransforms;
	}
	
	public void putJointData(String name, JointTransform transform) {
		this.jointTransformData.put(name, transform);
	}
	
	public Map<String, JointTransform> getJointTransformData() {
		return this.jointTransformData;
	}
	
	public void disableJoint(Predicate<? super Map.Entry<String, JointTransform>> predicate) {
		this.jointTransformData.entrySet().removeIf(predicate);
	}
	
	public void disableAllJoints() {
		this.jointTransformData.clear();
	}
	
	public boolean hasTransform(String jointName) {
		return this.jointTransformData.containsKey(jointName);
	}
	
	public JointTransform get(String jointName) {
		return this.jointTransformData.get(jointName);
	}
	
	public JointTransform orElseEmpty(String jointName) {
		return this.jointTransformData.getOrDefault(jointName, JointTransform.empty());
	}
	
	public JointTransform orElse(String jointName, JointTransform orElse) {
		return this.jointTransformData.getOrDefault(jointName, orElse);
	}
	
	public void forEachEnabledTransforms(BiConsumer<String, JointTransform> task) {
		this.jointTransformData.forEach(task);
	}
	
	public void load(Pose pose, LoadOperation operation) {
		switch (operation) {
		case SET -> {
			this.disableAllJoints();
			pose.forEachEnabledTransforms(this::putJointData);
		}
		case OVERWRITE -> {
			pose.forEachEnabledTransforms(this::putJointData);
		}
		case APPEND_ABSENT -> {
			pose.forEachEnabledTransforms((name, transform) -> {
				if (!this.hasTransform(name)) {
					this.putJointData(name, transform);
				}
			});
		}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Pose: ");
		
		for (Map.Entry<String, JointTransform> entry : this.jointTransformData.entrySet()) {
			sb.append(String.format("%s{%s, %s}, ", entry.getKey(), entry.getValue().translation().toString(), entry.getValue().rotation().toString()) + "\n");
		}
		
		return sb.toString();
	}
	
	public enum LoadOperation {
		SET, OVERWRITE, APPEND_ABSENT
	}
}