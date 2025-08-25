package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

public interface IArrayBufferProxy {
    void updateFromTo(int from, int to);
    void bindBufferBase(int binding);
    void unbind();
    void updateAll();
    void close();
}
