package yesman.epicfight.client.renderer.shader.compute.backend.ssbo;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.io.Closeable;


public class OutputSSBO implements Closeable {
    public final int glSSBO;
    private int lastBinding = -1;

    public OutputSSBO(short srcSize, int len, DynamicSSBO.DataMode mode) {
        this.glSSBO = GL15C.glGenBuffers();

        GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
        GlStateManager._glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, (long)srcSize * len * 4, mode.glMode);
        GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void bindBufferBase(int binding) {
        this.unbind();
        this.lastBinding = binding;
        GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, this.glSSBO);
    }

    public void unbind(){
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
