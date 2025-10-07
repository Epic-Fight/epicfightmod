package yesman.epicfight.client.renderer.shader.compute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.opengl.*;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.SkinnedMesh.SkinnedMeshPart;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.GLConstants;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.DynamicSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.IArrayBufferProxy;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.OutputSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.StaticSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

@OnlyIn(Dist.CLIENT)
public class VanillaComputeShaderSetup extends ComputeShaderSetup {

	public VanillaComputeShaderSetup(SkinnedMesh skinnedMesh) {
        super(skinnedMesh, 12);
    }

	@Override
	public void bindBufferFormat(VertexFormat vertexFormat) {
		var elems = vertexFormat.getElements();
		GL43C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, outVertexAttrBO.glSSBO);
		for (int i = 0; i < elems.size(); ++i) {
			VertexFormatElement elem = elems.get(i);
			
			if (elem == DefaultVertexFormat.ELEMENT_POSITION) {
				//ComputeShaderSetup.bindAttrPointer(buffers[0], 3, i, GL11C.GL_FLOAT);
				GL43C.glVertexAttribPointer(i, 3, GL43C.GL_FLOAT, false, 48, 0);
				GL43C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV) {
				//ComputeShaderSetup.bindAttrPointer(buffers[3], 2, i, GL11C.GL_FLOAT);
				GL43C.glVertexAttribPointer(i, 2, GL43C.GL_FLOAT, false, 48, 28);
				GL43C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_COLOR) {
				GL43C.glVertexAttribPointer(i, 4, GL43C.GL_FLOAT, true, 48, 12);
				GL43C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_NORMAL) {
				GL43C.glVertexAttribPointer(i, 3, GL43C.GL_BYTE, true, 48, 36);
				GL43C.glEnableVertexAttribArray(i);

			} else if (elem == DefaultVertexFormat.ELEMENT_UV1) {
				GL43C.glVertexAttribIPointer(i, 2, GL43C.GL_UNSIGNED_SHORT, 48, 40);
				GL43C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV2) {
				GL43C.glVertexAttribIPointer(i, 2, GL43C.GL_UNSIGNED_SHORT, 48, 44);
				GL43C.glEnableVertexAttribArray(i);
			}
		}
		
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void applyComputeShader(PoseStack poseStack, OpenMatrix4f partTransform, float r, float g, float b, float a, int overlay, int light, int jointCount) {
		ComputeProgram shader = ComputeShaderProvider.meshComputeVanilla;
		shader.useProgram();
		shader.getUniform("colorIn").uploadVec4(r,g,b,a);
		shader.getUniform("uv1In").uploadUnsignedInt(overlay);
		shader.getUniform("uv2In").uploadUnsignedInt(light);
		shader.getUniform("part_offset").uploadUnsignedInt(jointCount);
		shader.getUniform("model_view_matrix").uploadMatrix4f(poseStack.last().pose());
		shader.getUniform("normal_matrix").uploadMatrix3f(poseStack.last().normal());
		
		ComputeShaderSetup.POSE_BO.bindBufferBase(0);

		this.elementsBO.bindBufferBase(1);
		this.vObjBO.bindBufferBase(2);
		this.jointBO.bindBufferBase(3);
		this.hiddenFlagsBO.bindBufferBase(4);
		this.outVertexAttrBO.bindBufferBase(5);

		int workGroupCount = (this.vcount + WORK_GROUP_SIZE - 1) / WORK_GROUP_SIZE;
		shader.dispatch(workGroupCount, 1, 1);
		shader.waitBarriers();

		ComputeShaderSetup.POSE_BO.unbind();
		this.elementsBO.unbind();
		this.vObjBO.unbind();
		this.jointBO.unbind();
		this.hiddenFlagsBO.unbind();
		this.outVertexAttrBO.unbind();
	}
	
	@Override
	public void drawWithShader(SkinnedMesh skinnedMesh, PoseStack poseStack, MultiBufferSource buffers, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		// pose setup and upload
		for (int i = 0; i < poses.length; i++) {
			TOTAL_POSES[i].load(poses[i]);

			if (armature != null) {
				TOTAL_POSES[i].mulBack(armature.searchJointById(i).getToOrigin());
			}
		}

		Arrays.fill(this.hiddenFlags, 0);

		for (SkinnedMeshPart part : skinnedMesh.getAllParts()) {
			OpenMatrix4f mat = part.getVanillaPartTransform();
			if (mat == null) mat = OpenMatrix4f.IDENTITY;
			TOTAL_POSES[poses.length + part.getPartVBO().partIdx()].load(mat);

			if (!part.isHidden()) continue;

			int flagPos = part.getPartVBO().partIdx() / 32;
			int flagOffset = part.getPartVBO().partIdx() % 32;
			int flag = this.hiddenFlags[flagPos];
			this.hiddenFlags[flagPos] = flag | ((part.isHidden() ? 1:0) << flagOffset);
		}

		this.hiddenFlagsBO.updateAll();
		POSE_BO.updateFromTo(0, poses.length + skinnedMesh.getAllParts().size());

		// state trace
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);

		// setup state
		GlStateManager._glBindVertexArray(this.arrayObjectId);
		renderType.setupRenderState();

		var mode = renderType.mode();
		
		ShaderInstance shader = RenderSystem.getShader();
		var format = shader.getVertexFormat();

		this.bindBufferFormat(format);

		ComputeShaderSetup.setShaderDefaultUniforms(RenderSystem.getModelViewMatrix(), shader, mode, Minecraft.getInstance().getWindow());
		shader.apply();
		
		this.applyComputeShader(poseStack, null, r, g, b, a, overlay, packedLight, poses.length);
		
		// draw call
		GL20C.glUseProgram(RenderSystem.getShader().getId());
		GL11C.glDrawArrays(VertexFormat.Mode.TRIANGLES.asGLMode, 0, vcount);
		
		// state restore
		RenderSystem.getShader().clear();
		renderType.clearRenderState();
		
		if (buffers instanceof OutlineBufferSource outlineBufferSource) {
			renderType.outline().ifPresent(outlineRendertype -> {
				outlineRendertype.setupRenderState();
				
				var outlinemode = outlineRendertype.mode();
				ShaderInstance outlineshader = RenderSystem.getShader();
				var outlineformat = outlineshader.getVertexFormat();
				
				this.bindBufferFormat(outlineformat);
				
				ComputeShaderSetup.setShaderDefaultUniforms(RenderSystem.getModelViewMatrix(), outlineshader, outlinemode, Minecraft.getInstance().getWindow());
				outlineshader.apply();
				
				this.applyComputeShader(poseStack, null, outlineBufferSource.teamR / 255.0F, outlineBufferSource.teamG / 255.0F, outlineBufferSource.teamB / 255.0F, outlineBufferSource.teamA / 255.0F, overlay, packedLight, poses.length);
				
				// draw call
				GL20C.glUseProgram(RenderSystem.getShader().getId());
				GL11C.glDrawArrays(VertexFormat.Mode.TRIANGLES.asGLMode, 0, this.vcount);
				
				// state restore
				RenderSystem.getShader().clear();
				
				outlineRendertype.clearRenderState();
			});
		}
		
		format.clearBufferState();

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}
	


	
	@Override
	public int vaoId() {
		return this.arrayObjectId;
	}
	
	@Override
	public int vertexCount() {
		return this.vcount;
	}
}
