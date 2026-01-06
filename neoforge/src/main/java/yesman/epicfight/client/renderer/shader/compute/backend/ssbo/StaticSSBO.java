package yesman.epicfight.client.renderer.shader.compute.backend.ssbo;

import com.mojang.blaze3d.platform.GlStateManager;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.io.Closeable;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.function.BiConsumer;


public class StaticSSBO<T> implements Closeable {
    public final int glSSBO;
    private int lastBinding = -1;

    public StaticSSBO(List<T> data, int dataSize, @NotNull BiConsumer<T, FloatBuffer> uploader) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.size() * dataSize);

        for (T d : data) {
            uploader.accept(d, buffer);
        }

        buffer.flip();

        this.glSSBO = GL15C.glGenBuffers();

        GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
        GL15C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, buffer, DynamicSSBO.DataMode.STATIC.glMode);
    }

    public void bindBufferBase(int binding) {
        this.unbind();
        this.lastBinding = binding;
        GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, this.glSSBO);
    }

    public void unbind() {
        if (this.lastBinding >= 0) {
            GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, this.lastBinding, 0);
        }
    }

    @Override
    public void close() {
        if (this.glSSBO != 0) {
            GlStateManager._glDeleteBuffers(this.glSSBO);
        }
    }
}
