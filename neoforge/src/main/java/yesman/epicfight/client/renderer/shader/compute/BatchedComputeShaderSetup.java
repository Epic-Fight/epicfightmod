package yesman.epicfight.client.renderer.shader.compute;

import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.ssbo.DynamicSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.ssbo.IArrayBufferProxy;
import yesman.epicfight.client.renderer.shader.compute.backend.ssbo.OutputSSBO;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderLoader;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

import java.nio.FloatBuffer;

// bated rendering is not complete
@SuppressWarnings("Unfinished")
public abstract class BatchedComputeShaderSetup extends ComputeShaderSetup{
    protected static OpenMatrix4f[] POSES_DATA = null;
    protected static final ModelInfo[] MODEL_INFOS = ModelInfo.allocate(256);
    protected static final Integer[] HIDDEN_FLAGS = new Integer[512];

    protected static IArrayBufferProxy POSES_DATA_BO = null;
    protected static final IArrayBufferProxy MODEL_INFOS_BO = ComputeShaderProvider.createDynamicBuffer(
        MODEL_INFOS,
        16,
        ModelInfo::store
    );

    protected static final IArrayBufferProxy HIDDEN_FLAGS_BO = ComputeShaderProvider.createDynamicBuffer(
        HIDDEN_FLAGS, 1,
        (v, b) -> b.put(Float.intBitsToFloat(v))
    );

    public BatchedComputeShaderSetup(SkinnedMesh skinnedMesh, int outBufferSize) {
        super(skinnedMesh, 1);

        if (POSES_DATA == null) {
            int len = (int)Math.max(8192, ComputeShaderLoader.getGLMaxSSBOSize() / (16 * 4));
            POSES_DATA = OpenMatrix4f.allocateMatrixArray(len);
            POSES_DATA_BO = ComputeShaderProvider.createDynamicBuffer(
                POSES_DATA,
                16,
                OpenMatrix4f::store
            );
        }

        this.outVertexAttrBO.close();

        int max = (int) Math.max(ComputeShaderLoader.getGLMaxSSBOSize() / 4, 1024 * 1024 * 1024 / 4);
        this.outVertexAttrBO = new OutputSSBO((short) outBufferSize, max, DynamicSSBO.DataMode.STREAM);
    }

    public static class ModelInfo {
        private final int poseOffset = 0;
        private final int hiddenOffset = 0;

        public ModelInfo() {
        }

        public static ModelInfo[] allocate(int size) {
            ModelInfo[] arr = new ModelInfo[size];

            for (int i = 0; i < size; i++) {
                arr[i] = new ModelInfo();
            }

            return arr;
        }

        public void store(FloatBuffer buf){
            buf.put(Float.intBitsToFloat(this.poseOffset));
            buf.put(Float.intBitsToFloat(this.hiddenOffset));
        }
    }
}
