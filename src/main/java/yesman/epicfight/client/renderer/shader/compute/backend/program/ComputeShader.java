package yesman.epicfight.client.renderer.shader.compute.backend.program;

import static org.lwjgl.opengl.GL46.*;

public class ComputeShader {
    public final int shaderHandle;

    public ComputeShader() {
        this.shaderHandle = glCreateShader(GL_COMPUTE_SHADER);
    }

    public void setShaderSource(String source) {
        glShaderSource(this.shaderHandle, source);
    }

    public void compileShader() {
        glCompileShader(this.shaderHandle);
    }

    public boolean isCompiled() {
        return glGetShaderi(this.shaderHandle, GL_COMPILE_STATUS) == GL_TRUE;
    }

    public String getInfoLog() {
        return glGetShaderInfoLog(this.shaderHandle);
    }

    public void delete() {
        glDeleteShader(this.shaderHandle);
    }
}
