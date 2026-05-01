package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

public interface IClientBuffer {

	long reserve	(long bytes);
	long reserve	(long bytes, boolean occupied);
	long addressAt	(long position);
}
