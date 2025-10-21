package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IArrayBufferProxy {
    void updateFromTo(int from, int to);
    void bindBufferBase(int binding);
    void unbind();
    void updateAll();
    void close();
}
