package yesman.epicfight.client.renderer.shader.compute.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.lwjgl.opengl.GL43C;
import yesman.epicfight.client.renderer.shader.compute.backend.program.BarrierFlags;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeShader;

public final class ComputeShaderLoader {
	public static ComputeProgram loadComputeShaderProgram(ResourceProvider resourceManager, ResourceLocation resourceLocation, BarrierFlags... barrierFlags) throws IllegalStateException {
		Optional<Resource> resource = resourceManager.getResource(resourceLocation);
		
		if (resource.isEmpty()) {
			throw new IllegalStateException("Cannot found compute shader: \"" + resourceLocation + "\"");
		}
		
		ShaderSource src = null;
		InputStream stream = null;
		
		try {
			stream = resource.get().open();
			src = new ShaderSource(new String(stream.readAllBytes(), StandardCharsets.UTF_8), BarrierFlags.getFlags(barrierFlags));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		
		ComputeProgram program = new ComputeProgram(BarrierFlags.getFlags(barrierFlags));
		ComputeShader computeShader = new ComputeShader();
		
		computeShader.setShaderSource(src.source());
		computeShader.compileShader();
		
		if (!computeShader.isCompiled()) {
			throw new IllegalStateException("Shader \"" + resourceLocation + "\" failed to compile because of the following errors: " + computeShader.getInfoLog());
		}
		
		program.attachShader(computeShader);
		program.linkProgram();
		
		if (!program.isLinked()) {
			throw new IllegalStateException("Program \"" + resourceLocation + "\" failed to link because of the following errors: " + program.getInfoLog());
		}
		
		computeShader.delete();
		
		return program;
	}

	public static long getGLMaxSSBOSize(){
		return GL43C.glGetInteger64(GL43C.GL_MAX_SHADER_STORAGE_BLOCK_SIZE);
	}

	public static long getSSBOAlignment(){
		return GL43C.glGetInteger(GL43C.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
	}

    public record ShaderSource(String source, int barrierFlags) {
    }
    
    private ComputeShaderLoader() {
    }
}
