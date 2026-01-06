package yesman.epicfight.client.renderer.shader.compute.backend.program;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public class ComputeShader {
    public final int shaderId;

    public ComputeShader() {
        this.shaderId = glCreateShader(GL_COMPUTE_SHADER);
    }

    public void setShaderSource(String source) {
        glShaderSource(this.shaderId, source);
    }

    public void compileShader() {
        glCompileShader(this.shaderId);
    }

    public boolean isCompiled() {
        return glGetShaderi(this.shaderId, GL_COMPILE_STATUS) == GL_TRUE;
    }

    public String getInfoLog() {
        return glGetShaderInfoLog(this.shaderId);
    }

    public void delete() {
        glDeleteShader(this.shaderId);
    }
}
