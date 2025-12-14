package yesman.epicfight.client.renderer.shader.compute.iris;

import static org.lwjgl.opengl.GL11C.GL_BYTE;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glVertexAttribIPointer;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.SkinnedMesh.SkinnedMeshPart;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.GLConstants;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.shader.compute.ComputeShaderSetup;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.StaticSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;

public class IrisComputeShaderSetup extends ComputeShaderSetup {
	protected StaticSSBO<Float> midUVBO;
	
	public IrisComputeShaderSetup(SkinnedMesh skinnedMesh) {
		super(skinnedMesh, 15);
	}
	
	@Override
	protected void initAttachmentSSBO(List<ElemInfo> elements, List<Float> uvList) {
		List<Float> midUVList = Lists.newArrayList();
		float[] midUVs = new float[(elements.size() / 3) * 2];
		
		for (int i = 0; i < elements.size(); i++) {
			int vertPoolIdx = elements.get(i).poolId();
			float u = uvList.get(vertPoolIdx * 2);
			float v = uvList.get(vertPoolIdx * 2 + 1);
			int faceIdx = i / 3;

			if (i % 3 == 0) {
				midUVs[faceIdx * 2] = u / 3;
				midUVs[faceIdx * 2 + 1] = v / 3;
			} else {
				midUVs[faceIdx * 2] += u / 3;
				midUVs[faceIdx * 2 + 1] += v / 3;
			}
		}
		
		for (int i = 0; i < elements.size(); i++) {
			int faceIdx = i / 3;
			midUVList.add(midUVs[faceIdx * 2]);
			midUVList.add(midUVs[faceIdx * 2 + 1]);
		}
		
		this.midUVBO = new StaticSSBO<> (midUVList, 1, (v, b) -> b.put(v));
	}
	
	@Override
	public void bindBufferFormat(VertexFormat vertexFormat) {
		var elems = vertexFormat.getElements();
		
		glBindBuffer(GL_ARRAY_BUFFER, outVertexAttrBO.glSSBO);
		
		int midUvPos = -1;
		
		for (int i = 0; i < elems.size(); ++i) {
			VertexFormatElement elem = elems.get(i);

			if (elem == DefaultVertexFormat.ELEMENT_POSITION) {
				glVertexAttribPointer(i, 3, GL_FLOAT, false, 60, 0);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV) {
				glVertexAttribPointer(i, 2, GL_FLOAT, false, 60, 28);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_COLOR) {
				glVertexAttribPointer(i, 4, GL_FLOAT, true, 60, 12);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_NORMAL) {
				glVertexAttribPointer(i, 3, GL_BYTE, true, 60, 36);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV1) {
				glVertexAttribIPointer(i, 2, GL_UNSIGNED_SHORT, 60, 40);
				glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV2) {
				glVertexAttribIPointer(i, 2, GL_UNSIGNED_SHORT, 60, 44);
				glEnableVertexAttribArray(i);
			}
			// iris part
			else if (elem == IrisVertexFormats.ENTITY_ID_ELEMENT) {
				glVertexAttribIPointer(i, 3, GL_UNSIGNED_SHORT, 60, 48);
				glEnableVertexAttribArray(i);
			} else if (elem == IrisVertexFormats.MID_TEXTURE_ELEMENT) {
				midUvPos = i;
			} else if (elem == IrisVertexFormats.TANGENT_ELEMENT) {
				glVertexAttribPointer(i, 4, GL_BYTE, false, 60, 56);
				glEnableVertexAttribArray(i);
			}
		}
		
		if (midUvPos >= 0) {
			glBindBuffer(GL_ARRAY_BUFFER, midUVBO.glSSBO);
			glVertexAttribPointer(midUvPos, 2, GL_FLOAT, false, 0, 0);
			glEnableVertexAttribArray(midUvPos);
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	private static final Matrix4f IDENTITY4X4 = new Matrix4f();
	private static final Matrix3f IDENTITY3X3 = new Matrix3f();
	
	@Override
	public void applyComputeShader(PoseStack poseStack, float r, float g, float b, float a, int overlay, int light, int jointCount) {
		// shader setup
		ComputeProgram shader = ComputeShaderProvider.meshComputeIris;
		shader.useProgram();
		shader.getUniform("colorIn").uploadVec4(r, g, b, a);
		shader.getUniform("uv1In").uploadUnsignedInt(overlay);
		shader.getUniform("uv2In").uploadUnsignedInt(light);
		shader.getUniform("part_offset").uploadUnsignedInt(jointCount);
		shader.getUniform("entity_id_0").uploadUnsignedInt(((getEntity() << 16) & 0xFFFF0000) | (getBlock() & 0xFFFF));
		shader.getUniform("entity_id_1").uploadUnsignedInt(getItem() << 16);
		
		if (!Iris.getIrisConfig().areShadersEnabled()) {
			shader.getUniform("model_view_matrix").uploadMatrix4f(poseStack.last().pose());
			shader.getUniform("normal_matrix").uploadMatrix3f(poseStack.last().normal());
		} else {
			shader.getUniform("model_view_matrix").uploadMatrix4f(IDENTITY4X4);
			shader.getUniform("normal_matrix").uploadMatrix3f(IDENTITY3X3);
		}
		
		ComputeShaderSetup.POSE_BO.bindBufferBase(0);
		
		this.elementsBO.bindBufferBase(1);
		this.vObjBO.bindBufferBase(2);
		this.jointBO.bindBufferBase(3);
		this.hiddenFlagsBO.bindBufferBase(4);
		this.outVertexAttrBO.bindBufferBase(5);
		
		int workGroupCount = ((this.vcount / 3) + WORK_GROUP_SIZE - 1) / WORK_GROUP_SIZE;
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
		Matrix4f frustumMatrix = Iris.getIrisConfig().areShadersEnabled() ? poseStack.last().pose() : RenderSystem.getModelViewMatrix();
		
		this.draw(poseStack, renderType, frustumMatrix, r, g, b, a, overlay, packedLight, poses.length);
		
		if (buffers instanceof OutlineBufferSource outlineBufferSource) {
			renderType.outline().ifPresent(outlineRendertype -> {
				this.draw(poseStack, outlineRendertype, frustumMatrix, outlineBufferSource.teamR / 255.0F, outlineBufferSource.teamG / 255.0F, outlineBufferSource.teamB / 255.0F, outlineBufferSource.teamA / 255.0F, overlay, packedLight, poses.length);
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
	public void destroyBuffers() {
		this.midUVBO.close();
		super.destroyBuffers();
	}

	@Override
	public int vertexCount() {
		return this.vcount;
	}
	
    static short getBlock() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
    }

	static short getEntity() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
    }

	static short getItem() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem();
    }
}
