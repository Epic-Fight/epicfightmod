package yesman.epicfight.api.physics.ik;

import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import yesman.epicfight.api.physics.SimulatableObject;

public interface InverseKinematicsSimulatable extends SimulatableObject {
	public float getRootXRot();
	public float getRootXRotO();
	
	public float getRootZRot();
	public float getRootZRotO();
	
	public Matrix4f getModelMatrix(float partialTick);
	
	InverseKinematicsSimulator getIKSimulator();
	
	Entity toEntity();
}
