package yesman.epicfight.api.client.animation.property;

import yesman.epicfight.api.client.animation.Layer;

public class LayerInfo {
	public final JointMaskEntry jointMaskEntry;
	public final Layer.Priority priority;
	public final Layer.LayerType layerType;
	
	public LayerInfo(JointMaskEntry jointMaskEntry, Layer.Priority priority, Layer.LayerType layerType) {
		this.jointMaskEntry = jointMaskEntry;
		this.priority = priority;
		this.layerType = layerType;
	}
}