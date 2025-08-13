package yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL46C.*;

public class DynamicSSBO<T> implements Closeable {
    public final T[] src;
    public final short src_size;
    public final int glSSBO;
    public final DataMode mode;
    public final BiConsumer<T, FloatBuffer> uploader;

    final FloatBuffer buffer;

    public DynamicSSBO(T[] src, short src_size, DataMode DataMode,
                       @Nullable BiConsumer<T, FloatBuffer> uploader
    ) {
        this.src = src;
        this.mode = DataMode;
        this.src_size = src_size;
        this.uploader = uploader;

        glSSBO = glGenBuffers();

        //helper = new float[src_size];

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);
        glBufferData(GL_SHADER_STORAGE_BUFFER,
                (long) src.length * src_size * 4, mode.asInt);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        buffer = BufferUtils.createByteBuffer(src.length * src_size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public void updateAll(){
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);

        for (T s : src) {
            uploader.accept(s, buffer);
        }

        buffer.position(0);

        glBufferSubData(GL_SHADER_STORAGE_BUFFER,
                0, buffer
        );

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }

    public void updateFromTo(int from, int to){
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);

        for (int i = from; i < to; i++) {
            uploader.accept(src[i], buffer);
        }

        buffer.position(0);

        glBufferSubData(GL_SHADER_STORAGE_BUFFER,
                (long) src_size * 4 * from, buffer
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
    public void close() {
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
