package yesman.epicfight.client.renderer.shader.compute_boost.backend.program;

import static org.lwjgl.opengl.GL46.*;

public class ComputeShader {
    public final int shaderHandle;

    public ComputeShader() {
        this.shaderHandle = glCreateShader(GL_COMPUTE_SHADER);
    }

    public void setShaderSource(String source) {
        glShaderSource(shaderHandle, source);
    }

    public void compileShader() {
        glCompileShader(shaderHandle);
    }

    public boolean isCompiled() {
        return glGetShaderi(shaderHandle, GL_COMPILE_STATUS) == GL_TRUE;
    }

    public String getInfoLog() {
        return glGetShaderInfoLog(shaderHandle);
    }

    public void delete() {
        glDeleteShader(shaderHandle);
    }
}
