package yesman.epicfight.client.renderer.shader.compute_boost.backend.program;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL41.glProgramUniform1ui;
import static org.lwjgl.opengl.GL46.glProgramUniformMatrix4fv;

public class Uniform {

    private final int programHandle;
    private final int uniformLocation;

    public Uniform(int programHandle, int uniformLocation) {
        this.programHandle		= programHandle;
        this.uniformLocation	= uniformLocation;
    }

    public void uploadMatrix4f(Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glProgramUniformMatrix4fv(
                    programHandle,
                    uniformLocation,
                    false,
                    matrix.get(stack.callocFloat(16))
            );
        }
    }

    public void uploadUnsignedInt(int value) {
        glProgramUniform1ui(
                programHandle,
                uniformLocation,
                value
        );
    }
}