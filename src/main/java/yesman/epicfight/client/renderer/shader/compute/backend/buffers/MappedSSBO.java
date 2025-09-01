package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;

import static org.lwjgl.opengl.GL45C.*;

// todo

@OnlyIn(Dist.CLIENT)
public class MappedSSBO<T> /*implements Closeable, IArrayBufferProxy*/ {
   /* public final T[] src;
    public final short srcSize;
    public final int glSSBO;
    //public final DynamicSSBO.DataMode mode;
    public final BiConsumer<T, FloatBuffer> uploader;
    private final FloatBuffer buffer;

    private int lastBinding = -1;

    private long fence;
    private boolean fenceInserted = false;

    public MappedSSBO(T[] src, short srcSize, BiConsumer<T, FloatBuffer> uploader) {
        this.src = src;
        //this.mode = DataMode;
        this.srcSize = srcSize;
        this.uploader = uploader;


        this.glSSBO = glGenBuffers();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, this.glSSBO);

        long size = (long) src.length * srcSize * 4;

        glBufferStorage(GL_SHADER_STORAGE_BUFFER, size,
                        GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);

        // 映射整个缓冲区到CPU内存
        ByteBuffer persistentMapping = glMapBufferRange(GL_SHADER_STORAGE_BUFFER, 0, size,
                GL_MAP_WRITE_BIT |
                        GL_MAP_PERSISTENT_BIT |
                        GL_MAP_COHERENT_BIT);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);

        buffer = persistentMapping.asFloatBuffer();
    }
    @Override
    public void updateAll() {
    	glBindBuffer(GL_SHADER_STORAGE_BUFFER, this.glSSBO);
    	
        for (T s : this.src) {
        	this.uploader.accept(s, this.buffer);
        }
        
        this.buffer.position(0);
        insertFence();
        waitForFence();
    }

    @Override
    public void updateFromTo(int from, int to) {
    	glBindBuffer(GL_SHADER_STORAGE_BUFFER, this.glSSBO);
        for (int i = from; i < to; i++) {
        	this.uploader.accept(this.src[i], this.buffer);
        }
        this.buffer.position(0);
        insertFence();
        waitForFence();
    }
    @Override
	public void bindBufferBase(int binding) {
    	this.unbind();
        this.lastBinding = binding;
        glBindBufferBase(GL_SHADER_STORAGE_BUFFER, binding, this.glSSBO);
    }
    @Override
	public void unbind() {
        if (this.lastBinding >= 0) {
        	glBindBufferBase(GL_SHADER_STORAGE_BUFFER, this.lastBinding, 0);
        }
    }
	
    @Override
    public void close() {
        if (this.glSSBO != 0) {
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, glSSBO);
            glUnmapBuffer(GL_SHADER_STORAGE_BUFFER);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
        	glDeleteBuffers(glSSBO);
        }
    }

    public void insertFence() {
        fence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        fenceInserted = true;
    }

    public void waitForFence() {
        if (fenceInserted) {
            int result;
            do {
                result = glClientWaitSync(fence, GL_SYNC_FLUSH_COMMANDS_BIT, 1000000000);
            } while (result == GL_TIMEOUT_EXPIRED);

            glDeleteSync(fence);
            fenceInserted = false;
        }
    }*/
}
