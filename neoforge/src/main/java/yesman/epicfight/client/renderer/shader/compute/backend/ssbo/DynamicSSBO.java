package yesman.epicfight.client.renderer.shader.compute.backend.ssbo;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;

import java.io.Closeable;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.function.BiConsumer;


public class DynamicSSBO<T> implements Closeable, IArrayBufferProxy {
    public final T[] src;
    public final short srcSize;
    public final int glSSBO;
    public final DataMode mode;
    public final BiConsumer<T, FloatBuffer> uploader;
    
    private final FloatBuffer buffer;
    private int lastBinding = -1;
    
    public DynamicSSBO(T[] src, short srcSize, DataMode DataMode, BiConsumer<T, FloatBuffer> uploader) {
        this.src = src;
        this.mode = DataMode;
        this.srcSize = srcSize;
        this.uploader = uploader;
        this.glSSBO = GlStateManager._glGenBuffers();
        
        GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
        GlStateManager._glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, (long) src.length * srcSize * 4, mode.glMode);
        GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
        
        this.buffer = BufferUtils.createByteBuffer(src.length * srcSize * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }
    
    @Override
    public void updateAll() {
    	GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
    	
        for (T s : this.src) {
        	this.uploader.accept(s, this.buffer);
        }
        
        this.buffer.position(0);
        
        GL15C.glBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, 0, this.buffer);
        GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
    }
    
    @Override
    public void updateFromTo(int from, int to) {
    	GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
    	
        for (int i = from; i < to; i++) {
        	this.uploader.accept(this.src[i], this.buffer);
        }
        
        this.buffer.position(0);
        
		GL15C.glBufferSubData(GL43C.GL_SHADER_STORAGE_BUFFER, (long) srcSize * 4 * from, this.buffer);
		GlStateManager._glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
    }
    
    @Override
	public void bindBufferBase(int binding) {
    	this.unbind();
        this.lastBinding = binding;
        GL30C.glBindBufferBase(GL43C.GL_SHADER_STORAGE_BUFFER, binding, this.glSSBO);
    }
    
    @Override
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
    
    public enum DataMode {
        STATIC(GL15C.GL_STATIC_DRAW), DYNAMIC(GL15C.GL_DYNAMIC_DRAW), STREAM(GL15C.GL_STREAM_DRAW);
    	
        public final int glMode;
        
        DataMode(int glMode) {
        	this.glMode = glMode;
        }
    }
}
