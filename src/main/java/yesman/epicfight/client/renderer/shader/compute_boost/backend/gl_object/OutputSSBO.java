package yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object;

import static org.lwjgl.opengl.GL46C.*;

public class OutputSSBO {

    public final short src_size;
    public final int glSSBO;

    public OutputSSBO(short srcSize, int len, SSBO.DataMode mode) {
        src_size = srcSize;
        glSSBO = glGenBuffers();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);
        glBufferData(GL_SHADER_STORAGE_BUFFER, (long) srcSize * len, mode.asInt);
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


}
