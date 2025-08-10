package yesman.epicfight.client.renderer.shader.compute_boost.loader;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL33C;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.program.BarrierFlags;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.program.ComputeProgram;
import yesman.epicfight.main.EpicFightMod;

public class ShaderRegistries {

    public static ComputeProgram mesh_compute;

    @Getter
    private static boolean ComputeShaderSupport = false;

    public static void register(RegisterShadersEvent event){
        var GL_VERSION = GL33C.glGetString(GL33C.GL_VERSION);

        int major = GL33C.glGetInteger(GL33C.GL_MAJOR_VERSION);
        int minor = GL33C.glGetInteger(GL33C.GL_MINOR_VERSION);

        ComputeShaderSupport = (major > 4) || (major == 4 && minor >= 6);

        EpicFightMod.LOGGER.warn("[Mesh Render Accelerate] OpenGL Version: " + GL_VERSION);
        EpicFightMod.LOGGER.warn("[Mesh Render Accelerate] Accelerate " +
                (ComputeShaderSupport ? "Support" : "Unsupported"));

        if(!ComputeShaderSupport) return;

        clear();
        try {
            mesh_compute = ComputeShaderLoader.LoadComputeShaderProgram(event.getResourceProvider(),
                    ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "shaders/compute/test.comp"),
                    BarrierFlags.SHADER_STORAGE
            );
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        if(mesh_compute == null){
            EpicFightMod.LOGGER.error("FUUUUCK");
        }
        else {
            EpicFightMod.LOGGER.warn("OKKKK");
        }

    }

    public static void clear(){
        if(mesh_compute != null) mesh_compute.delete();
    }

}
