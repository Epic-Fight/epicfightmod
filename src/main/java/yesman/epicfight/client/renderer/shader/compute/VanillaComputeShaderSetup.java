package yesman.epicfight.client.renderer.shader.compute;

import java.util.*;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.opengl.*;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.minecraft.client.Minecraft;
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
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

@OnlyIn(Dist.CLIENT)
public class VanillaComputeShaderSetup implements ComputeShaderSetup {
	private final StaticSSBO<Float> uvsBO;
	private final StaticSSBO<VertexObj> vObjBO;
	private final StaticSSBO<Integer> jointBO;
	private	final StaticSSBO<Float> weightBO;
	private final StaticSSBO<ElemInfo> elementsBO;

	private final OutputSSBO outPos;
	private final OutputSSBO outNormal;
	private final OutputSSBO outColor;
	private final OutputSSBO outUv0;
	private final OutputSSBO outUv1;
	private final OutputSSBO outUv2;

	private final IArrayBufferProxy hiddenFlagsBO;
	private final Integer[] hiddenFlags;
	
	private final int arrayObjectId;
	private final int vcount;
	
	public VanillaComputeShaderSetup(SkinnedMesh skinnedMesh) {
		Map<VertexBuilder, Integer> vertexBuilderMap = new HashMap<> ();
		List<ElemInfo> elements = new ArrayList<> ();

		this.arrayObjectId = GlStateManager._glGenVertexArrays();
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);
		GlStateManager._glBindVertexArray(this.arrayObjectId);

		List<Float> uvList = Lists.newArrayList();
		this.hiddenFlags = new Integer[(skinnedMesh.getAllParts().size() + 31) / 32];
		this.hiddenFlagsBO = ComputeShaderProvider.createDynamicBuffer(this.hiddenFlags, 1, (v, b) -> b.put(Float.intBitsToFloat(v)));

		MutableInt partIdx = new MutableInt(0);

		skinnedMesh.getAllParts().forEach(skinnedMeshPart -> {
			skinnedMeshPart.initVBO(new PartBuffer(skinnedMeshPart.getVertices(), vertexBuilderMap, skinnedMesh.uvs(), uvList, elements, partIdx.intValue()));
			partIdx.add(1);
		});

		VertexObj[] vertexObjs = new VertexObj[vertexBuilderMap.size()];
		List<Integer> jointList = new ArrayList<> ();
		List<Float> weightList = new ArrayList<> ();

		vertexBuilderMap.forEach((vb, idx) -> {
			int startPos = jointList.size();

			for (int i = 0; i < skinnedMesh.affectingJointCounts()[vb.position]; i++) {
				int jointIndex = skinnedMesh.affectingJointIndices()[vb.position][i];
				int weightIndex = skinnedMesh.affectingWeightIndices()[vb.position][i];
				float weight = skinnedMesh.weights()[weightIndex];

				jointList.add(jointIndex);
				weightList.add(weight);
			}

			vertexObjs[idx] = new VertexObj(
					skinnedMesh.positions()[vb.position * 3],
					skinnedMesh.positions()[vb.position * 3 + 1],
					skinnedMesh.positions()[vb.position * 3 + 2],
					skinnedMesh.normals()[vb.normal * 3],
					skinnedMesh.normals()[vb.normal * 3 + 1],
					skinnedMesh.normals()[vb.normal * 3 + 2],
					startPos,
					startPos + skinnedMesh.affectingJointCounts()[vb.position]
			);
		});

		this.vcount = elements.size();

		this.elementsBO = new StaticSSBO<> (elements, 2, ElemInfo::store);
		this.uvsBO = new StaticSSBO<> (uvList, 1, (v, b) -> b.put(v));
		this.vObjBO = new StaticSSBO<> (Lists.newArrayList(vertexObjs), 8, VertexObj::store);
		this.jointBO = new StaticSSBO<> (jointList, 1, (v, b) -> b.put(Float.intBitsToFloat(v)));
		this.weightBO = new StaticSSBO<> (weightList, 1, (v, b) -> b.put(v));

