package yesman.epicfight.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import yesman.epicfight.api.utils.math.Vec3f;

public class RenderingTool {
	public static void drawQuad(PoseStack poseStack, VertexConsumer vertexBuilder, Vec3f pos, float size, float r, float g, float b) {
		vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y, pos.z - size).color(r, g, b, 1.0F).endVertex();
	}
	
	public static void drawCube(PoseStack poseStack, VertexConsumer vertexBuilder, Vec3f pos, float size, float r, float g, float b) {
		vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y - size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y - size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y - size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y - size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y + size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y + size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y + size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y + size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y + size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y + size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y - size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y - size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y + size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y + size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y - size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y - size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y + size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y + size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y - size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y - size, pos.z - size).color(r, g, b, 1.0F).endVertex();
        
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y + size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y + size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x - size, pos.y - size, pos.z + size).color(r, g, b, 1.0F).endVertex();
        vertexBuilder.vertex(poseStack.last().pose(), pos.x + size, pos.y - size, pos.z + size).color(r, g, b, 1.0F).endVertex();
	}
}