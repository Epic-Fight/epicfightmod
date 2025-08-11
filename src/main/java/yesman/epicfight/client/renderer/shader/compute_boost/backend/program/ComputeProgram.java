package yesman.epicfight.client.renderer.shader.compute_boost.backend.program;

import static org.lwjgl.opengl.GL46.*;


public class ComputeProgram {

    private final int programHandle;
    private final int barrierFlags;

    public ComputeProgram(int barrierFlags) {
        this.programHandle	= glCreateProgram();
        this.barrierFlags	= barrierFlags;
    }

    public void dispatch(
            int countX,
            int countY,
            int countZ
    ) {
        glDispatchCompute(
                countX,
                countY,
                countZ
        );
    }

    public void linkProgram() {
        glLinkProgram(programHandle);
    }

    public boolean isLinked() {
        return glGetProgrami(programHandle, GL_LINK_STATUS) == GL_TRUE;
    }

    public void useProgram() {
        glUseProgram(programHandle);
    }

    public void resetProgram() {
        glUseProgram(0);
    }

    public void attachShader(ComputeShader computeShader) {
        glAttachShader(programHandle, computeShader.shaderHandle);
    }

    public void waitBarriers() {
        glMemoryBarrier(barrierFlags);
    }

    public void waitBarriersWith(int subTag) {
        glMemoryBarrier(barrierFlags | subTag);
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(programHandle, name);
    }

    public Uniform getUniform(String name) {
        return new Uniform(programHandle, getUniformLocation(name));
    }

    public String getInfoLog() {
        return glGetProgramInfoLog(programHandle);
    }

    public void delete() {
        glDeleteProgram(programHandle);
    }
}