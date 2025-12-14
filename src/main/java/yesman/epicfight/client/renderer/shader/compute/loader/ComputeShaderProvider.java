package yesman.epicfight.client.renderer.shader.compute.loader;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.lwjgl.opengl.GL33C;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.renderer.shader.compute.ComputeShaderSetup;
import yesman.epicfight.client.renderer.shader.compute.VanillaComputeShaderSetup;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.DynamicSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.IArrayBufferProxy;
import yesman.epicfight.client.renderer.shader.compute.backend.program.BarrierFlags;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.iris.IrisComputeShaderSetup;
import yesman.epicfight.main.EpicFightMod;

public class ComputeShaderProvider {
    public static ComputeProgram meshComputeVanilla;
    public static ComputeProgram meshComputeIris;
    
    private static boolean supportComputeShader = false;
    private static boolean irisLoaded = false;
    private static Function<SkinnedMesh, ComputeShaderSetup> computeShaderProvider = VanillaComputeShaderSetup::new;
    
    public static void initIris() {
    	irisLoaded = true;
    	computeShaderProvider = IrisComputeShaderSetup::new;
    }
    
    public static boolean supportComputeShader() {
    	return supportComputeShader;
    }
    
    public static boolean irisLoaded() {
        return irisLoaded;
    }
    
    public static void checkIfSupports() {
    	String glVersion = GL33C.glGetString(GL33C.GL_VERSION);
        int major = GL33C.glGetInteger(GL33C.GL_MAJOR_VERSION);
        int minor = GL33C.glGetInteger(GL33C.GL_MINOR_VERSION);
        
        supportComputeShader = ((major > 4) || (major == 4 && minor >= 3));
        
        EpicFightMod.LOGGER.warn("[Computer Shader Acceleration] OpenGL Version: " + glVersion);
        EpicFightMod.LOGGER.warn("[Computer Shader Acceleration] Compute Shader: " + (supportComputeShader ? "Supported" : "Unsupported"));
    }
    
    public static void epicfight$registerComputeShaders(RegisterShadersEvent event) {
        if (!supportComputeShader) return;
        
        clear();
        
        try {
            meshComputeVanilla = ComputeShaderLoader.loadComputeShaderProgram(event.getResourceProvider(), EpicFightMod.identifier("shaders/compute/vanilla_mesh_transformer.comp"), BarrierFlags.SHADER_STORAGE, BarrierFlags.VERTEX_ATTRIB_ARRAY);
            if (irisLoaded) meshComputeIris = ComputeShaderLoader.loadComputeShaderProgram(event.getResourceProvider(), EpicFightMod.identifier("shaders/compute/iris_mesh_transformer.comp"), BarrierFlags.SHADER_STORAGE, BarrierFlags.VERTEX_ATTRIB_ARRAY);
        } catch (Exception e) {
            supportComputeShader = false;
            EpicFightMod.LOGGER.warn("[Computer Shader Acceleration] There were some errors while loading the compute shader, and this feature will be forcibly disabled.");
            EpicFightMod.LOGGER.warn("[Computer Shader Acceleration] Detail: " + e);
        }
    }
    
    public static void clear() {
        if (meshComputeVanilla != null) {
        	meshComputeVanilla.delete();
        }
        
        if (meshComputeIris != null) {
        	meshComputeIris.delete();
        }
    }
    
    public static ComputeShaderSetup getComputeShaderSetup(SkinnedMesh mesh) {
    	return computeShaderProvider.apply(mesh);
    }
    
    public static <T> IArrayBufferProxy createDynamicBuffer(T[] src, int srcSize, BiConsumer<T, FloatBuffer> uploader){
        return new DynamicSSBO<>(src, (short) srcSize, DynamicSSBO.DataMode.DYNAMIC, uploader);
    }
}
