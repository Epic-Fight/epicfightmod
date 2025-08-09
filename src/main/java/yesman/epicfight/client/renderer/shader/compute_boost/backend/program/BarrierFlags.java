package yesman.epicfight.client.renderer.shader.compute_boost.backend.program;

import static org.lwjgl.opengl.GL46.*;

public enum BarrierFlags {

    SHADER_STORAGE	(GL_SHADER_STORAGE_BARRIER_BIT),
    ATOMIC_COUNTER	(GL_ATOMIC_COUNTER_BARRIER_BIT),
    ELEMENT_ARRAY	(GL_ELEMENT_ARRAY_BARRIER_BIT),
    COMMAND			(GL_COMMAND_BARRIER_BIT);

    private final int flag;

    BarrierFlags(int flag) {
        this.flag = flag;
    }

    public static int getFlags(BarrierFlags... barrierFlags) {
        int intFlags = 0;

        for (BarrierFlags barrierFlag : barrierFlags) {
            intFlags = intFlags | barrierFlag.flag;
        }

        return intFlags;
    }
}
