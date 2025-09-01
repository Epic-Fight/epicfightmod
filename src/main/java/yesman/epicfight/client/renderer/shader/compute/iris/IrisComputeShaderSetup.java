package yesman.epicfight.client.renderer.shader.compute.iris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL46;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
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
import yesman.epicfight.client.renderer.shader.compute.ComputeShaderSetup;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.DynamicSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.IArrayBufferProxy;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.OutputSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.StaticSSBO;
import yesman.epicfight.client.renderer.shader.compute.backend.program.ComputeProgram;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

@OnlyIn(Dist.CLIENT)
public class IrisComputeShaderSetup implements ComputeShaderSetup {
	private final StaticSSBO<Float> uvsBO;
	private final StaticSSBO<VertexObj> vObjBO;
	private final StaticSSBO<Integer> jointBO;
	private	final StaticSSBO<Float> weightBO;
	private final StaticSSBO<ElemInfo> elementsBO;
	private final StaticSSBO<Float> midUVBO;
	
	private final OutputSSBO outPos;
	private final OutputSSBO outNormal;
	private final OutputSSBO outColor;
	private final OutputSSBO outUv0;
	private final OutputSSBO outUv1;
	private final OutputSSBO outUv2;
	private final OutputSSBO outEntityId;
	private final OutputSSBO outTangent;
	
	private final IArrayBufferProxy hiddenFlagsBO;
	private final Integer[] hiddenFlags;
	
	private final int arrayObjectId;
	private final int vcount;
	
	public IrisComputeShaderSetup(SkinnedMesh skinnedMesh) {
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
		
		List<Float> midUVList = Lists.newArrayList();
		float[] midUVs = new float[(elements.size() / 3) * 2];
		this.vcount = elements.size();
		
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
		
		this.elementsBO = new StaticSSBO<> (elements, 2, ElemInfo::store);
		this.uvsBO = new StaticSSBO<> (uvList, 1, (v, b) -> b.put(v));
		this.midUVBO = new StaticSSBO<> (midUVList, 1, (v, b) -> b.put(v));
		this.vObjBO = new StaticSSBO<> (Lists.newArrayList(vertexObjs), 8, VertexObj::store);
		this.jointBO = new StaticSSBO<> (jointList, 1, (v, b) -> b.put(Float.intBitsToFloat(v)));
		this.weightBO = new StaticSSBO<> (weightList, 1, (v, b) -> b.put(v));
		
		this.outPos = new OutputSSBO((short) 3, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outNormal = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outColor = new OutputSSBO((short) 4, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outUv0 = new OutputSSBO((short) 2, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outUv1 = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outUv2 = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);
		
		this.outEntityId = new OutputSSBO((short) 2, elements.size(), DynamicSSBO.DataMode.STREAM);
		this.outTangent = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}
	
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
			// iris part
			else if (elem == IrisVertexFormats.ENTITY_ID_ELEMENT) {
				ComputeShaderSetup.bindIntAttrPointer(buffers[6], 3, i, GL11C.GL_UNSIGNED_SHORT, 4);
			} else if (elem == IrisVertexFormats.MID_TEXTURE_ELEMENT) {
				ComputeShaderSetup.bindAttrPointer(buffers[7], 2, i, GL11C.GL_FLOAT);
			} else if (elem == IrisVertexFormats.TANGENT_ELEMENT) {
				GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, buffers[8]);
				GL20C.glVertexAttribPointer(i, 4, GL11C.GL_BYTE, false, 0, 0);
				GL20C.glEnableVertexAttribArray(i);
			}
		}
		
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
	}
	
	@Override
	public void applyComputeShader(PoseStack poseStack, OpenMatrix4f partTransform, float r, float g, float b, float a, int overlay, int light, int jointCount) {
		// shader setup
		ComputeProgram shader = ComputeShaderProvider.meshComputeIris;
		shader.useProgram();
		shader.getUniform("colorIn").uploadVec4(r,g,b,a);
		shader.getUniform("uv1In").uploadUnsignedInt(overlay);
		shader.getUniform("uv2In").uploadUnsignedInt(light);
		shader.getUniform("part_offset").uploadUnsignedInt(jointCount);
		shader.getUniform("entity_id_0").uploadUnsignedInt(((this.getEntity() << 16) & 0xFFFF0000) | (this.getBlock() & 0xFFFF));
		shader.getUniform("entity_id_1").uploadUnsignedInt(this.getItem() << 16);

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
		this.outEntityId.bindBufferBase(12);
		this.outTangent.bindBufferBase(13);
		
		this.hiddenFlagsBO.bindBufferBase(14);
		
		int workGroupCount = ((this.vcount / 3) + WORK_GROUP_SIZE - 1) / WORK_GROUP_SIZE;
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
		this.outEntityId.unbind();
		this.outTangent.unbind();
		
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
				this.outPos.glSSBO, this.outNormal.glSSBO, this.outColor.glSSBO,
				this.outUv0.glSSBO, this.outUv1.glSSBO, this.outUv2.glSSBO,
				this.outEntityId.glSSBO, this.midUVBO.glSSBO, this.outTangent.glSSBO);
		
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
	public int vaoId() {
		return this.arrayObjectId;
	}
	
	@Override
	public int vertexCount() {
		return this.vcount;
	}
	
	@Override
	public void destroyBuffers() {
		this.uvsBO.close();
		this.vObjBO.close();
		this.jointBO.close();
		this.weightBO.close();
		this.elementsBO.close();
		this.midUVBO.close();
		
		this.outPos.close();
		this.outNormal.close();
		this.outColor.close();
		this.outUv0.close();
		this.outUv1.close();
		this.outUv2.close();
		this.outEntityId.close();
		this.outTangent.close();
		
		this.hiddenFlagsBO.close();
		
		RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
	}
	
    public short getBlock() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
    }
    
    public short getEntity() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
    }
    
    public short getItem() {
        return (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem();
    }
}
