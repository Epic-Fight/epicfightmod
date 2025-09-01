package yesman.epicfight.client.renderer.shader.compute;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL46C;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.IArrayBufferProxy;
//import yesman.epicfight.client.renderer.shader.compute.backend.buffers.MappedSSBO;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.main.EpicFightSharedConstants;

@OnlyIn(Dist.CLIENT)
public interface ComputeShaderSetup {
    static final int WORK_GROUP_SIZE = 128;

	static final OpenMatrix4f[] TOTAL_POSES = OpenMatrix4f.allocateMatrixArray(EpicFightSharedConstants.MAX_JOINTS);
	static final OpenMatrix4f[] TOTAL_NORMALS = OpenMatrix4f.allocateMatrixArray(EpicFightSharedConstants.MAX_JOINTS);


    static final IArrayBufferProxy POSE_BO = ComputeShaderProvider.createDynamicBuffer
            (TOTAL_POSES, 16, OpenMatrix4f::store);

/*
	static final DynamicSSBO<OpenMatrix4f> POSE_BO = new DynamicSSBO<>
            (TOTAL_POSES, (short) 16, DynamicSSBO.DataMode.DYNAMIC, OpenMatrix4f::store);*/

	static void setShaderDefaultUniforms(ShaderInstance shader, VertexFormat.Mode mode, Matrix4f frustumMatrix, Matrix4f projectionMatrix, Window window) {
        for (int i = 0; i < 12; i++) {
            int j = RenderSystem.getShaderTexture(i);
            shader.setSampler("Sampler" + i, j);
        }

        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(frustumMatrix);
        }

        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(projectionMatrix);
        }

        if (shader.COLOR_MODULATOR != null) {
            shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }

        if (shader.GLINT_ALPHA != null) {
            shader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }

        if (shader.FOG_START != null) {
            shader.FOG_START.set(RenderSystem.getShaderFogStart());
        }

        if (shader.FOG_END != null) {
            shader.FOG_END.set(RenderSystem.getShaderFogEnd());
        }

        if (shader.FOG_COLOR != null) {
            shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }

        if (shader.FOG_SHAPE != null) {
            shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }

        if (shader.TEXTURE_MATRIX != null) {
            shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }

        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        if (shader.SCREEN_SIZE != null) {
            shader.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
        }

        if (shader.LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
            shader.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
        }

        RenderSystem.setupShaderLights(shader);
    }
	
	static void bindAttrPointer(int vao, int size, int bindingPos, int glType) {
    	GL46C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vao);
    	GL46C.glVertexAttribPointer(bindingPos, size, glType, false, 0, 0);
    	GL46C.glEnableVertexAttribArray(bindingPos);
    }
    
    static void bindAttrPointer(int vao, int size, int bindingPos, int glType, int stride) {
    	GL46C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vao);
    	GL46C.glVertexAttribPointer(bindingPos, size, glType, false, stride, 0);
    	GL46C.glEnableVertexAttribArray(bindingPos);
    }
    
    static void bindIntAttrPointer(int vao, int size, int bindingPos, int glType, int stride) {
    	GL46C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vao);
    	GL46C.glVertexAttribIPointer(bindingPos, size, glType, stride, 0);
    	GL46C.glEnableVertexAttribArray(bindingPos);
    }
    
    static void clearBufferState(VertexFormat vertexFormat) {
        vertexFormat.clearBufferState();
    }
    
	void bindBufferFormat(VertexFormat vertexFormat, int... buffers);
	
	void applyComputeShader(PoseStack poseStack, OpenMatrix4f partTransform, float r, float g, float b, float a, int overlay, int light, int jointCount);
	
	void drawWithShader(SkinnedMesh skinnedMesh, PoseStack poseStack, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses);
	
	int vaoId();
	
	int vertexCount();
	
	void destroyBuffers();
	
	@OnlyIn(Dist.CLIENT)
	interface BufferUploadable {
		public void store(FloatBuffer buffer);
	}
	
	@OnlyIn(Dist.CLIENT)
	interface MeshPartBuffer {
		// For vanilla compute shader
		int vboId();
		
		// For iris compute shader
		int partIdx();
	}
	
	@OnlyIn(Dist.CLIENT)
	record VertexObj(float px, float py, float pz, float nx, float ny, float nz, int jts, int jte) implements ComputeShaderSetup.BufferUploadable {
		@Override
		public void store(FloatBuffer floatBuffer) {
			floatBuffer.put(this.px);
			floatBuffer.put(this.py);
			floatBuffer.put(this.pz);

			floatBuffer.put(this.nx);
			floatBuffer.put(this.ny);
			floatBuffer.put(this.nz);

			floatBuffer.put(Float.intBitsToFloat(this.jts));
			floatBuffer.put(Float.intBitsToFloat(this.jte));
		}
	}

    @OnlyIn(Dist.CLIENT)
    public record ElemInfo(int poolId, int partId) implements ComputeShaderSetup.BufferUploadable {
        @Override
        public void store(FloatBuffer buffer) {
            buffer.put(Float.intBitsToFloat(this.poolId));
            buffer.put(Float.intBitsToFloat(this.partId));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class PartBuffer implements MeshPartBuffer {
        private final int partIdx;

        public PartBuffer(List<VertexBuilder> vertexBuilders, Map<VertexBuilder, Integer> vertexBuilderMap, float[] uvs, List<Float> uvList, List<ElemInfo> elements, int partIdx) {
            this.partIdx = partIdx;

            for (VertexBuilder vb : vertexBuilders) {
                if (!vertexBuilderMap.containsKey(vb)) {
                    int next = vertexBuilderMap.size();
                    vertexBuilderMap.put(vb, next);

                    uvList.add(uvs[vb.uv * 2]);
                    uvList.add(uvs[vb.uv * 2 + 1]);
                }

                int vertexPoolIndex = vertexBuilderMap.get(vb);

                elements.add(new ElemInfo(vertexPoolIndex, partIdx));
            }
        }

        @Override
        public int vboId() {
            return -1;
        }

        @Override
        public int partIdx() {
            return this.partIdx;
        }
    }

}
