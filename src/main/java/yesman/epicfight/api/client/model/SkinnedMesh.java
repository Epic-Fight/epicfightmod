package yesman.epicfight.api.client.model;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.primitives.UnsignedInteger;
import com.mojang.blaze3d.vertex.*;
import org.joml.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL46;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.model.SkinnedMesh.SkinnedMeshPart;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.GLConstants;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.EpicFightVertexFormat;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.GLUtils;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object.OutputSSBO;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object.DynamicSSBO;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object.StaticSSBO;
import yesman.epicfight.client.renderer.shader.compute_boost.compat.IIrisCompatContext;
import yesman.epicfight.client.renderer.shader.compute_boost.loader.ShaderRegistries;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

@OnlyIn(Dist.CLIENT)
public class SkinnedMesh extends StaticMesh<SkinnedMeshPart> {
	protected final float[] weights;
	protected final int[] affectingJointCounts;
	protected final int[][] affectingWeightIndices;
	protected final int[][] affectingJointIndices;
	
	private final int maxJointCount;
	private int arrayObjectId;
	
	private boolean bufferInitialized;
	//private VertexBuffer<Float> positionsBuffer;
	//private VertexBuffer<Float> uvsBuffer;
	//private VertexBuffer<Byte> normalsBuffer;
	//private VertexBuffer<Short> jointsBuffer;
	//private VertexBuffer<Float> weightsBuffer;

	protected static final OpenMatrix4f[] FINAL_POSES = OpenMatrix4f.allocateMatrixArray(EpicFightSharedConstants.MAX_JOINTS);
	protected static final OpenMatrix4f[]
			NORMAL_POSES = OpenMatrix4f.allocateMatrixArray(EpicFightSharedConstants.MAX_JOINTS);


	protected static DynamicSSBO<OpenMatrix4f> poseBO;
	/*
	 * Compute Boost
	 */

	private record VertexObj(float px, float py, float pz,
			float nx, float ny, float nz, int jts, int jte){
		public void store(FloatBuffer floatBuffer){
			floatBuffer.put(px);
			floatBuffer.put(py);
			floatBuffer.put(pz);

			floatBuffer.put(nx);
			floatBuffer.put(ny);
			floatBuffer.put(nz);

			floatBuffer.put(Float.intBitsToFloat(jts));
			floatBuffer.put(Float.intBitsToFloat(jte));
		}
	}

	//StaticSSBO<Float> positionBO;
	//StaticSSBO<Float> normalBO;

	int vcount;
	private StaticSSBO<Float> uvsBO;
	private StaticSSBO<VertexObj> vObjBO;
	private StaticSSBO<Integer> jointBO;
	private	StaticSSBO<Float> weightBO;

	//StaticSSBO<Float> colorBO;

	private OutputSSBO out_pos;
	private OutputSSBO out_normal;
	private OutputSSBO out_color;
	private OutputSSBO out_uv1;
	private OutputSSBO out_uv2;


	// iris compat



	/*private OutputSSBO out_vert;*/

	private void init_boost_vanilla(){
		if(poseBO == null) poseBO = new DynamicSSBO<>(FINAL_POSES,
				(short) 16, DynamicSSBO.DataMode.DYNAMIC,
				OpenMatrix4f::store);
		//System.out.println("VANILLA");
		Map<VertexBuilder, Integer> vertexBuilderMap = Maps.newHashMap();

		//List<Float> positionList = Lists.newArrayList();
		List<Float> uvList = Lists.newArrayList();
		//List<Byte> normalList = Lists.newArrayList();

		this.arrayObjectId = GlStateManager._glGenVertexArrays();
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);
		GlStateManager._glBindVertexArray(this.arrayObjectId);

		for (SkinnedMeshPart part : this.parts.values()) {
			part.createSSBO_Vanilla(vertexBuilderMap, /*this.positions,*/ this.uvs, /*this.normals, this.weights,
					this.affectingJointCounts, this.affectingJointIndices, this.affectingWeightIndices,*/
					/*positionList,*/ uvList);
		}

		var vertexObjs = new VertexObj[vertexBuilderMap.size()];

		List<Integer> jointList = Lists.newArrayList();
		List<Float> weightList = Lists.newArrayList();

		vertexBuilderMap.forEach((vb, idx) -> {
			int start_pos = jointList.size();
			for (int i = 0; i < affectingJointCounts[vb.position]; i++) {
				int jointIndex = affectingJointIndices[vb.position][i];
				int weightIndex = affectingWeightIndices[vb.position][i];
				float weight = weights[weightIndex];

				jointList.add(jointIndex);
				weightList.add(weight);
			}

			vertexObjs[idx] = new VertexObj(
					positions[vb.position * 3], positions[vb.position * 3 + 1], positions[vb.position * 3 + 2],
					normals[vb.normal * 3], normals[vb.normal * 3 + 1], normals[vb.normal * 3 + 2],
					start_pos, start_pos + affectingJointCounts[vb.position]
			);
		});

		vcount = vertexObjs.length;

		uvsBO  = new StaticSSBO<>(uvList,
				1,
				(v, b) -> b.put(v)
		);

