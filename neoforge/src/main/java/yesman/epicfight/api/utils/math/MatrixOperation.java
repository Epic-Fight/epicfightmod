package yesman.epicfight.api.utils.math;

@FunctionalInterface
public interface MatrixOperation {
	OpenMatrix4f mul(OpenMatrix4f left, OpenMatrix4f right, OpenMatrix4f dest);
}