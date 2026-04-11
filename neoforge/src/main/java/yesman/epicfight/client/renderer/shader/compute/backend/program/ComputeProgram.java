package yesman.epicfight.client.renderer.shader.compute.backend.program;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL46;


public class ComputeProgram {
    private final int programHandle;
    private final int barrierFlags;
    
    public ComputeProgram(int barrierFlags) {
        this.programHandle = GL46.glCreateProgram();
        this.barrierFlags = barrierFlags;
    }
    
	public void dispatch(int countX, int countY, int countZ) {
		GL46.glDispatchCompute(countX, countY, countZ);
	}
    
    public void linkProgram() {
    	GL46.glLinkProgram(this.programHandle);
    }

    public boolean isLinked() {
        return GL46.glGetProgrami(this.programHandle, GL20.GL_LINK_STATUS) == GL11.GL_TRUE;
    }

    public void useProgram() {
    	GL46.glUseProgram(this.programHandle);
    }

    public void resetProgram() {
    	GL46.glUseProgram(0);
    }

    public void attachShader(ComputeShader computeShader) {
    	GL46.glAttachShader(this.programHandle, computeShader.shaderId);
    }

    public void memBarriers() {
    	GL46.glMemoryBarrier(this.barrierFlags);
    }

    public void waitBarriersWith(int subTag) {
    	GL46.glMemoryBarrier(this.barrierFlags | subTag);
    }

    public int getUniformLocation(String name) {
        return GL46.glGetUniformLocation(this.programHandle, name);
    }

    public Uniform getUniform(String name) {
        return new Uniform(this.programHandle, this.getUniformLocation(name));
    }

    public String getInfoLog() {
        return GL46.glGetProgramInfoLog(this.programHandle);
    }

    public void delete() {
    	GL46.glDeleteProgram(this.programHandle);
    }
}
