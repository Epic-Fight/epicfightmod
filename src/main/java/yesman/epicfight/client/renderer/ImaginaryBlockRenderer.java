package yesman.epicfight.client.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class ImaginaryBlockRenderer {
	private static final Direction[] DIRECTIONS = Direction.values();
	
	public void render(Camera camera, PoseStack poseStack, MultiBufferSource buffers, Level level, BlockPos bp, float r, float g, float b, float a) {
		BlockState bs = level.getBlockState(bp);
		BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(bs);
		RandomSource randomsource = RandomSource.create();
		long seed = bs.getSeed(bp);
		randomsource.setSeed(seed);
		
		poseStack.pushPose();
		
		Vec3 cameraPosition = camera.getPosition();
		poseStack.translate(bp.getX() - cameraPosition.x, bp.getY() - cameraPosition.y, bp.getZ() - cameraPosition.z);
		
		level.getLightEmission(bp);
		
		VertexConsumer buffer = buffers.getBuffer(RenderType.debugSectionQuads());
		BlockPos.MutableBlockPos mutablepos = bp.mutable();
		
		for (Direction d : DIRECTIONS) {
			List<BakedQuad> culledFaces = model.getQuads(bs, d, randomsource, ModelData.EMPTY, null);
			mutablepos.setWithOffset(bp, d);
			
			if (Block.shouldRenderFace(bs, level, bp, d, mutablepos)) {
				this.renderPreviewBlocks(poseStack, buffer, culledFaces);
			}
		}
		
		this.renderPreviewBlocks(poseStack, buffer, model.getQuads(bs, null, randomsource, ModelData.EMPTY, null));
		
		poseStack.popPose();
		
		((MultiBufferSource.BufferSource)buffers).endBatch();
	}
	
	private void renderPreviewBlocks(PoseStack poseStack, VertexConsumer consumer, List<BakedQuad> quads) {
		for (BakedQuad bakedquad : quads) { 
			int[] vertices = bakedquad.getVertices();
			Matrix4f matrix4f = poseStack.last().pose();
			
			try (MemoryStack memorystack = MemoryStack.stackPush()) {
				ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
				IntBuffer intbuffer = bytebuffer.asIntBuffer();

				for (int k = 0; k < vertices.length / 8; ++k) {
					intbuffer.clear();
					intbuffer.put(vertices, k * 8, 8);
					float f = bytebuffer.getFloat(0);
					float f1 = bytebuffer.getFloat(4);
					float f2 = bytebuffer.getFloat(8);
					
					Vector4f vector4f = matrix4f.transform(new Vector4f(f, f1, f2, 1.0F));
					consumer.vertex(vector4f.x(), vector4f.y(), vector4f.z());
					consumer.color(255, 255, 255, 44);
					consumer.endVertex();
				}
			}
		}
	}
}
