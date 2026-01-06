package yesman.epicfight.client.renderer.shader.compute.backend.program;

import org.lwjgl.opengl.GL46C;


public enum BarrierFlags {
    SHADER_STORAGE(GL46C.GL_SHADER_STORAGE_BARRIER_BIT),
    ATOMIC_COUNTER(GL46C.GL_ATOMIC_COUNTER_BARRIER_BIT),
    ELEMENT_ARRAY(GL46C.GL_ELEMENT_ARRAY_BARRIER_BIT),
    VERTEX_ATTRIB_ARRAY(GL46C.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT),
    COMMAND(GL46C.GL_COMMAND_BARRIER_BIT);
	
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
