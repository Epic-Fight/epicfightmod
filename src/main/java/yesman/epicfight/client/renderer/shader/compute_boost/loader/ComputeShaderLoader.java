package yesman.epicfight.client.renderer.shader.compute_boost.loader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.program.BarrierFlags;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.program.ComputeShader;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ComputeShaderLoader {


    public static ComputeProgram LoadComputeShaderProgram(ResourceProvider resourceManager, ResourceLocation resourceLocation,
                                                          BarrierFlags... barrierFlags
    ){
        var resource = resourceManager.getResource(resourceLocation);

        if (resource.isEmpty()) {
            throw new IllegalStateException("Cannot found compute shader: \"" + resourceLocation + "\"");
        }

        ShaderSource src;
        try (var stream = resource.get().open()) {
            src = new ShaderSource(new String(stream.readAllBytes(),
                    UTF_8), BarrierFlags.getFlags(barrierFlags));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var program			= new ComputeProgram(BarrierFlags.getFlags(barrierFlags));
        var computeShader	= new ComputeShader();

        computeShader.setShaderSource(src.source());
        computeShader.compileShader();

        if (!computeShader.isCompiled()) {
            throw new IllegalStateException("Shader \"" + resourceLocation +
                    "\"failed to compile because of the following errors: "
                    + computeShader.getInfoLog());
        }

        program.attachShader(computeShader);
        program.linkProgram	();

        if (!program.isLinked()) {
            throw new IllegalStateException("Program \"" + resourceLocation +
                    "\" failed to link because of the following errors: " +
                    program.getInfoLog());
        }
        computeShader.delete();

        return program;
    }

    public record ShaderSource(String source, int barrierFlags) {
    }

}
