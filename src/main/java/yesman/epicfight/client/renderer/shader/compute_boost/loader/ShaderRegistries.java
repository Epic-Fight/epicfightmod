package yesman.epicfight.client.renderer.shader.compute_boost.loader;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33C;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.program.BarrierFlags;
import yesman.epicfight.main.EpicFightMod;

public class ShaderRegistries {

    @Getter
    private static boolean ComputeShaderSupport = false;

    public static void register(RegisterShadersEvent event){
        var GL_VERSION = GL33C.glGetString(GL33C.GL_VERSION);
        ComputeShaderSupport = GL_VERSION.compareTo("4.6.0") >= 0;

        EpicFightMod.LOGGER.warn("[Mesh Render Accelerate] OpenGL Version: " + GL_VERSION);
        EpicFightMod.LOGGER.warn("[Mesh Render Accelerate] Accelerate " +
                (ComputeShaderSupport ? "Support" : "Unsupported"));

        var result = ComputeShaderLoader.LoadComputeShader(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "shaders/compute/test.comp"),
                BarrierFlags.SHADER_STORAGE
                );

        if(result == null){
            EpicFightMod.LOGGER.error("FUUUUCK");
        }
        else {
            EpicFightMod.LOGGER.warn("OKKKK");
        }

    }

}
