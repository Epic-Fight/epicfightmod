package yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object;

import java.io.Closeable;
import java.io.IOException;

import static org.lwjgl.opengl.GL46.*;

public class OutputSSBO implements Closeable {

    public final short src_size;
    public final int glSSBO;

    public OutputSSBO(short srcSize, int len, DynamicSSBO.DataMode mode) {
        src_size = srcSize;
        glSSBO = glGenBuffers();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) srcSize * len * 4, mode.asInt);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    private int lastBinding = -1;
    public void bindBufferBase(int binding){
        unbind();
        lastBinding = binding;
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, binding, glSSBO);
    }

    public void unbind(){
        if(lastBinding >= 0) glBindBufferBase(GL_SHADER_STORAGE_BUFFER, lastBinding, 0);
    }


    @Override
    public void close() {
        if (glSSBO != 0) glDeleteBuffers(glSSBO);
    }
}
