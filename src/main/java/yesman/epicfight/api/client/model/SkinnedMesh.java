package yesman.epicfight.api.client.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.alexthe666.citadel.repack.jcodec.common.ArrayUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.model.SkinnedMesh.SkinnedMeshPart;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.GLConstants;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec4f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.EpicFightVertexFormatElement;
import yesman.epicfight.client.renderer.shader.AnimationShaderInstance;
import yesman.epicfight.client.renderer.shader.ShaderParser;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object.OutputSSBO;
import yesman.epicfight.client.renderer.shader.compute_boost.backend.gl_object.SSBO;
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
	private VertexBuffer<Float> positionsBuffer;
	private VertexBuffer<Float> uvsBuffer;
	private VertexBuffer<Byte> normalsBuffer;
	private VertexBuffer<Short> jointsBuffer;
	private VertexBuffer<Float> weightsBuffer;

	protected /*static*/ final OpenMatrix4f[] FINAL_POSES;
	protected /*static*/ final OpenMatrix4f[] NORMAL_POSES;


	/*
	 * Compute Boost
	 */
	private SSBO<OpenMatrix4f> pose_ssbo;

	private record VertexObj(float[] pos, float[] nor, int[] joint){
		public void store2(FloatBuffer floatBuffer){
			floatBuffer.put(pos);
			floatBuffer.put(nor);
			floatBuffer.put( Float.intBitsToFloat(joint[0]));
			floatBuffer.put( Float.intBitsToFloat(joint[1]));
		}
		public void store(float[] buf){
			buf[0] = pos[0];
			buf[1] = pos[1];
			buf[2] = pos[2];
			buf[3] = nor[0];
			buf[4] = nor[1];
			buf[5] = nor[2];
			buf[6] = Float.intBitsToFloat(joint[0]);
			buf[7] = Float.intBitsToFloat(joint[1]);
		}
	}

	private VertexObj[] vertexObjs;
	private Integer[] jointPool;
	private Float[] weightPool;

	private SSBO<VertexObj> vertex_ssbo;
	private SSBO<Integer> joint_ssbo;
	private SSBO<Float> weight_ssbo;

	private OutputSSBO out_vertex;
	private OutputSSBO out_normal;

	private void init_boost(){
		var vertxCounts = vertexBuilderMap.keySet().size();
		vertexObjs = new VertexObj[vertxCounts];

		List<Integer> joint_p = new ArrayList<>();
		List<Float> weight_p = new ArrayList<>();

		vertexBuilderMap.forEach((k, v) -> {
			int index = k.position * 3;
			float[] pos = {this.positions[index], this.positions[index + 1], this.positions[index + 2]};
			index = k.normal * 3;
			float[] nor = {this.normals[index], this.normals[index + 1], this.normals[index + 2]};

			int j_start=joint_p.size();
			for (int i = 0; i < this.affectingJointCounts[k.position]; i++) {
				int jointIndex = this.affectingJointIndices[k.position][i];
				int weightIndex = this.affectingWeightIndices[k.position][i];
				float weight = this.weights[weightIndex];
				joint_p.add(jointIndex);
				weight_p.add(weight);
			}
			int[] jt = {j_start, joint_p.size()-j_start};

			var vo = new VertexObj(pos, nor, jt);
			vertexObjs[v] = vo;
		});

		jointPool = joint_p.toArray(new Integer[0]);
		weightPool = weight_p.toArray(new Float[0]);

		pose_ssbo = new SSBO<>(FINAL_POSES,
				(short) (16 * 4), SSBO.DataMode.DYNAMIC,
				(pose, buf) ->
				{
					buf[0] = pose.m00; buf[1] = pose.m01; buf[2] = pose.m02; buf[3] = pose.m03;
					buf[4] = pose.m10; buf[5] = pose.m11; buf[6] = pose.m12; buf[7] = pose.m13;
					buf[8] = pose.m20; buf[9] = pose.m21; buf[10] = pose.m22; buf[11] = pose.m23;
					buf[12] = pose.m30; buf[13] = pose.m31; buf[14] = pose.m32; buf[15] = pose.m33;
				}, OpenMatrix4f::store);

		vertex_ssbo = new SSBO<>(vertexObjs,
				(short) (8 * 4), SSBO.DataMode.STATIC,
				VertexObj::store, VertexObj::store2);

		joint_ssbo = new SSBO<>(jointPool,
				(short) (8 * 4), SSBO.DataMode.STATIC,
				(j, b) -> b[0] = j, (j, b) -> b.put(Float.intBitsToFloat(j)));

		weight_ssbo = new SSBO<>(weightPool,
				(short) (8 * 4), SSBO.DataMode.STATIC,
				(j, b) -> b[0] = j, (j, b) -> b.put(j));

		out_vertex = new OutputSSBO((short) (3 * 4), vertexObjs.length, SSBO.DataMode.STREAM);
		out_normal = new OutputSSBO((short) (3 * 4), vertexObjs.length, SSBO.DataMode.STREAM);
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

		FINAL_POSES = OpenMatrix4f.allocateMatrixArray(maxJointId);
		NORMAL_POSES = OpenMatrix4f.allocateMatrixArray(maxJointId);
		
		if (RenderSystem.isOnRenderThread()) {
			this.initBuffers();
		} else {
			RenderSystem.recordRenderCall(this::initBuffers);
		}
	}


	protected Map<VertexBuilder, Integer> vertexBuilderMap = Maps.newHashMap();
	public void initBuffers() {
		this.positionsBuffer = new VertexBuffer<>
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
		
		this.bufferInitialized = true;
		init_boost();
	}
	
	public void pointPositionsBuffer(int attrIndex) {
		this.positionsBuffer.vertexAttribPointer(attrIndex);
	}
	
	public void uvPositionsBuffer(int attrIndex) {
		this.uvsBuffer.vertexAttribPointer(attrIndex);
	}
	
	public void normalPositionsBuffer(int attrIndex) {
		this.normalsBuffer.vertexAttribPointer(attrIndex);
	}
	
	public void jointPositionsBuffer(int attrIndex) {
		this.jointsBuffer.vertexAttribPointer(attrIndex);
	}
	
	public void weightPositionsBuffer(int attrIndex) {
		this.weightsBuffer.vertexAttribPointer(attrIndex);
	}
	
	public void destroy() {
		if (!this.bufferInitialized) {
			return;
		}

		this.positionsBuffer.destroy();
		this.uvsBuffer.destroy();
		this.normalsBuffer.destroy();
		this.jointsBuffer.destroy();
		this.weightsBuffer.destroy();
        this.parts.values().forEach(part -> RenderSystem.glDeleteBuffers(part.indexBufferId));
        
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
		if (ClientConfig.activateAnimationShader && poses.length <= ShaderParser.SHADER_ARRAY_LIMIT) {
			renderType.setupRenderState();
			AnimationShaderInstance animationShader = EpicFightRenderTypes.getAnimationShader(renderType);
			this.drawWithShader(poseStack, animationShader, packedLight, r, g, b, a, overlay, armature, poses);
			renderType.clearRenderState();
		} else {
			this.drawPosed(poseStack, bufferSources.getBuffer(EpicFightRenderTypes.getTriangulated(renderType)), drawingFunction, packedLight, r, g, b, a, overlay, armature, poses);
		}
	}
	
	/**
	 * Draw the model with shader optimization by shader and vertex format
	 */
	public void drawWithShader(PoseStack poseStack, ShaderInstance shader, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		AnimationShaderInstance animationShader = EpicFightRenderTypes.getAnimationShader(shader);
		this.drawWithShader(poseStack, animationShader, packedLight, r, g, b, a, OverlayTexture.NO_OVERLAY, armature, poses);
	}
	
	public void drawWithShader(PoseStack poseStack, AnimationShaderInstance animationShaderInstance, int packedLight, float r, float g, float b, float a, int overlay, @Nullable Armature armature, OpenMatrix4f[] poses) {
		if (this.arrayObjectId < 0) {
			return;
		}
		
		if (animationShaderInstance == null) {
			return;
		}
		
		for (int i = 0; i < 12; ++i) {
			int j = RenderSystem.getShaderTexture(i);
			animationShaderInstance._setSampler("Sampler" + i, j);
		}
		
		if (animationShaderInstance.getModelViewMatrixUniform() != null) {
			animationShaderInstance.getModelViewMatrixUniform().set(poseStack.last().pose());
		}
		
		if (animationShaderInstance.getProjectionMatrixUniform() != null) {
			animationShaderInstance.getProjectionMatrixUniform().set(RenderSystem.getProjectionMatrix());
		}
		
		if (animationShaderInstance.getNormalMatrixUniform() != null) {
			animationShaderInstance.getNormalMatrixUniform().set(poseStack.last().normal());
		}
		
		if (animationShaderInstance.getInverseViewRotationMatrixUniform() != null) {
			animationShaderInstance.getInverseViewRotationMatrixUniform().set(RenderSystem.getInverseViewRotationMatrix());
		}
		
		if (animationShaderInstance.getColorModulatorUniform() != null) {
			animationShaderInstance.getColorModulatorUniform().set(RenderSystem.getShaderColor());
		}
		
		if (animationShaderInstance.getGlintAlphaUniform() != null) {
			animationShaderInstance.getGlintAlphaUniform().set(RenderSystem.getShaderGlintAlpha());
		}
		
		if (animationShaderInstance.getFogStartUniform() != null) {
			animationShaderInstance.getFogStartUniform().set(RenderSystem.getShaderFogStart());
		}
		
		if (animationShaderInstance.getFogEndUniform() != null) {
			animationShaderInstance.getFogEndUniform().set(RenderSystem.getShaderFogEnd());
		}
		
		if (animationShaderInstance.getFogColorUniform() != null) {
			animationShaderInstance.getFogColorUniform().set(RenderSystem.getShaderFogColor());
		}
		
		if (animationShaderInstance.getFogShapeUniform() != null) {
			animationShaderInstance.getFogShapeUniform().set(RenderSystem.getShaderFogShape().getIndex());
		}
		
		if (animationShaderInstance.getTextureMatrixUniform() != null) {
			animationShaderInstance.getTextureMatrixUniform().set(RenderSystem.getTextureMatrix());
		}
		
		if (animationShaderInstance.getGameTimeUniform() != null) {
			animationShaderInstance.getGameTimeUniform().set(RenderSystem.getShaderGameTime());
		}
		
		if (animationShaderInstance.getScreenSizeUniform() != null) {
			Window window = Minecraft.getInstance().getWindow();
			animationShaderInstance.getScreenSizeUniform().set((float) window.getWidth(), (float) window.getHeight());
		}
		
		if (animationShaderInstance.getColorUniform() != null) {
			animationShaderInstance.getColorUniform().set(r, g, b, a);
		}
		
		if (animationShaderInstance.getOverlayUniform() != null) {
			animationShaderInstance.getOverlayUniform().set(overlay & '\uffff', overlay >> 16 & '\uffff');
		}
		
		if (animationShaderInstance.getLightUniform() != null) {
			animationShaderInstance.getLightUniform().set(packedLight & '\uffff', packedLight >> 16 & '\uffff');
		}
		
		animationShaderInstance.setupShaderLights();
		
		int currentBoundVao = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BINDING);
		int currentBoundVbo = GlStateManager._getInteger(GLConstants.GL_VERTEX_ARRAY_BUFFER_BINDING);
		
		GlStateManager._glBindVertexArray(this.arrayObjectId);
		EpicFightVertexFormatElement.bindDrawing(this);
		
		for (SkinnedMeshPart part : this.parts.values()) {
			part.drawWithShader(animationShaderInstance, r, g, b, a, armature, poses);
		}
		
		EpicFightVertexFormatElement.unbindDrawing();
		
		GlStateManager._glBindVertexArray(currentBoundVao);
		GlStateManager._glBindBuffer(GLConstants.GL_ARRAY_BUFFER, currentBoundVbo);
	}
	
	public int getMaxJointCount() {
		return this.maxJointCount;
	}
	
	@OnlyIn(Dist.CLIENT)
	public class SkinnedMeshPart extends MeshPart {
		private int indexBufferId;
		
		public SkinnedMeshPart(List<VertexBuilder> animatedMeshPartList, @Nullable Mesh.RenderProperties renderProperties, @Nullable Supplier<OpenMatrix4f> vanillaPartTracer) {
			super(animatedMeshPartList, renderProperties, vanillaPartTracer);
		}
		
		private void createVbo(
			  Map<VertexBuilder, Integer> vertexBuilderMap
			, float[] positions
			, float[] uvs
			, float[] normals
			, float[] weights
			, int[] affectingJointCounts
			, int[][] affectingJointIndices
			, int[][] affectingWeightsIndices
			, List<Float> position
			, List<Float> uv
			, List<Byte> normal
			, List<Short> joint
			, List<Float> weight
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
					position.add(positions[vb.position * 3]);
					position.add(positions[vb.position * 3 + 1]);
					position.add(positions[vb.position * 3 + 2]);
					uv.add(uvs[vb.uv * 2]);
					uv.add(uvs[vb.uv * 2 + 1]);
					normal.add(packNormal(normals[vb.normal * 3]));
					normal.add(packNormal(normals[vb.normal * 3 + 1]));
					normal.add(packNormal(normals[vb.normal * 3 + 2]));
					joint.add(affectingJointCounts[vb.position] > 0 ? (short)affectingJointIndices[vb.position][0] : -1);
					joint.add(affectingJointCounts[vb.position] > 1 ? (short)affectingJointIndices[vb.position][1] : -1);
					joint.add(affectingJointCounts[vb.position] > 2 ? (short)affectingJointIndices[vb.position][2] : -1);
					weight.add(affectingJointCounts[vb.position] > 0 ? weights[affectingWeightsIndices[vb.position][0]] : 0.0F);
					weight.add(affectingJointCounts[vb.position] > 1 ? weights[affectingWeightsIndices[vb.position][1]] : 0.0F);
					weight.add(affectingJointCounts[vb.position] > 2 ? weights[affectingWeightsIndices[vb.position][2]] : 0.0F);
				}
			}
			
			indicesBuffer.flip();
			
			this.indexBufferId = GlStateManager._glGenBuffers();
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
			GlStateManager._glBufferData(GLConstants.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GLConstants.GL_STATIC_DRAW);
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, 0);
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
		
		public void drawWithShader(AnimationShaderInstance animationShaderInstance, float r, float g, float b, float a, @Nullable Armature armature, OpenMatrix4f[] poses) {
			if (this.isHidden()) {
				return;
			}
			
			OpenMatrix4f transform = this.getVanillaPartTransform();
			
			for (int i = 0; i < poses.length; i++) {
				FINAL_POSES[i].load(poses[i]);
				
				if (armature != null) {
					FINAL_POSES[i].mulBack(armature.searchJointById(i).getToOrigin());
				}
				
				if (transform != null) {
					FINAL_POSES[i].mulBack(transform);
				}
			}
			
			for (int i = 0; i < poses.length; i++) {
				if (animationShaderInstance.getPoses(i) != null) {
					animationShaderInstance.getPoses(i).set(OpenMatrix4f.exportToMojangMatrix(FINAL_POSES[i]));
				}
			}
			
			boolean hasCustomColor = this.renderProperties != null && this.renderProperties.customColor() != null;
			
			if (hasCustomColor) {
				animationShaderInstance.getColorUniform().set(this.renderProperties.customColor().x, this.renderProperties.customColor().y, this.renderProperties.customColor().z, 1.0F);
			}
			
			animationShaderInstance._getVertexFormat().setupBufferState();
			animationShaderInstance._apply();
			
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
			RenderSystem.drawElements(VertexFormat.Mode.TRIANGLES.asGLMode, this.getVertices().size(), VertexFormat.IndexType.INT.asGLType);
			GlStateManager._glBindBuffer(GLConstants.GL_ELEMENT_ARRAY_BUFFER, 0);
			
			animationShaderInstance._clear();
			animationShaderInstance._getVertexFormat().clearBufferState();
			
			if (hasCustomColor) {
				animationShaderInstance.getColorUniform().set(r, g, b, a);
			}
		}
		
		static byte packNormal(float f) {
			return (byte)((int)(Mth.clamp(f, -1.0F, 1.0F) * 127.0F) & 255);
		}
	}
	
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
			RenderSystem.glDeleteBuffers(this.vertexBufferIds);
			this.vertexBufferIds = -1;
		}
	}
	
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