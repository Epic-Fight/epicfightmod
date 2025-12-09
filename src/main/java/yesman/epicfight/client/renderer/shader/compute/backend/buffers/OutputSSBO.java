package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import java.io.Closeable;

import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;


public class OutputSSBO implements Closeable {
    public final int glSSBO;
    
    private int lastBinding = -1;
    
    public OutputSSBO(short srcSize, int len, DynamicSSBO.DataMode mode) {
        this.glSSBO = GL15C.glGenBuffers();
        
        GL15C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, this.glSSBO);
        GL15C.glBufferData(GL43C.GL_SHADER_STORAGE_BUFFER, (long)srcSize * len * 4, mode.glMode);
        GL15C.glBindBuffer(GL43C.GL_SHADER_STORAGE_BUFFER, 0);
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
        	GL15C.glDeleteBuffers(this.glSSBO);
        }
    }
}
