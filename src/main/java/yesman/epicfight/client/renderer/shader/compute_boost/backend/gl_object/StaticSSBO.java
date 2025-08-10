package yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glBindBufferBase;
import static org.lwjgl.opengl.GL43C.GL_SHADER_STORAGE_BUFFER;

public class StaticSSBO<T> implements Closeable {

    public final int glSSBO;

    public StaticSSBO(List<T> data, int data_size,
                      @NotNull BiConsumer<T, FloatBuffer> uploader){
        glSSBO = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);
        glBufferData(GL_SHADER_STORAGE_BUFFER,
                (long) data.size() * data_size * 4, DynamicSSBO.DataMode.STATIC.asInt);


        FloatBuffer buffer = BufferUtils.createByteBuffer(data.size() * data_size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (T d : data) {
            uploader.accept(d, buffer);
        }
        buffer.flip();
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, buffer);


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
