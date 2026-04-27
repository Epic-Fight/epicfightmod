package yesman.epicfight.client.renderer.shader.compute;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL43C;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.GLConstants;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.ssbo.*;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.main.EpicFightSharedConstants;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL20C.glUseProgram;

public abstract class ComputeShaderSetup {
    protected static final int WORK_GROUP_SIZE = 128;

    public static final OpenMatrix4f[] TOTAL_POSES = OpenMatrix4f.allocateMatrixArray(EpicFightSharedConstants.MAX_JOINTS);
    public static final OpenMatrix4f[] TOTAL_NORMALS = OpenMatrix4f.allocateMatrixArray(EpicFightSharedConstants.MAX_JOINTS);


    protected static final int unmappedBufferPoolSize = 32;
    protected static final ArrayProxyPool POSE_BO_POOL = new ArrayProxyPool(() ->
            ComputeShaderProvider.createDynamicBuffer(TOTAL_POSES, 16, OpenMatrix4f::store)
            , unmappedBufferPoolSize); // PoseBuffersPool


    protected static final Integer[] HF = new Integer[8];
    protected static final ArrayProxyPool HF_BO_POOL = new ArrayProxyPool(() ->
            ComputeShaderProvider.createDynamicBuffer(HF, 1, (v, b) -> b.put(Float.intBitsToFloat(v)))
            , unmappedBufferPoolSize); // PoseBuffersPool

    protected IArrayBufferProxy curr_POSE_BO;
    protected IArrayBufferProxy curr_hiddenFlagsBO;

    protected final StaticSSBO<VertexObj> vObjBO; // VertexBuffer
    protected final StaticSSBO<WeightInfo> jointBO;
    protected final StaticSSBO<ElemInfo> elementsBO; // ElementsPool
    protected OutputSSBO outVertexAttrBO;

    //protected final IArrayBufferProxy hiddenFlagsBO;
    protected final Integer[] hiddenFlags;

    protected final int arrayObjectId;
    protected final int vcount;

    public ComputeShaderSetup(SkinnedMesh skinnedMesh, int outBufferSize) {
        Map<VertexBuilder, Integer> vertexBuilderMap = new HashMap<>();
        List<ElemInfo> elements = new ArrayList<>();

        this.arrayObjectId = GlStateManager._glGenVertexArrays();
        int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
        int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);
        GlStateManager._glBindVertexArray(this.arrayObjectId);

        List<Float> uvList = Lists.newArrayList();
        this.hiddenFlags = new Integer[(skinnedMesh.getAllParts().size() + 31) / 32];
        //this.hiddenFlagsBO = ComputeShaderProvider.createDynamicBuffer(this.hiddenFlags, 1, (v, b) -> b.put(Float.intBitsToFloat(v)));

        MutableInt partIdx = new MutableInt(0);

        skinnedMesh.getAllParts().forEach(skinnedMeshPart -> {
            skinnedMeshPart.initVBO(new PartBuffer(skinnedMeshPart.getVertices(), vertexBuilderMap, skinnedMesh.uvs(), uvList, elements, partIdx.intValue()));
            partIdx.add(1);
        });

        VertexObj[] vertexObjs = new VertexObj[vertexBuilderMap.size()];
        List<WeightInfo> jointList = new ArrayList<> ();

        vertexBuilderMap.forEach((vb, idx) -> {
            int startPos = jointList.size();

            for (int i = 0; i < skinnedMesh.affectingJointCounts()[vb.position]; i++) {
                int jointIndex = skinnedMesh.affectingJointIndices()[vb.position][i];
                int weightIndex = skinnedMesh.affectingWeightIndices()[vb.position][i];
                float weight = skinnedMesh.weights()[weightIndex];
                jointList.add(new WeightInfo(jointIndex, weight));
            }

            vertexObjs[idx] = new VertexObj(
                skinnedMesh.positions()[vb.position * 3],
                skinnedMesh.positions()[vb.position * 3 + 1],
                skinnedMesh.positions()[vb.position * 3 + 2],
                skinnedMesh.normals()[vb.normal * 3],
                skinnedMesh.normals()[vb.normal * 3 + 1],
                skinnedMesh.normals()[vb.normal * 3 + 2],
                skinnedMesh.uvs()[vb.uv*2],
                skinnedMesh.uvs()[vb.uv*2+1],
                startPos,
                startPos + skinnedMesh.affectingJointCounts()[vb.position]
            );
        });

