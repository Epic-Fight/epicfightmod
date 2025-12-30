package yesman.epicfight.api.client.model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import yesman.epicfight.api.client.model.Meshes.MeshAccessor;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator.ClothObject.ClothPart.ConstraintType;
import yesman.epicfight.api.physics.SimulationProvider;

public interface SoftBodyTranslatable extends SimulationProvider<ClothSimulatable, ClothSimulator.ClothObject, ClothSimulator.ClothObjectBuilder, SoftBodyTranslatable> {
	public static final List<ClothSimulatable> TRACKING_SIMULATION_SUBJECTS = Lists.newArrayList();
	
	default boolean canStartSoftBodySimulation() {
		return this.getSoftBodySimulationInfo() != null;
	}
	
	void putSoftBodySimulationInfo(Map<String, ClothSimulationInfo> sofyBodySimulationInfo);
	
	Map<String, ClothSimulationInfo> getSoftBodySimulationInfo();
	
	default StaticMesh<?> getOriginalMesh() {
		if (this instanceof MeshAccessor<?> meshAccessor) {
			return (StaticMesh<?>)meshAccessor.get();
		} else {
			return (StaticMesh<?>)this;
		}
	}
	
	public static record ClothSimulationInfo(float particleMass, float selfCollision, List<int[]> constraints, ConstraintType[] constraintTypes, float[] compliances, int[] particles, float[] weights, float[] rootDistance, int[] normalOffsetMapping) {
	}
}
