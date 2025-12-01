package yesman.epicfight.api.utils.math;

import org.joml.Matrix4f;

@FunctionalInterface
public interface MatrixOperation {
	Matrix4f mul(Matrix4f left, Matrix4f right, Matrix4f dest);
}