package yesman.epicfight.api.client.model;

import java.util.function.Supplier;

import yesman.epicfight.api.utils.math.OpenMatrix4f;

public interface MeshPartDefinition {
	String partName();
	Mesh.RenderProperties renderProperties();
	Supplier<OpenMatrix4f> getModelPartAnimationProvider();
}