        this.initAttachmentSSBO(elements, uvList);

        this.vcount = elements.size();

        this.elementsBO = new StaticSSBO<>(elements, 2, ElemInfo::store);
        this.vObjBO = new StaticSSBO<> (Lists.newArrayList(vertexObjs), 10, VertexObj::store);
        this.jointBO = new StaticSSBO<> (jointList, 2, WeightInfo::store);

        this.outVertexAttrBO = new OutputSSBO((short) outBufferSize, elements.size(), DynamicSSBO.DataMode.STREAM);

        GlStateManager._glBindVertexArray(currentBoundVao);
        GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
    }

    protected void initAttachmentSSBO(List<ElemInfo> elements, List<Float> uvList) {
    }

    public static void setShaderDefaultUniforms(ShaderInstance shader, VertexFormat.Mode mode, Window window) {
        for (int i = 0; i < 12; i++) {
            int j = RenderSystem.getShaderTexture(i);
            shader.setSampler("Sampler" + i, j);
        }

        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
        }

        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
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

    static void clearBufferState(VertexFormat vertexFormat) {
        vertexFormat.clearBufferState();
    }

    public abstract void bindBufferFormat(VertexFormat vertexFormat);

    public abstract void applyComputeShader(PoseStack poseStack, float r, float g, float b, float a, int overlay, int light, int jointCount);

    public abstract void drawWithShader(SkinnedMesh skinnedMesh, PoseStack poseStack, MultiBufferSource buffers, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses);

    public abstract int vaoId();

    public abstract int vertexCount();

    public void destroyBuffers() {
        this.vObjBO.close();
        this.jointBO.close();
        this.elementsBO.close();
        //this.hiddenFlagsBO.close();
        RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
    }

    protected void draw(PoseStack poseStack, RenderType renderType, float r, float g, float b, float a, int overlay, int packedLight, int joints) {
        renderType.setupRenderState();

        var mode = renderType.mode();
        ShaderInstance shader = RenderSystem.getShader();
        var format = shader.getVertexFormat();

        this.bindBufferFormat(format);

        ComputeShaderSetup.setShaderDefaultUniforms(shader, mode, Minecraft.getInstance().getWindow());
        shader.apply();

        this.applyComputeShader(poseStack, r, g, b, a, overlay, packedLight, joints);
        // draw call

        //GL43C.glMemoryBarrier(GL43C.GL_ALL_BARRIER_BITS);
        //GlStateManager._glBindVertexArray(this.arrayObjectId);
        glUseProgram(RenderSystem.getShader().getId());
        glDrawArrays(VertexFormat.Mode.TRIANGLES.asGLMode, 0, this.vcount);
        //GL33C.glFinish();

        // state restore
        RenderSystem.getShader().clear();
        renderType.clearRenderState();
        format.clearBufferState();

    }

    public interface BufferUploadable {
        public void store(FloatBuffer buffer);
    }

    public interface MeshPartBuffer {
        // For vanilla compute shader
        int vboId();

        // For iris compute shader
        int partIdx();
    }

    // VertexData
    public record VertexObj(
        float px, float py, float pz,
        float nx, float ny, float nz,
        float u, float v,
        int jts, int jte
    ) implements BufferUploadable {
        @Override
        public void store(FloatBuffer floatBuffer) {
            floatBuffer.put(this.px);
            floatBuffer.put(this.py);
            floatBuffer.put(this.pz);

            floatBuffer.put(this.nx);
            floatBuffer.put(this.ny);
            floatBuffer.put(this.nz);

            floatBuffer.put(this.u);
            floatBuffer.put(this.v);

            floatBuffer.put(Float.intBitsToFloat(this.jts));
            floatBuffer.put(Float.intBitsToFloat(this.jte));
        }
    }

    public record ElemInfo(int poolId, int partId) implements BufferUploadable {
        @Override
        public void store(FloatBuffer buffer) {
            buffer.put(Float.intBitsToFloat(this.poolId));
            buffer.put(Float.intBitsToFloat(this.partId));
        }
    }

    public record WeightInfo(int jtId, float weight) implements BufferUploadable {
        @Override
        public void store(FloatBuffer buffer) {
            buffer.put(Float.intBitsToFloat(this.jtId));
            buffer.put(weight);
        }
    }

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
