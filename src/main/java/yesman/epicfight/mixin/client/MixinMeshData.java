package yesman.epicfight.mixin.client;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.apache.commons.lang3.mutable.MutableLong;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexSorting;

import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.minecraft.util.Mth;

@Mixin(value = MeshData.class)
public class MixinMeshData {
	@Shadow
	@Final
	private MeshData.DrawState drawState;
	
	@Shadow
    private ByteBufferBuilder.Result indexBuffer;
	
	@Shadow
	@Final
	private ByteBufferBuilder.Result vertexBuffer;
	
	@Inject(at = @At(value = "HEAD"), method = "sortQuads(Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;Lcom/mojang/blaze3d/vertex/VertexSorting;)Lcom/mojang/blaze3d/vertex/MeshData$SortState;", cancellable = true)
    public void epicfight$sortQuads(ByteBufferBuilder bufferBuilder, VertexSorting sorting, CallbackInfoReturnable<MeshData.SortState> callback) {
		if (this.drawState.mode() == VertexFormat.Mode.TRIANGLES) {
			
			Vector3f[] centroids = unpackTriangleCentroids(this.vertexBuffer.byteBuffer(), this.drawState.vertexCount(), this.drawState.mode().primitiveStride, this.drawState.format());
            int[] aint = sorting.sort(centroids);
            long i = bufferBuilder.reserve(aint.length * 6 * this.drawState.indexType().bytes);
            IntConsumer intconsumer = indexWriter(i, this.drawState.indexType());

            for (int j : aint) {
                intconsumer.accept(j * this.drawState.mode().primitiveStride + 0);
                intconsumer.accept(j * this.drawState.mode().primitiveStride + 1);
                intconsumer.accept(j * this.drawState.mode().primitiveStride + 2);
            }
            
            this.indexBuffer = bufferBuilder.build();
            
            // No triangle render type for Chunk renderer, 
			callback.setReturnValue(null);
			callback.cancel();
		}
	}
	
	private static IntConsumer indexWriter(long index, VertexFormat.IndexType type) {
        MutableLong mutablelong = new MutableLong(index);

        return switch (type) {
            case SHORT -> p_350656_ -> MemoryUtil.memPutShort(mutablelong.getAndAdd(2L), (short)p_350656_);
            case INT -> p_350913_ -> MemoryUtil.memPutInt(mutablelong.getAndAdd(4L), p_350913_);
        };
    }
	
	private static Vector3f[] unpackTriangleCentroids(ByteBuffer byteBuffer, int vertexCount, int primitiveStride, VertexFormat format) {
        int i = format.getOffset(VertexFormatElement.POSITION);
        
        if (i == -1) {
            throw new IllegalArgumentException("Cannot identify quad centers with no position element");
        } else {
            FloatBuffer floatbuffer = byteBuffer.asFloatBuffer();
            int j = format.getVertexSize() / 4;
            int k = j * primitiveStride;
            int l = vertexCount / primitiveStride;
            Vector3f[] avector3f = new Vector3f[l];
            
            for (int i1 = 0; i1 < l; i1++) {
                int v1 = i + i1 * k;
                int v2 = v1 + j;
                int v3 = v1 + j * 2;
                float x1 = floatbuffer.get(v1 + 0);
    			float y1 = floatbuffer.get(v1 + 1);
    			float z1 = floatbuffer.get(v1 + 2);
    			float x2 = floatbuffer.get(v2 + 0);
    			float y2 = floatbuffer.get(v2 + 1);
    			float z2 = floatbuffer.get(v2 + 2);
    			float x3 = floatbuffer.get(v3 + 0);
    			float y3 = floatbuffer.get(v3 + 1);
    			float z3 = floatbuffer.get(v3 + 2);
                
    			avector3f[i1] = triangleCentroid(x1, y1, z1, x2, y2, z2, x3, y3, z3);
            }

            return avector3f;
        }
    }
	
	private static Vector3f triangleCentroid(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
		float[] lineLength = new float[3];
		lineLength[0] = Mth.sqrt(Mth.square(x2 - x1) + Mth.square(y2 - y1) + Mth.square(z2 - z1));
		lineLength[1] = Mth.sqrt(Mth.square(x3 - x2) + Mth.square(y3 - y2) + Mth.square(z3 - z2));
		lineLength[2] = Mth.sqrt(Mth.square(x1 - x3) + Mth.square(y1 - y3) + Mth.square(z1 - z3));
		int longest = 0;
		
		for (int i = 1; i < 3; i++) {
			if (lineLength[i] > lineLength[longest]) {
				longest = i;
			}
		}
		
		switch (longest) {
		case 0:
			return new Vector3f((x1 + x2) * 0.5F, (y1 + y2) * 0.5F, (z1 + z2) * 0.5F);
		case 1:
			return new Vector3f((x2 + x3) * 0.5F, (y2 + y3) * 0.5F, (z2 + z3) * 0.5F);
		case 2:
			return new Vector3f((x3 + x1) * 0.5F, (y3 + y1) * 0.5F, (z3 + z1) * 0.5F);
		}
		
		return null;
	}
}