		vObjBO = new StaticSSBO<>(Lists.newArrayList(vertexObjs),
					8,
					VertexObj::store
				);

		jointBO = new StaticSSBO<>(jointList,
				1,
				(v, b) -> b.put(Float.intBitsToFloat(v))
		);

		weightBO = new StaticSSBO<>(weightList,
				1,
				(v, b) -> b.put(v)
		);

	/*	poseBO = new DynamicSSBO<>(FINAL_POSES,
				(short) 16, DynamicSSBO.DataMode.DYNAMIC,
                OpenMatrix4f::store);*/

		out_pos = new OutputSSBO((short) 3, vertexObjs.length, DynamicSSBO.DataMode.STREAM);
		out_normal = new OutputSSBO((short) 1, vertexObjs.length, DynamicSSBO.DataMode.STREAM);

		out_color = new OutputSSBO((short) 4, vertexObjs.length, DynamicSSBO.DataMode.STREAM);
		out_uv1 = new OutputSSBO((short) 1, vertexObjs.length, DynamicSSBO.DataMode.STREAM);
		out_uv2 = new OutputSSBO((short) 1, vertexObjs.length, DynamicSSBO.DataMode.STREAM);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}

	private StaticSSBO<ElemInfo> elementsBO;
	private StaticSSBO<Float> midUVBO;
	private DynamicSSBO<Integer> hiddenFlagsBO;
	private Integer[] HIDDEN_FLAGS;

	private OutputSSBO out_uv0;
	private OutputSSBO out_entity_id;
	private OutputSSBO out_tangent;

	public record ElemInfo(int pool_id, int part_id){
		public void store(FloatBuffer buffer){
			buffer.put(Float.intBitsToFloat(pool_id));
			buffer.put(Float.intBitsToFloat(part_id));
		}
	}

	private void init_boost_iris(){
		//System.out.println("IRIS");
		if(poseBO == null) poseBO = new DynamicSSBO<>(FINAL_POSES,
				(short) 16, DynamicSSBO.DataMode.DYNAMIC,
				OpenMatrix4f::store);

		Map<VertexBuilder, Integer> vertexBuilderMap = Maps.newHashMap();
		List<ElemInfo> elements = Lists.newArrayList();

		this.arrayObjectId = GlStateManager._glGenVertexArrays();
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);
		GlStateManager._glBindVertexArray(this.arrayObjectId);

		List<Float> uvList = Lists.newArrayList();


		HIDDEN_FLAGS = new Integer[(this.parts.values().size()+31) / 32];
		hiddenFlagsBO = new DynamicSSBO<>(HIDDEN_FLAGS, (short) 1, DynamicSSBO.DataMode.DYNAMIC,
				(v, b) -> b.put(Float.intBitsToFloat(v))
		);

		int part_idx = 0;
		for (SkinnedMeshPart part : this.parts.values()) {
			part.createSSBO_Iris(vertexBuilderMap, uvs, uvList, elements, part_idx++);
		}

		//PrintNumberList(elements);

		var vertexObjs = new VertexObj[vertexBuilderMap.size()];

		List<Integer> jointList = Lists.newArrayList();
		List<Float> weightList = Lists.newArrayList();

		vertexBuilderMap.forEach((vb, idx) -> {
			int start_pos = jointList.size();
			for (int i = 0; i < affectingJointCounts[vb.position]; i++) {
				int jointIndex = affectingJointIndices[vb.position][i];
				int weightIndex = affectingWeightIndices[vb.position][i];
				float weight = weights[weightIndex];

				jointList.add(jointIndex);
				weightList.add(weight);
			}

			vertexObjs[idx] = new VertexObj(
					positions[vb.position * 3], positions[vb.position * 3 + 1], positions[vb.position * 3 + 2],
					normals[vb.normal * 3], normals[vb.normal * 3 + 1], normals[vb.normal * 3 + 2],
					start_pos, start_pos + affectingJointCounts[vb.position]
			);
		});

		List<Float> midUVList = Lists.newArrayList();
		float[] midUVs = new float[(elements.size()/3)*2];

		vcount = elements.size();

		//if(elements.size() % 3 != 0) System.err.println("Face count not divided by 3.");

		for (int i = 0; i < elements.size(); i++) {
			int vert_pool_idx = elements.get(i).pool_id;

			float u = uvList.get(vert_pool_idx*2);
			float v = uvList.get(vert_pool_idx*2+1);

			int face_idx = i / 3;
			if(i % 3 == 0){
				midUVs[face_idx * 2] = u / 3;
				midUVs[face_idx * 2 + 1] = v / 3;
			}
			else {
				midUVs[face_idx * 2] += u / 3;
				midUVs[face_idx * 2 + 1] += v / 3;
			}
		}

		for (int i = 0; i < elements.size(); i++) {
			int face_idx = i / 3;
			midUVList.add(midUVs[face_idx*2]);
			midUVList.add(midUVs[face_idx*2+1]);
		}

		elementsBO = new StaticSSBO<>(elements,
				2,
				ElemInfo::store
		);

		uvsBO  = new StaticSSBO<>(uvList,
				1,
				(v, b) -> b.put(v)
		);

		midUVBO = new StaticSSBO<>(midUVList,
				1,
				(v, b) -> b.put(v)
		);

		vObjBO = new StaticSSBO<>(Lists.newArrayList(vertexObjs),
				8,
				VertexObj::store
		);

		jointBO = new StaticSSBO<>(jointList,
				1,
				(v, b) -> b.put(Float.intBitsToFloat(v))
		);

		weightBO = new StaticSSBO<>(weightList,
				1,
				(v, b) -> b.put(v)
		);

		/*poseBO = new DynamicSSBO<>(FINAL_POSES,
				(short) 16, DynamicSSBO.DataMode.DYNAMIC,
				OpenMatrix4f::store);*/

		out_pos = new OutputSSBO((short) 3, elements.size(), DynamicSSBO.DataMode.STREAM);
		out_normal = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);

		out_color = new OutputSSBO((short) 4, elements.size(), DynamicSSBO.DataMode.STREAM);
		out_uv0 = new OutputSSBO((short) 2, elements.size(), DynamicSSBO.DataMode.STREAM);
		out_uv1 = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);
		out_uv2 = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);

		out_entity_id = new OutputSSBO((short) 2, elements.size(), DynamicSSBO.DataMode.STREAM);
		out_tangent = new OutputSSBO((short) 1, elements.size(), DynamicSSBO.DataMode.STREAM);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}

	//protected Map<VertexBuilder, Integer> vertexBuilderMap = Maps.newHashMap();
	public void initBuffers() {
		/*this.positionsBuffer = new VertexBuffer<>
				(GLConstants.GL_FLOAT, 3, false, ByteBuffer::putFloat);
		this.uvsBuffer = new VertexBuffer<>
				(GLConstants.GL_FLOAT, 2, false, ByteBuffer::putFloat);
		this.normalsBuffer = new VertexBuffer<>
				(GLConstants.GL_BYTE, 3, true, ByteBuffer::put);
		this.jointsBuffer = new VertexBuffer<>
				(GLConstants.GL_SHORT, 3, false, ByteBuffer::putShort);
		this.weightsBuffer = new VertexBuffer<>
				(GLConstants.GL_FLOAT, 3, false, ByteBuffer::putFloat);

		this.arrayObjectId = GlStateManager._glGenVertexArrays();

		List<Float> positionList = Lists.newArrayList();
		List<Float> uvList = Lists.newArrayList();
		List<Byte> normalList = Lists.newArrayList();
		List<Short> jointList = Lists.newArrayList();
		List<Float> weightList = Lists.newArrayList();

		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);

		GlStateManager._glBindVertexArray(this.arrayObjectId);

		for (SkinnedMeshPart part : this.parts.values()) {
			part.createVbo(vertexBuilderMap, this.positions, this.uvs, this.normals, this.weights,
					this.affectingJointCounts, this.affectingJointIndices, this.affectingWeightIndices,
					positionList, uvList, normalList, jointList, weightList);
		}

		this.positionsBuffer.bindVertexData(positionList);
		this.uvsBuffer.bindVertexData(uvList);
		this.normalsBuffer.bindVertexData(normalList);
		this.jointsBuffer.bindVertexData(jointList);
		this.weightsBuffer.bindVertexData(weightList);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);

		this.bufferInitialized = true;*/
		if(ShaderRegistries.IrisLoaded){
			init_boost_iris();
		}
		else {
			init_boost_vanilla();
		}
		this.bufferInitialized = true;
	}

	public SkinnedMesh(@Nullable Map<String, Number[]> arrayMap,
					   @Nullable Map<MeshPartDefinition, List<VertexBuilder>> partBuilders,
					   @Nullable SkinnedMesh parent, RenderProperties properties) {
		super(arrayMap, partBuilders, parent, properties);
		
		this.weights = parent == null ? ParseUtil.unwrapFloatWrapperArray(arrayMap.get("weights")) : parent.weights;
		this.affectingJointCounts = parent == null ? ParseUtil.unwrapIntWrapperArray(arrayMap.get("vcounts")) : parent.affectingJointCounts;
		
		if (parent != null) {
			this.affectingJointIndices = parent.affectingJointIndices;
			this.affectingWeightIndices = parent.affectingWeightIndices;
		} else {
			int[] vindices = ParseUtil.unwrapIntWrapperArray(arrayMap.get("vindices"));
			this.affectingJointIndices = new int[this.affectingJointCounts.length][];
			this.affectingWeightIndices = new int[this.affectingJointCounts.length][];
			int idx = 0;
			
			for (int i = 0; i < this.affectingJointCounts.length; i++) {
				int count = this.affectingJointCounts[i];
				int[] jointId = new int[count];
				int[] weights = new int[count];
				
				for (int j = 0; j < count; j++) {
					jointId[j] = vindices[idx * 2];
					weights[j] = vindices[idx * 2 + 1];
					idx++;
				}
				
				this.affectingJointIndices[i] = jointId;
				this.affectingWeightIndices[i] = weights;
			}
		}
		
		int maxJointId = 0;
		
		for (int[] i : this.affectingJointIndices) {
			for (int j : i) {
				if (maxJointId < j) {
					maxJointId = j;
				}
			}
		}
		
		this.maxJointCount = maxJointId;

		//FINAL_POSES = OpenMatrix4f.allocateMatrixArray(maxJointCount + 2);
		
		if (RenderSystem.isOnRenderThread()) {
			this.initBuffers();
		} else {
			RenderSystem.recordRenderCall(this::initBuffers);
		}
	}
	
	public void destroy() {
		if (!this.bufferInitialized) {
			return;
		}

		vObjBO.close();
		uvsBO.close();
		weightBO.close();
		jointBO.close();

		out_normal.close();
		out_pos.close();

		out_color.close();
		out_uv1.close();
		out_uv2.close();

		if(out_tangent != null){
			out_tangent.close();
			out_uv0.close();
			out_entity_id.close();
			midUVBO.close();
		}

		parts.values().forEach(part -> RenderSystem.glDeleteBuffers(part.indexBufferId));
        
        RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
        this.arrayObjectId = -1;
	}
	
	@Override
	protected Map<String, SkinnedMeshPart> createModelPart(Map<MeshPartDefinition, List<VertexBuilder>> partBuilders) {
		Map<String, SkinnedMeshPart> parts = Maps.newHashMap();
		
		partBuilders.forEach((partDefinition, vertexBuilder) -> {
			parts.put(partDefinition.partName(), new SkinnedMeshPart(vertexBuilder,
					partDefinition.renderProperties(),
					partDefinition.getModelPartAnimationProvider()));
		});
		
		return parts;
	}
	
	@Override
	protected SkinnedMeshPart getOrLogException(Map<String, SkinnedMeshPart> parts, String name) {
		if (!parts.containsKey(name)) {
			if (EpicFightSharedConstants.IS_DEV_ENV) {
				EpicFightMod.LOGGER.debug("Cannot find the mesh part named " + name + " in " + this.getClass().getCanonicalName());
			}
			
			return null;
		}
		
		return parts.get(name);
	}
	
	private static final Vec4f TRANSFORM = new Vec4f();
	private static final Vec4f POS = new Vec4f();
	private static final Vec4f TOTAL_POS = new Vec4f();
	
	@Override
	public void getVertexPosition(int positionIndex, Vector4f dest, @Nullable OpenMatrix4f[] poses) {
		int index = positionIndex * 3;
		
		POS.set(this.positions[index], this.positions[index + 1], this.positions[index + 2], 1.0F);
		TOTAL_POS.set(0.0F, 0.0F, 0.0F, 0.0F);
		
		for (int i = 0; i < this.affectingJointCounts[positionIndex]; i++) {
			int jointIndex = this.affectingJointIndices[positionIndex][i];
			int weightIndex = this.affectingWeightIndices[positionIndex][i];
			float weight = this.weights[weightIndex];
			Vec4f.add(OpenMatrix4f.transform(poses[jointIndex], POS, TRANSFORM).scale(weight), TOTAL_POS, TOTAL_POS);
		}
		
		dest.set(TOTAL_POS.x, TOTAL_POS.y, TOTAL_POS.z, 1.0F);
	}
	
	private static final Vec4f NORM = new Vec4f();
	private static final Vec4f TOTAL_NORM = new Vec4f();
	
	@Override
	public void getVertexNormal(int positionIndex, int normalIndex, Vector3f dest, @Nullable OpenMatrix4f[] poses) {
		int index = normalIndex * 3;
		NORM.set(this.normals[index], this.normals[index + 1], this.normals[index + 2], 1.0F);
		TOTAL_NORM.set(0.0F, 0.0F, 0.0F, 0.0F);
		
		for (int i = 0; i < this.affectingJointCounts[positionIndex]; i++) {
			int jointIndex = this.affectingJointIndices[positionIndex][i];
			int weightIndex = this.affectingWeightIndices[positionIndex][i];
			float weight = this.weights[weightIndex];
			Vec4f.add(OpenMatrix4f.transform(poses[jointIndex], NORM, TRANSFORM).scale(weight), TOTAL_NORM, TOTAL_NORM);
		}
		
		dest.set(TOTAL_NORM.x, TOTAL_NORM.y, TOTAL_NORM.z);
	}
	
	/**
	 * Draws the model without applying animation
	 */
	@Override
	public void draw(PoseStack poseStack, VertexConsumer bufferbuilder, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay) {
		for (SkinnedMeshPart part : this.parts.values()) {
			part.draw(poseStack, bufferbuilder, drawingFunction, packedLight, r, g, b, a, overlay);
		}
	}

	//protected static final int last_size = 0;
	protected static final Vector4f POSITION = new Vector4f();
	protected static final Vector3f NORMAL = new Vector3f();


	/**
	 * Draws the model to vanilla buffer
	 */
	@Override
	public void drawPosed(PoseStack poseStack, VertexConsumer bufferbuilder, Mesh.DrawingFunction drawingFunction,
						  int packedLight, float r, float g, float b, float a, int overlay,
						  @Nullable Armature armature, OpenMatrix4f[] poses) {
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();

		for (SkinnedMeshPart part : this.parts.values()) {
			if (!part.isHidden()) {
				OpenMatrix4f transform = part.getVanillaPartTransform();
				
				for (int i = 0; i < poses.length; i++) {
					FINAL_POSES[i].load(poses[i]);
					
					if (armature != null) {
						FINAL_POSES[i].mulBack(armature.searchJointById(i).getToOrigin());
					}
					
					if (transform != null) {
						FINAL_POSES[i].mulBack(transform);
					}
					
					NORMAL_POSES[i] = FINAL_POSES[i].removeTranslation();
				}
				
				for (VertexBuilder vi : part.getVertices()) {
					getVertexPosition(vi.position, POSITION, FINAL_POSES);
					getVertexNormal(vi.position, vi.normal, NORMAL, NORMAL_POSES);
					
					POSITION.mul(matrix4f);
					NORMAL.mul(matrix3f);
					
					drawingFunction
							.draw(bufferbuilder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x,
									NORMAL.y, NORMAL.z, packedLight, r, g, b, a,
									this.uvs[vi.uv * 2], this.uvs[vi.uv * 2 + 1], overlay);

				}
			}
		}
	}
	
	/**
	 * Draws the model depending on animation shader option
	 * @param armature give this parameter as null if @param poses already bound origin translation
	 * @param poses
	 */
	public void draw(PoseStack poseStack, MultiBufferSource bufferSources, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		this.draw(poseStack, bufferSources, renderType, Mesh.DrawingFunction.NEW_ENTITY, packedLight, r, g, b, a, overlay, armature, poses);
	}

	@Override
	public void draw(PoseStack poseStack, MultiBufferSource bufferSources, RenderType renderType, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		if (ClientConfig.activateAnimationShader && ShaderRegistries.isComputeShaderSupport()) {
			//var ef_rt = EpicFightRenderTypes.getTriangulated(renderType);
			if(!ShaderRegistries.IrisLoaded) drawWithShader_Vanilla(poseStack, renderType, packedLight, r, g, b, a, overlay, armature, poses);
			else drawWithShader_Iris(poseStack, renderType, packedLight, r, g, b, a, overlay, armature, poses);

		} else {
			this.drawPosed(poseStack,
					bufferSources.getBuffer(EpicFightRenderTypes.getTriangulated(renderType)),
					drawingFunction, packedLight, r, g, b, a, overlay, armature, poses);
		}
	}

	/**
	 * Draw the model with shader optimization by shader and vertex format
	 */
	public void drawWithShader_Vanilla(PoseStack poseStack, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		if (this.arrayObjectId < 0 || !bufferInitialized) {
			return;
		}

		// pose setup and upload
		for (int i = 0; i < poses.length; i++) {
			FINAL_POSES[i].load(poses[i]);
			if (armature != null) {
				FINAL_POSES[i].mulBack(armature.searchJointById(i).getToOrigin());
			}
		}

		poseBO.updateFromTo(0, poses.length);

		// state trace
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);

		// setup state
		GlStateManager._glBindVertexArray(arrayObjectId);
		renderType.setupRenderState();

		var mode	= renderType.mode();
		ShaderInstance shader	= RenderSystem.getShader();
		var format =  shader.getVertexFormat();


		EpicFightVertexFormat.vertexFormatSetupImpl.invoke(format,
				out_pos.glSSBO, out_normal.glSSBO, out_color.glSSBO,
				uvsBO.glSSBO, out_uv1.glSSBO, out_uv2.glSSBO
		);  //shader.getVertexFormat().setupBufferState();


		GLUtils.SetShaderDefaultUniforms(shader,
				mode,
				poseStack.last().pose(),
				RenderSystem			.getProjectionMatrix(),
				Minecraft.getInstance()	.getWindow			()
		);
		shader.apply();

		// draw call
		for (SkinnedMeshPart part : this.parts.values()) {
			part.drawWithShader_Vanilla(poseStack, renderType, r, g, b, a, overlay, packedLight);
		}

		// state restore
		RenderSystem.getShader().clear();
		renderType.clearRenderState();
		EpicFightVertexFormat.clearBufferState(format);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}

	public void drawWithShader_Iris(PoseStack poseStack, RenderType renderType, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		if (this.arrayObjectId < 0 || !bufferInitialized) {
			return;
		}

		// pose setup and upload
		for (int i = 0; i < poses.length; i++) {
			FINAL_POSES[i].load(poses[i]);
			if (armature != null) {
				FINAL_POSES[i].mulBack(armature.searchJointById(i).getToOrigin());
			}
		}

        Arrays.fill(HIDDEN_FLAGS, 0);

		for (SkinnedMeshPart part : parts.values()) {
			var mat = part.getVanillaPartTransform();
			if(mat == null) mat = OpenMatrix4f.IDENTITY;
			FINAL_POSES[poses.length + part.part_idx].load(mat);

			if(!part.isHidden) continue;
			int flag_pos = part.getPartIdx() / 32;
			int flag_off = part.getPartIdx() % 32;
			int flag = HIDDEN_FLAGS[flag_pos];
			HIDDEN_FLAGS[flag_pos] = flag | ((part.isHidden() ? 1:0) << flag_off);
		}

		hiddenFlagsBO.updateAll();
		poseBO.updateFromTo(0, poses.length + parts.size());

		// state trace
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);

		// setup state
		GlStateManager._glBindVertexArray(arrayObjectId);
		renderType.setupRenderState();

		var mode	= renderType.mode();
		ShaderInstance shader	= RenderSystem.getShader();
		var format =  shader.getVertexFormat();

		EpicFightVertexFormat.vertexFormatSetupImpl.invoke(format,
				out_pos.glSSBO, out_normal.glSSBO, out_color.glSSBO,
				out_uv0.glSSBO, out_uv1.glSSBO, out_uv2.glSSBO,
				out_entity_id.glSSBO, midUVBO.glSSBO, out_tangent.glSSBO
		);  //shader.getVertexFormat().setupBufferState();

		GLUtils.SetShaderDefaultUniforms(shader,
				mode,
				poseStack.last().pose(),
				RenderSystem			.getProjectionMatrix(),
				Minecraft.getInstance()	.getWindow			()
		);
		shader.apply();

		applyComputeShader_Iris(poses.length, r,g,b,a,overlay, packedLight);

		// draw call

		GL46.glUseProgram(RenderSystem.getShader().getId());
		GL46.glDrawArrays(VertexFormat.Mode.TRIANGLES.asGLMode, 0, vcount);

		// state restore
		RenderSystem.getShader().clear();
		renderType.clearRenderState();
		EpicFightVertexFormat.clearBufferState(format);

		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}

	public void applyComputeShader_Vanilla(OpenMatrix4f partTransform, float r, float g, float b, float a, int overlay, int light){
		// shader setup
		var shader = ShaderRegistries.mesh_compute;
		shader.useProgram();

		shader.getUniform("colorIn").uploadVec4(r,g,b,a);
		shader.getUniform("uv1In").uploadUnsignedInt(overlay);
		shader.getUniform("uv2In").uploadUnsignedInt(light);
		shader.getUniform("partTransform")
				.uploadMatrix4f(OpenMatrix4f.exportToMojangMatrix(partTransform));

		poseBO.bindBufferBase(0); 		vObjBO.bindBufferBase(1);
		jointBO.bindBufferBase(2); 		weightBO.bindBufferBase(3);

		out_pos.bindBufferBase(4);		out_normal.bindBufferBase(5);
		out_color.bindBufferBase(6);	out_uv1.bindBufferBase(7);

		out_uv2.bindBufferBase(8);

		int workGroupSize = 128;
		int workGroupCount = (vcount + workGroupSize - 1) / workGroupSize;

		shader.dispatch(workGroupCount, 1, 1);
		shader.waitBarriers();

		poseBO.unbind();
		vObjBO.unbind();
		jointBO.unbind();
		weightBO.unbind();

		out_pos.unbind();
		out_normal.unbind();
		out_color.unbind();
		out_uv1.unbind();
		out_uv2.unbind();

		GL46.glUseProgram(0);
	}

	static final int workGroupSize = 128;
	public void applyComputeShader_Iris(int part_off, float r, float g, float b, float a, int overlay, int light){
		// shader setup
		var shader = ShaderRegistries.mesh_compute_iris;
		shader.useProgram();

		short entity = IIrisCompatContext.CTX.INSTANCE.getEntity();
		short block = IIrisCompatContext.CTX.INSTANCE.getBlock();
		short item = IIrisCompatContext.CTX.INSTANCE.getItem();

		shader.getUniform("colorIn").uploadVec4(r,g,b,a);
		shader.getUniform("uv1In").uploadUnsignedInt(overlay);
		shader.getUniform("uv2In").uploadUnsignedInt(light);

		shader.getUniform("part_offset").uploadUnsignedInt(part_off);

		shader.getUniform("entity_id_0").uploadUnsignedInt(
				((entity << 16) & 0xFFFF0000) | (block & 0xFFFF));
		shader.getUniform("entity_id_1").uploadUnsignedInt(item << 16);

		poseBO.bindBufferBase(0); 		elementsBO.bindBufferBase(1);
		vObjBO.bindBufferBase(2); 		jointBO.bindBufferBase(3);
		weightBO.bindBufferBase(4);		uvsBO.bindBufferBase(5);

		out_pos.bindBufferBase(6);		out_normal.bindBufferBase(7);
		out_color.bindBufferBase(8);	out_uv0.bindBufferBase(9);
		out_uv1.bindBufferBase(10);		out_uv2.bindBufferBase(11);
		out_entity_id.bindBufferBase(12);		out_tangent.bindBufferBase(13);

		hiddenFlagsBO.bindBufferBase(14);

		int workGroupCount = ((vcount / 3) + workGroupSize - 1) / workGroupSize;

		shader.dispatch(workGroupCount, 1, 1);
		shader.waitBarriers();

		poseBO.unbind(); 		elementsBO.unbind();
		vObjBO.unbind(); 		jointBO.unbind();
		weightBO.unbind();		uvsBO.unbind();

		out_pos.unbind();		out_normal.unbind();
		out_color.unbind();	out_uv0.unbind();
		out_uv1.unbind();		out_uv2.unbind();
		out_entity_id.unbind();		out_tangent.unbind();

		hiddenFlagsBO.unbind();
	}
	
	public int getMaxJointCount() {
		return this.maxJointCount;
	}

	@OnlyIn(Dist.CLIENT)
	public class SkinnedMeshPart extends MeshPart {
		private int indexBufferId;

		public SkinnedMeshPart(List<VertexBuilder> animatedMeshPartList,
							   @Nullable Mesh.RenderProperties renderProperties, @Nullable Supplier<OpenMatrix4f> vanillaPartTracer) {
			super(animatedMeshPartList, renderProperties, vanillaPartTracer);
		}
		
		private void createSSBO_Vanilla(
			  Map<VertexBuilder, Integer> vertexBuilderMap
			, float[] uvs
			, List<Float> uv
		) {
			ByteBuffer indicesBuffer = ByteBuffer
					.allocateDirect(this.getVertices().size() * 4)
					.order(ByteOrder.nativeOrder());
			
			for (VertexBuilder vb : this.getVertices()) {
				if (vertexBuilderMap.containsKey(vb)) {
					indicesBuffer.putInt(vertexBuilderMap.get(vb));
				} else {
					int next = vertexBuilderMap.size();
					indicesBuffer.putInt(next);
					vertexBuilderMap.put(vb, next);

					uv.add(uvs[vb.uv * 2]);
					uv.add(uvs[vb.uv * 2 + 1]);
				}
			}
			
			indicesBuffer.flip();
			
			this.indexBufferId = GlStateManager._glGenBuffers();
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
			GlStateManager._glBufferData(GLConstants.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GLConstants.GL_STATIC_DRAW);
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, 0);
		}

		private int part_idx;
		public int getPartIdx() { return part_idx; }

		private void createSSBO_Iris(
				Map<VertexBuilder, Integer> vertexBuilderMap,
				float[] uvs,
				List<Float> uvList,
				List<ElemInfo> elements,
				int part_idx
		) {
			this.part_idx = part_idx;

			for (VertexBuilder vb : this.getVertices()) {
				if (!vertexBuilderMap.containsKey(vb)) {
					int next = vertexBuilderMap.size();
					vertexBuilderMap.put(vb, next);

					uvList.add(uvs[vb.uv * 2]);
					uvList.add(uvs[vb.uv * 2 + 1]);
				}
				int vertex_pool_index = vertexBuilderMap.get(vb);
				elements.add(new ElemInfo(vertex_pool_index, part_idx));
			}
		}
		
		@Override
		public void draw(PoseStack poseStack, VertexConsumer bufferBuilder, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay) {
			if (this.isHidden()) {
				return;
			}
			
			Vector4f color = this.getColor(r, g, b, a);
			Matrix4f matrix4f = poseStack.last().pose();
			Matrix3f matrix3f = poseStack.last().normal();
			
			for (VertexBuilder vi : this.getVertices()) {
				getVertexPosition(vi.position, POSITION);
				getVertexNormal(vi.normal, NORMAL);
				POSITION.mul(matrix4f);
				NORMAL.mul(matrix3f);

				drawingFunction.draw(bufferBuilder, POSITION.x(), POSITION.y(), POSITION.z(), NORMAL.x(), NORMAL.y(), NORMAL.z(), packedLight, color.x, color.y, color.z, color.w, uvs[vi.uv * 2], uvs[vi.uv * 2 + 1], overlay);
			}
		}


		public void drawWithShader_Vanilla(PoseStack poseStack, RenderType renderType,
								   float r, float g, float b, float a, int overlay, int light) {
			if (this.isHidden()) {
				return;
			}

			OpenMatrix4f transform = this.getVanillaPartTransform();
			// computeVertex
			if(transform == null) transform = OpenMatrix4f.IDENTITY;

			applyComputeShader_Vanilla(transform, r,g,b,a,overlay,light);

			// draw call
			GL46.glUseProgram(RenderSystem.getShader().getId());
			GL46.glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
			GL46.glDrawElements(VertexFormat.Mode.TRIANGLES.asGLMode,
					this.getVertices().size(),
					VertexFormat.IndexType.INT.asGLType, 0);
		}
	}
	/*
	@OnlyIn(Dist.CLIENT)
	private class VertexBuffer<T extends Number> {
		private int vertexBufferIds;
		private final int glType;
		private final int size;
		private final boolean normalize;
		private final BiConsumer<ByteBuffer, T> bufferUploader;
		
		public VertexBuffer(int glType, int size, boolean normalize, BiConsumer<ByteBuffer, T> bufferUploader) {
			this.vertexBufferIds = GlStateManager._glGenBuffers();
			this.glType = glType;
			this.size = size;
			this.normalize = normalize;
			this.bufferUploader = bufferUploader;
		}
		
		public void bindVertexData(List<T> data) {
			if (this.vertexBufferIds < 0) {
				throw new RuntimeException("vertex buffer is already destroyed");
			}
			
			ByteBuffer buf = ByteBuffer.allocateDirect(data.size() * 4).order(ByteOrder.nativeOrder());
			
			for (T f : data) {
				this.bufferUploader.accept(buf, f);
			}
			
			buf.flip();
			
			GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, this.vertexBufferIds);
			GlStateManager._glBufferData(GLConstants.GL_ARRAY_BUFFER, buf, GLConstants.GL_STATIC_DRAW);
			GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, 0);
		}
		
		public void vertexAttribPointer(int attrIndex) {
			if (this.vertexBufferIds < 0) {
				throw new RuntimeException("vertex buffer is already destroyed");
			}
			
			GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, this.vertexBufferIds);
			
			switch (this.glType) {
			case GLConstants.GL_DOUBLE, GLConstants.GL_FLOAT -> {
				GlStateManager._vertexAttribPointer(attrIndex, this.size, this.glType, this.normalize, 0, 0);
			}
			case GLConstants.GL_BYTE, GLConstants.GL_SHORT, GLConstants.GL_INT -> {
				if (this.normalize) {
					GlStateManager._vertexAttribPointer(attrIndex, this.size, this.glType, true, 0, 0);
				} else {
					GlStateManager._vertexAttribIPointer(attrIndex, this.size, this.glType, 0, 0);
				}
			}
			}
		}
		
		public void destroy() {
			RenderglDeleteBuffers(this.vertexBufferIds);
			this.vertexBufferIds = -1;
		}
	}*/
	
	/**
	 * Export this model as Json format
	 */
	public JsonObject toJsonObject() {
		JsonObject root = new JsonObject();
		JsonObject vertices = new JsonObject();
		float[] positions = this.positions.clone();
		float[] normals = this.normals.clone();
		
		for (int i = 0; i < positions.length / 3; i++) {
			int k = i * 3;
			Vec4f posVector = new Vec4f(positions[k], positions[k+1], positions[k+2], 1.0F);
			posVector.transform(JsonAssetLoader.MINECRAFT_TO_BLENDER_COORD);
			positions[k] = posVector.x;
			positions[k+1] = posVector.y;
			positions[k+2] = posVector.z;
		}
		
		for (int i = 0; i < normals.length / 3; i++) {
			int k = i * 3;
			Vec4f normVector = new Vec4f(normals[k], normals[k+1], normals[k+2], 1.0F);
			normVector.transform(JsonAssetLoader.MINECRAFT_TO_BLENDER_COORD);
			normals[k] = normVector.x;
			normals[k+1] = normVector.y;
			normals[k+2] = normVector.z;
		}
		
		IntList affectingJointAndWeightIndices = new IntArrayList();
		
		for (int i = 0; i < this.affectingJointCounts.length; i++) {
			for (int j = 0; j < this.affectingJointCounts[j]; j++) {
				affectingJointAndWeightIndices.add(this.affectingJointIndices[i][j]);
				affectingJointAndWeightIndices.add(this.affectingWeightIndices[i][j]);
			}
		}
		
		vertices.add("positions", ParseUtil.farrayToJsonObject(positions, 3));
		vertices.add("uvs", ParseUtil.farrayToJsonObject(this.uvs, 2));
		vertices.add("normals", ParseUtil.farrayToJsonObject(normals, 3));
		vertices.add("vcounts", ParseUtil.iarrayToJsonObject(this.affectingJointCounts, 1));
		vertices.add("weights", ParseUtil.farrayToJsonObject(this.weights, 1));
		vertices.add("vindices", ParseUtil.iarrayToJsonObject(affectingJointAndWeightIndices.toIntArray(), 1));
		
		if (!this.parts.isEmpty()) {
			JsonObject parts = new JsonObject();
			
			for (Map.Entry<String, SkinnedMeshPart> partEntry : this.parts.entrySet()) {
				IntList indicesArray = new IntArrayList();
				
				for (VertexBuilder vertexIndicator : partEntry.getValue().getVertices()) {
					indicesArray.add(vertexIndicator.position);
					indicesArray.add(vertexIndicator.uv);
					indicesArray.add(vertexIndicator.normal);
				}
				
				parts.add(partEntry.getKey(), ParseUtil.iarrayToJsonObject(indicesArray.toIntArray(), 3));
			}
			
			vertices.add("parts", parts);
		} else {
			int i = 0;
			int[] indices = new int[this.vertexCount * 3];
			
			for (SkinnedMeshPart part : this.parts.values()) {
				for (VertexBuilder vertexIndicator : part.getVertices()) {
					indices[i * 3] = vertexIndicator.position;
					indices[i * 3 + 1] = vertexIndicator.uv;
					indices[i * 3 + 2] = vertexIndicator.normal;
					i++;
				}
			}
			
			vertices.add("indices", ParseUtil.iarrayToJsonObject(indices, 3));
		}
		
		root.add("vertices", vertices);
		
		if (this.renderProperties != null) {
			JsonObject renderProperties = new JsonObject();
			renderProperties.addProperty("texture_path", this.renderProperties.customTexturePath().toString());
			renderProperties.addProperty("transparent", this.renderProperties.isTransparent());
			root.add("render_properties", renderProperties);
		}
		
		return root;
	}
}
