package yesman.epicfight.mixin.iris;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.image.ImageHolder;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.uniform.DynamicLocationalUniformHolder;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import yesman.epicfight.api.exception.ShaderParsingException;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.shader.AnimationShaderInstance;
import yesman.epicfight.client.renderer.shader.IrisAnimationShader;
import yesman.epicfight.client.renderer.shader.ShaderParser;
import yesman.epicfight.client.renderer.shader.ShaderParser.Formatter;
import yesman.epicfight.compat.IRISCompat;
import yesman.epicfight.main.EpicFightMod;

@Mixin(value = net.irisshaders.iris.pipeline.programs.ExtendedShader.class)
public abstract class MixinExtendedShader {
	/*@Inject(at = @At(value = "TAIL"), method = "<init>", cancellable = true)
	private void epicfight_constructor(ResourceProvider resourceFactory, String name, VertexFormat vertexFormat, boolean usesTessellation, GlFramebuffer writingToBeforeTranslucent, GlFramebuffer writingToAfterTranslucent,
			BlendModeOverride blendModeOverride, AlphaTest alphaTest, Consumer<DynamicLocationalUniformHolder> uniformCreator, BiConsumer<SamplerHolder, ImageHolder> samplerCreator, boolean isIntensity, IrisRenderingPipeline parent,
			@Nullable List<BufferBlendOverride> bufferBlendOverrides, CustomUniforms customUniforms, CallbackInfo info)
	{
		if (!(this instanceof AnimationShaderInstance)) {
			IRISCompat.putIrisShaderProvider(name, () -> {
				ShaderParser shaderParser = null;
				
				try {
					shaderParser = new ShaderParser(resourceFactory, name);
					ResourceLocation shaderLocation = ResourceLocation.parse(name);
					boolean hasNormalAttribute = shaderParser.hasAttribute("Normal");
					boolean isEyesShader = "rendertype_eyes".equals(shaderLocation.getPath());
					
					if (shaderParser.hasAttribute("Color")) {
						shaderParser.addUniform("iris_Color", ShaderParser.GLSLType.VEC4, "in .* iris_Color;", ShaderParser.InsertPosition.FOLLOWING, Integer.MAX_VALUE, ShaderParser.ExceptionHandler.THROW, new Double[] {1.0D, 1.0D, 1.0D, 1.0D});
					}
					
					if (shaderParser.hasAttribute("UV1") && !isEyesShader) {
						shaderParser.addUniform("iris_UV1", ShaderParser.GLSLType.IVEC2, "in .* iris_UV1;", ShaderParser.InsertPosition.FOLLOWING, Integer.MAX_VALUE, ShaderParser.ExceptionHandler.IGNORE, new Integer[] {0, 0});
					}
					
					if (shaderParser.hasAttribute("UV2") && !isEyesShader) {
						shaderParser.addUniform("iris_UV2", ShaderParser.GLSLType.IVEC2, "in .* iris_UV2;", ShaderParser.InsertPosition.FOLLOWING, Integer.MAX_VALUE, ShaderParser.ExceptionHandler.IGNORE, new Integer[] {0, 0});
					}
					
					shaderParser.remove("Color", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
					shaderParser.remove("UV1", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
					shaderParser.remove("UV2", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
					shaderParser.remove("iris_Color", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
					shaderParser.remove("iris_UV1", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
					shaderParser.remove("iris_UV2", ShaderParser.Usage.ATTRIBUTE, ShaderParser.ExceptionHandler.IGNORE);
					shaderParser.addAttribute("Joints", ShaderParser.ExceptionHandler.THROW, ShaderParser.GLSLType.IVEC3);
					shaderParser.addAttribute("Weights", ShaderParser.ExceptionHandler.THROW, ShaderParser.GLSLType.VEC3);
					
					if (hasNormalAttribute && !isEyesShader) {
						shaderParser.addUniform("iris_Normal_Mv_Matrix", ShaderParser.GLSLType.MATRIX3F, ShaderParser.ExceptionHandler.THROW, null);
					}
					
					shaderParser.addUniformArray("iris_Poses", ShaderParser.GLSLType.MATRIX4F, ShaderParser.ExceptionHandler.THROW, null, ShaderParser.SHADER_ARRAY_LIMIT);
					
					shaderParser.replaceScript("iris_Position", "Position_a", -1, ShaderParser.ExceptionHandler.THROW, "in vec3 iris_Position;");
					
					if (hasNormalAttribute && !isEyesShader) {
						shaderParser.replaceScript("iris_Normal", "Normal_a", -1, ShaderParser.ExceptionHandler.THROW, "in .* iris_Normal;", "iris_NormalMat", "uniform mat3 iris_Normal_Mv_Matrix;");
					}
					
					shaderParser.insertToScript("in vec3 iris_Position;", "\nvec3 Position_a = vec3(0.0);", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.THROW);
					
					Formatter normalAttribute = null;
					
					if (hasNormalAttribute && !isEyesShader) {
						normalAttribute = shaderParser.insertToScript("in vec3 iris_Normal;", "\nvec3 Normal_a = vec3(0.0);", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.IGNORE);
					}
					
					shaderParser.insertToScript("void main\\(\\) \\{",
											    "\n\nvoid setAnimationPosition() {\n"
											  + "    for(int i=0;i<3;i++)\n"
											  + "    {\n"
											  + "        mat4 jointTransform = iris_Poses[Joints[i]];\n"
											  + "        vec4 posePosition = jointTransform * vec4(iris_Position, 1.0);\n"
											  + "        Position_a += vec3(posePosition.xyz) * Weights[i];\n"
											  + "    }\n"
											  + "}\n", 0, ShaderParser.InsertPosition.PRECEDING, ShaderParser.ExceptionHandler.THROW);
					
					if (hasNormalAttribute && !isEyesShader) {
						shaderParser.insertToScript("void main\\(\\) \\{",
												    "\n\nvoid setAnimationNormal() {\n"
												  + "    \n"
												  + "    for(int i=0;i<3;i++)\n"
												  + "    {\n"
												  + "        mat4 jointTransform = iris_Poses[Joints[i]];\n"
												  + "        vec4 poseNormal = jointTransform * vec4(iris_Normal, 1.0);\n"
												  + "        Normal_a += vec3(poseNormal.xyz) * Weights[i];\n"
												  + "    }\n"
												  + "    \n"
												  + "    Normal_a = iris_Normal_Mv_Matrix * Normal_a;\n"
												  + "}\n", 0, ShaderParser.InsertPosition.PRECEDING, ShaderParser.ExceptionHandler.THROW).whenSuccess(normalAttribute);
						
						shaderParser.insertToScript("void main\\(\\) \\{", "\n    setAnimationNormal();", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.THROW).whenSuccess(normalAttribute);
					}
					
					shaderParser.insertToScript("void main\\(\\) \\{", "\n    setAnimationPosition();", 0, ShaderParser.InsertPosition.FOLLOWING, ShaderParser.ExceptionHandler.THROW);
					
					Map<ResourceLocation, Resource> cache = Maps.newHashMap();
					shaderParser.addToResourceCache(cache);
					GameRenderer.ResourceCache resourceProvider = new GameRenderer.ResourceCache(resourceFactory, cache);
					ResourceLocation rl = ResourceLocation.parse(name);
					VertexFormat animationvertexFormat = EpicFightRenderTypes.getAnimationVertexFormat(vertexFormat);
					
					return new IrisAnimationShader(resourceProvider, EpicFightMod.MODID + ":" + rl.getPath(), animationvertexFormat, usesTessellation, writingToBeforeTranslucent, writingToAfterTranslucent, blendModeOverride, alphaTest,
													uniformCreator, samplerCreator, isIntensity, parent, bufferBlendOverrides, customUniforms);
				} catch (IOException | ShaderParsingException e) {
					e.printStackTrace();
					
					if (shaderParser != null) {
						EpicFightMod.LOGGER.warn("Shader Script\n " + shaderParser.getOriginalScript());
					}
					
					throw new RuntimeException(e);
				}
			});
		}
	}*/
}