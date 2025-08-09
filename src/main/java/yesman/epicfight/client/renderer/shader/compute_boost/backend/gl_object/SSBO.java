package yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.lwjgl.opengl.GL46C.*;

public class SSBO<T> implements AutoCloseable {
    public final T[] src;
    public final short src_size;
    public final int glSSBO;
    public final DataMode mode;
    public final BiConsumer<T, float[]> uploader;
    public final BiConsumer<T, FloatBuffer> uploader2;

    final float[] helper;

    public SSBO(T[] src, short src_size, DataMode DataMode,
                @NotNull BiConsumer<T, float[]> uploader,
                @Nullable BiConsumer<T, FloatBuffer> uploader2
    ) {
        this.src = src;
        this.mode = DataMode;
        this.src_size = src_size;
        this.uploader = uploader;
        this.uploader2 = uploader2;

        glSSBO = glGenBuffers();

        helper = new float[src_size / 4];

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);
        glBufferData(GL_SHADER_STORAGE_BUFFER,
                (long) src.length * src_size, mode.asInt);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }


    public void updateDataAt(int pos){
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);
        uploader.accept(src[pos], helper);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER,
                (long)pos * src_size, helper
                );
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void updateAll(){
        FloatBuffer buffer = BufferUtils.createFloatBuffer(src.length * src_size);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);

        if(uploader2 == null){
            for (T s : src) {
                uploader.accept(s, helper);
                buffer.put(helper);
            }
        }
        else {
            for (T s : src) {
                uploader2.accept(s, buffer);
            }
        }

        glBufferSubData(GL_SHADER_STORAGE_BUFFER,
                0, buffer
        );

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
    public void close() throws Exception {
        if (glSSBO != 0) glDeleteBuffers(glSSBO);
    }

    public enum DataMode{
        STATIC(GL_STATIC_DRAW), DYNAMIC(GL_DYNAMIC_DRAW), STREAM(GL_STREAM_DRAW);

        public final int asInt;
        DataMode(int _GL_MODE){
            asInt = _GL_MODE;
        }

    }
}
