package yesman.epicfight.client.renderer.shader.compute.backend.buffers;


public interface IArrayBufferProxy {
    void updateFromTo(int from, int to);
    void bindBufferBase(int binding);
    void unbind();
    void updateAll();
    void close();
}