		this.outPos = new OutputSSBO((short) 3, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outNormal = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outColor = new OutputSSBO((short) 4, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outUv0 = new OutputSSBO((short) 2, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outUv1 = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outUv2 = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}

	/*
				this.outPos.glSSBO, this.outNormal.glSSBO, this.outColor.glSSBO,
				this.outUv0.glSSBO, this.outUv1.glSSBO, this.outUv2.glSSBO);
	 */
	@Override
	public void bindBufferFormat(VertexFormat vertexFormat, int... buffers) {
		var elems = vertexFormat.getElements();

		for (int i = 0; i < elems.size(); ++i) {
			VertexFormatElement elem = elems.get(i);

			if (elem == DefaultVertexFormat.ELEMENT_POSITION) {
				ComputeShaderSetup.bindAttrPointer(buffers[0], 3, i, GL11C.GL_FLOAT);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV) {
				ComputeShaderSetup.bindAttrPointer(buffers[3], 2, i, GL11C.GL_FLOAT);
			} else if (elem == DefaultVertexFormat.ELEMENT_COLOR) {
				GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, buffers[2]);
				GL20C.glVertexAttribPointer(i, 4, GL11C.GL_FLOAT, true, 0, 0);
				GL20C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_NORMAL) {
				GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, buffers[1]);
				GL20C.glVertexAttribPointer(i, 3, GL11C.GL_BYTE, true, 4, 0);
				GL20C.glEnableVertexAttribArray(i);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV1) {
				ComputeShaderSetup.bindIntAttrPointer(buffers[4], 2, i, GL11C.GL_UNSIGNED_SHORT, 0);
			} else if (elem == DefaultVertexFormat.ELEMENT_UV2) {
				ComputeShaderSetup.bindIntAttrPointer(buffers[5], 2, i, GL11C.GL_UNSIGNED_SHORT, 0);
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

		ComputeShaderSetup.POSE_BO.bindBufferBase(0);

		this.elementsBO.bindBufferBase(1);
		this.vObjBO.bindBufferBase(2);
		this.jointBO.bindBufferBase(3);
		this.weightBO.bindBufferBase(4);
		this.uvsBO.bindBufferBase(5);

		this.outPos.bindBufferBase(6);
		this.outNormal.bindBufferBase(7);
		this.outColor.bindBufferBase(8);
		this.outUv0.bindBufferBase(9);
		this.outUv1.bindBufferBase(10);
		this.outUv2.bindBufferBase(11);

		this.hiddenFlagsBO.bindBufferBase(14);

		int workGroupCount = (this.vcount + WORK_GROUP_SIZE - 1) / WORK_GROUP_SIZE;
		shader.dispatch(workGroupCount, 1, 1);
		shader.waitBarriers();

		ComputeShaderSetup.POSE_BO.unbind();
		this.elementsBO.unbind();
		this.vObjBO.unbind();
		this.jointBO.unbind();
		this.weightBO.unbind();
		this.uvsBO.unbind();

		this.outPos.unbind();
		this.outNormal.unbind();
		this.outColor.unbind();
		this.outUv0.unbind();
		this.outUv1.unbind();
		this.outUv2.unbind();

		this.hiddenFlagsBO.unbind();
	}
	
	@Override
	public void drawWithShader(SkinnedMesh skinnedMesh, PoseStack poseStack, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
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

		this.bindBufferFormat(format,
				this.outPos.glSSBO, this.outNormal.glSSBO, this.outColor.glSSBO, this.outUv0.glSSBO,
				this.outUv1.glSSBO, this.outUv2.glSSBO);

		ComputeShaderSetup.setShaderDefaultUniforms(shader, mode, poseStack.last().pose(), RenderSystem.getProjectionMatrix(), Minecraft.getInstance().getWindow());
		shader.apply();

		this.applyComputeShader(poseStack, null, r, g, b, a, overlay, packedLight, poses.length);

		// draw call
		GL46.glUseProgram(RenderSystem.getShader().getId());
		GL46.glDrawArrays(VertexFormat.Mode.TRIANGLES.asGLMode, 0, vcount);

		// state restore
		RenderSystem.getShader().clear();
		renderType.clearRenderState();
		format.clearBufferState();

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}
	
	@Override
	public void destroyBuffers() {
		this.vObjBO.close();
		this.uvsBO.close();
		this.weightBO.close();
		this.jointBO.close();
		
		this.outNormal.close();
		this.outPos.close();
		
		this.outColor.close();
		this.outUv1.close();
		this.outUv2.close();
		
		RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
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
