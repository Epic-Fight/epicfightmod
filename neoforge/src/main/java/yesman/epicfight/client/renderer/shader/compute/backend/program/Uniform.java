package yesman.epicfight.client.renderer.shader.compute.backend.program;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;

public record Uniform(int programHandle, int uniformLocation) {
    public void uploadMatrix3f(Matrix3f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
			GL46C.glProgramUniformMatrix3fv(this.programHandle, this.uniformLocation, false, matrix.get(stack.callocFloat(9)));
        }
    }
    
    public void uploadMatrix4f(Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
			GL46C.glProgramUniformMatrix4fv(this.programHandle, this.uniformLocation, false, matrix.get(stack.callocFloat(16)));
        }
    }

    public void uploadUnsignedInt(int value) {
		GL46C.glProgramUniform1ui(this.programHandle, this.uniformLocation, value);
    }

    public void uploadVec4(float a, float b, float c, float d){
		GL46C.glProgramUniform4f(this.programHandle, this.uniformLocation, a, b, c, d);
    }
}