package yesman.epicfight.client.renderer.shader.compute;

import static org.lwjgl.opengl.GL11C.GL_BYTE;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glVertexAttribIPointer;

import java.util.Arrays;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.SkinnedMesh.SkinnedMeshPart;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.GLConstants;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

public class VanillaComputeShaderSetup extends ComputeShaderSetup {
	
	public VanillaComputeShaderSetup(SkinnedMesh skinnedMesh) {
        super(skinnedMesh, 12);
    }
	
	@Override
	public void bindBufferFormat(VertexFormat vertexFormat) {
		var elems = vertexFormat.getElements();
		glBindBuffer(GL_ARRAY_BUFFER, this.outVertexAttrBO.glSSBO);
		
		for (int i = 0; i < elems.size(); ++i) {
			VertexFormatElement elem = elems.get(i);
			
			if (elem == DefaultVertexFormat.ELEMENT_POSITION) {
				glVertexAttribPointer(i, 3, GL_FLOAT, false, 48, 0);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV) {
				glVertexAttribPointer(i, 2, GL_FLOAT, false, 48, 28);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_COLOR) {
				glVertexAttribPointer(i, 4, GL_FLOAT, true, 48, 12);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_NORMAL) {
				glVertexAttribPointer(i, 3, GL_BYTE, true, 48, 36);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV1) {
				glVertexAttribIPointer(i, 2, GL_UNSIGNED_SHORT, 48, 40);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV2) {
				glVertexAttribIPointer(i, 2, GL_UNSIGNED_SHORT, 48, 44);
				glEnableVertexAttribArray(i);
			}
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void applyComputeShader(PoseStack poseStack, float r, float g, float b, float a, int overlay, int light, int jointCount) {
		ComputeProgram shader = ComputeShaderProvider.meshComputeVanilla;
		shader.useProgram();
		shader.getUniform("colorIn").uploadVec4(r, g, b, a);
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
		
		this.draw(poseStack, renderType, RenderSystem.getModelViewMatrix(), r, g, b, a, overlay, packedLight, poses.length);
		
		if (buffers instanceof OutlineBufferSource outlineBufferSource) {
			renderType.outline().ifPresent(outlineRendertype -> {
				this.draw(poseStack, outlineRendertype, RenderSystem.getModelViewMatrix(), outlineBufferSource.teamR / 255.0F, outlineBufferSource.teamG / 255.0F, outlineBufferSource.teamB / 255.0F, outlineBufferSource.teamA / 255.0F, overlay, packedLight, poses.length);
			});
		}
		
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
