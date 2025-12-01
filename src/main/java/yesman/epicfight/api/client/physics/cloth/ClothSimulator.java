package yesman.epicfight.api.client.physics.cloth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.joml.Math;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.client.model.CompositeMesh;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPart;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.client.physics.AbstractSimulator;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator.ClothObjectBuilder;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.SimulationObject;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.api.utils.math.joml.VectorUtils;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

/**
 * Referred to Matthias Müller's Ten minuates physics tutorial video number 14, 15
 * 
 * https://matthias-research.github.io/pages/tenMinutePhysics/index.html
 * 
 * https://www.youtube.com/@TenMinutePhysics
 **/
@OnlyIn(Dist.CLIENT)
public class ClothSimulator extends AbstractSimulator<ResourceLocation, ClothObjectBuilder, SoftBodyTranslatable, ClothSimulatable, ClothSimulator.ClothObject> {
	public static final ResourceLocation PLAYER_CLOAK = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "ingame_cloak");
	public static final ResourceLocation MODELPREVIEWER_CLOAK = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "previewer_cloak");
	private static final float SPATIAL_HASH_SPACING = 0.05F;
	
	@OnlyIn(Dist.CLIENT)
	public static class ClothObjectBuilder extends SimulationObject.SimulationObjectBuilder {
		List<Pair<Function<ClothSimulatable, Matrix4f>, ClothSimulator.ClothOBBCollider>> clothColliders = Lists.newArrayList();
		Joint joint;
		
		public ClothObjectBuilder addEntry(Function<ClothSimulatable, Matrix4f> obbTransformer, ClothOBBCollider clothOBBCollider) {
			this.clothColliders.add(Pair.of(obbTransformer, clothOBBCollider));
			return this;
		}
		
		public ClothObjectBuilder putAll(List<Pair<Function<ClothSimulatable, Matrix4f>, ClothSimulator.ClothOBBCollider>> clothOBBColliders) {
			this.clothColliders.addAll(clothOBBColliders);
			return this;
		}
		
		public ClothObjectBuilder parentJoint(Joint joint) {
			this.joint = joint;
			return this;
		}
		
		public static ClothObjectBuilder create() {
			return new ClothObjectBuilder();
		}
	}
	
	// Developer configurations
	private static boolean DRAW_MESH_COLLIDERS = false;
	private static boolean DRAW_NORMAL_OFFSET = true;
	private static boolean DRAW_OUTLINES = false;
	
	public static void drawMeshColliders(boolean flag) {
		if (!EpicFightSharedConstants.IS_DEV_ENV) {
			throw new IllegalStateException("Can't switch developer configuration in product environment.");
		}
		
		DRAW_MESH_COLLIDERS = flag;
	}
	
	public static void drawNormalOffset(boolean flag) {
		if (!EpicFightSharedConstants.IS_DEV_ENV) {
			throw new IllegalStateException("Can't switch developer configuration in product environment.");
		}
		
		DRAW_NORMAL_OFFSET = flag;
	}
	
	public static void drawOutlines(boolean flag) {
		if (!EpicFightSharedConstants.IS_DEV_ENV) {
			throw new IllegalStateException("Can't switch developer configuration in product environment.");
		}
		
		DRAW_OUTLINES = flag;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ClothObject implements SimulationObject<ClothObjectBuilder, SoftBodyTranslatable, ClothSimulatable>, Mesh {
		private final SoftBodyTranslatable provider;
		private final Map<String, ClothPart> parts;
		
		private final Map<Integer, Particle> particles;
		private final Map<Integer, ClothPart.OffsetParticle> normalOffsetParticles;
		private final List<Map<Integer, Vector3f>> particleNormals;
		
		private final Quaternionf rotationO = new Quaternionf();
		private final Vector3f centrifugalO = new Vector3f();
		
		@Nullable
		protected List<Pair<Function<ClothSimulatable, Matrix4f>, ClothSimulator.ClothOBBCollider>> clothColliders;
		protected final Joint parentJoint;
		
		//Storage vectors
		private static final Vector3f TRASNFORMED = new Vector3f();
		private static final Vector4f POSITION = new Vector4f();
		private static final Vector3f NORMAL = new Vector3f();
		
		public ClothObject(ClothObjectBuilder builder, SoftBodyTranslatable provider, Map<String, MeshPart> parts, float[] positions) {
			this.clothColliders = builder.clothColliders;
			this.parentJoint = builder.joint;
			
			this.provider = provider;
			this.particles = Maps.newHashMap();
			this.normalOffsetParticles = Maps.newHashMap();
			this.particleNormals = Lists.newArrayList();
			
			for (int i = 0; i < positions.length / 3; i++) {
				this.particleNormals.add(Maps.newHashMap());
			}
			
			for (Map.Entry<String, MeshPart> meshPart : parts.entrySet()) {
				for (VertexBuilder vb : meshPart.getValue().getVertices()) {
					Map<Integer, Vector3f> posNormals = this.particleNormals.get(vb.position);
					
					if (!posNormals.containsKey(vb.normal)) {
						provider.getOriginalMesh().getVertexNormal(vb.normal, NORMAL);
						posNormals.put(vb.normal, new Vector3f(NORMAL.x, NORMAL.y, NORMAL.z));
					}
				}
			}
			
			ImmutableMap.Builder<String, ClothPart> partBuilder = ImmutableMap.builder();
			
			for (Map.Entry<String, SoftBodyTranslatable.ClothSimulationInfo> entry : provider.getSoftBodySimulationInfo().entrySet()) {
				partBuilder.put(entry.getKey(), new ClothPart(entry.getValue(), positions));
			}
			
			this.parts = partBuilder.build();
		}
		
		private ClothObject(ClothObject copyTarget) {
			this.provider = copyTarget.provider;
			this.parts = copyTarget.parts;
			
			this.particles = new HashMap<> ();
			this.normalOffsetParticles = new HashMap<> ();
			
			for (Map.Entry<Integer, Particle> entry : copyTarget.particles.entrySet()) {
				this.particles.put(entry.getKey(), entry.getValue().copy());
			}
			
			for (Map.Entry<Integer, ClothPart.OffsetParticle> entry : copyTarget.normalOffsetParticles.entrySet()) {
				this.normalOffsetParticles.put(entry.getKey(), entry.getValue().copy());
			}
			
			for (Map.Entry<Integer, ClothPart.OffsetParticle> entry : copyTarget.normalOffsetParticles.entrySet()) {
				this.normalOffsetParticles.put(entry.getKey(), entry.getValue().copy());
			}
			
			this.particleNormals = ImmutableList.copyOf(copyTarget.particleNormals);
			this.parentJoint = copyTarget.parentJoint;
		}
		
		public ClothObject captureMyself() {
			return new ClothObject(this);
		}
		
		private static final int SUB_STEPS = 6;
		private static final Vector3f EXTERNAL_FORCE = new Vector3f();
		private static final Vector3f OFFSET = new Vector3f();
		private static final Vector3f CENTRIFUGAL = new Vector3f();
		private static final Vector3f CIRCULAR = new Vector3f();
		
		private static final Matrix4f[] BOUND_ANIMATION_TRANSFORM = Matrix4fUtils.allocateArray(EpicFightSharedConstants.MAX_JOINTS);
		private static final Matrix4f COLLIDER_TRANSFORM = new Matrix4f();
		private static final Matrix4f TO_CENTRIFUGAL = new Matrix4f();
		private static final Matrix4f OBJECT_TRANSFORM = new Matrix4f();
		private static final Matrix4f INVERTED = new Matrix4f();
		private static final Quaternionf ROTATOR = new Quaternionf();
		
		/**
		 * This method needs be called before drawing simulated cloth
		 */
		public void tick(ClothSimulatable simulatableObj, Function<Float, Matrix4f> colliderTransformGetter, float partialTick, @Nullable Armature armature, @Nullable Matrix4f[] poses) {
			// Configure developer options
			//drawMeshColliders(true);
			//drawNormalOffset(false);
			//drawOutlines(true);
			
			// Revert
			//drawMeshColliders(false);
			//drawNormalOffset(true);
			//drawOutlines(false);
			
			if (!Minecraft.getInstance().isPaused()) {
				boolean skinned = poses != null && armature != null;
				
				for (int j = 0; j < armature.getJointNumber(); j++) {
					if (skinned) {
						BOUND_ANIMATION_TRANSFORM[j].set(poses[j]);
						BOUND_ANIMATION_TRANSFORM[j].mul(armature.searchJointById(j).getToOrigin());
						Matrix4f buffer = new Matrix4f().setTranslation(BOUND_ANIMATION_TRANSFORM[j].getTranslation(new Vector3f())).rotation(BOUND_ANIMATION_TRANSFORM[j].getNormalizedRotation(new Quaternionf()));
						BOUND_ANIMATION_TRANSFORM[j].set(buffer);
					}
				}
				
				float deltaFrameTime = Minecraft.getInstance().getDeltaFrameTime();
				float subStebInvert = 1.0F / SUB_STEPS;
				float subSteppingDeltaTime = deltaFrameTime * subStebInvert;
				float gravity = simulatableObj.getGravity() * subSteppingDeltaTime * EpicFightSharedConstants.A_TICK;
				
				// Update circular force
				float yRot = Mth.wrapDegrees(Mth.rotLerp(partialTick, Mth.wrapDegrees(simulatableObj.getYRotO()), Mth.wrapDegrees(simulatableObj.getYRot())));
				
				TO_CENTRIFUGAL.set(BOUND_ANIMATION_TRANSFORM[this.parentJoint.getId()]);
				TO_CENTRIFUGAL.mulLocal(new Matrix4f().rotation(Math.toRadians(-yRot + 180.0F), 0, 1, 0));
				TO_CENTRIFUGAL.getNormalizedRotation(ROTATOR);
				
				Vec3 velocity = simulatableObj.getObjectVelocity();
				float delta = MathUtils.wrapRadian(MathUtils.getAngleBetween(this.rotationO, ROTATOR));
				float speed = Math.min((float)velocity.length() * deltaFrameTime, 0.2F);
				float rotationForce = Math.abs(delta);
				
				this.rotationO.set(ROTATOR);
				
				Matrix4fUtils.transform3v(TO_CENTRIFUGAL, new Vector3f(0, 0, 1), CENTRIFUGAL);
				int deltaSign = Math.abs(delta) < 0.02D ? 0 : MathUtils.getSign(delta);
				
				if (deltaSign == 0) {
					CIRCULAR.set(new Vector3f());
				} else {
					CENTRIFUGAL.sub(centrifugalO, CIRCULAR);
					CIRCULAR.normalize();
				}
				
				this.centrifugalO.set(CENTRIFUGAL);
				
				CENTRIFUGAL.mul(rotationForce * (1.0F + speed * 50.0F));
				CIRCULAR.mul(rotationForce * (1.0F + speed * 50.0F));
				velocity = velocity.scale(rotationForce);
				CIRCULAR.add(CENTRIFUGAL, EXTERNAL_FORCE);
				EXTERNAL_FORCE.add(velocity.toVector3f());
				
				// Reset normal vectors
				this.particleNormals.forEach((poseNormals) -> poseNormals.values().forEach((vec3f) -> vec3f.set(0.0F, 0.0F, 0.0F)));
				
				Vec3 pos = simulatableObj.getAccuratePartialLocation(partialTick);
				float yRotLerp = simulatableObj.getAccurateYRot(partialTick);
				Matrix4f objectTransform = OBJECT_TRANSFORM.setTranslation((float)pos.x, (float)pos.y, (float)pos.z).rotate(Math.toRadians(180.0F - yRotLerp), 0, 1, 0);
				objectTransform.invert(INVERTED);
				
				for (ClothPart part : this.parts.values()) {
					part.tick(objectTransform, EXTERNAL_FORCE, skinned ? BOUND_ANIMATION_TRANSFORM : null);
				}
				
				for (int i = 0; i < SUB_STEPS; i++) {
					float substepPartialTick = partialTick - deltaFrameTime + subSteppingDeltaTime * (i + 1);
					
					if (this.clothColliders != null) {
						simulatableObj.getArmature().setPose(simulatableObj.getSimulatableAnimator().getPose(Mth.clamp(substepPartialTick, 0.0F, 1.0F)));
						Matrix4f colliderTransform = colliderTransformGetter.apply(substepPartialTick);
						
						for (Pair<Function<ClothSimulatable, Matrix4f>, ClothSimulator.ClothOBBCollider> entry : this.clothColliders) {
							entry.getSecond().transform(Matrix4fUtils.mulBoth(colliderTransform, entry.getFirst().apply(simulatableObj), COLLIDER_TRANSFORM));
						}
					}
					
					for (ClothPart part : this.parts.values()) {
						part.substepTick(gravity, subSteppingDeltaTime, i + 1, this.clothColliders);
					}
				}
			}
			
			this.updateNormal(false);
			
			// Update normals & offset particles
			if (!this.normalOffsetParticles.isEmpty()) {
				for (ClothPart.OffsetParticle offsetParticle : this.normalOffsetParticles.values()) {
					// Update offset positions
					Particle rootParticle = offsetParticle.rootParticle();
					Map<Integer, Vector3f> rootNormalMap = this.particleNormals.get(rootParticle.meshVertexId);
					OFFSET.set(0.0F, 0.0F, 0.0F);
					
					for (Integer normIdx : offsetParticle.positionNormalMembers()) {
						OFFSET.add(rootNormalMap.get(normIdx).normalize());
					}
					
					OFFSET.mul(offsetParticle.length / OFFSET.length());
					
					offsetParticle.position.set(
						  rootParticle.position.x - OFFSET.x
						, rootParticle.position.y - OFFSET.y
						, rootParticle.position.z - OFFSET.z
					);
				}
			}
			
			this.updateNormal(true);
			this.captureModelPosition(INVERTED);
		}
		
		private static final Vector3f TO_P2 = new Vector3f();
		private static final Vector3f TO_P3 = new Vector3f();
		private static final Vector3f CROSS = new Vector3f();
		
		// Calculate vertex normals
		private void updateNormal(boolean updateOffsetParticles) {
			SoftBodyTranslatable softBodyMesh = this.provider;
			
			for (MeshPart modelPart : softBodyMesh.getOriginalMesh().getAllParts()) {
				for (int i = 0; i < modelPart.getVertices().size() / 3; i++) {
					VertexBuilder triP1 = modelPart.getVertices().get(i * 3);
					VertexBuilder triP2 = modelPart.getVertices().get(i * 3 + 1);
					VertexBuilder triP3 = modelPart.getVertices().get(i * 3 + 2);
					
					if (!this.particles.containsKey(triP1.position) || !this.particles.containsKey(triP2.position) || !this.particles.containsKey(triP3.position)) {
						if (!updateOffsetParticles) {
							continue;
						}
					} else {
						if (updateOffsetParticles) {
							continue;
						}
					}
					
					Vector3f p1Pos = this.getParticlePosition(triP1.position);
					Vector3f p2Pos = this.getParticlePosition(triP2.position);
					Vector3f p3Pos = this.getParticlePosition(triP3.position);
					p2Pos.sub(p1Pos, TO_P2).cross(p3Pos.sub(p1Pos, TO_P3), CROSS);
					CROSS.normalize();
					
					Map<Integer, Vector3f> triP1Normals = particleNormals.get(triP1.position);
					Map<Integer, Vector3f> triP2Normals = particleNormals.get(triP2.position);
					Map<Integer, Vector3f> triP3Normals = particleNormals.get(triP3.position);
					
					triP1Normals.get(triP1.normal).add(CROSS);
					triP2Normals.get(triP2.normal).add(CROSS);
					triP3Normals.get(triP3.normal).add(CROSS);
				}
			}
		}
		
		private static final Vector3f SCALE = new Vector3f();
		
		public void scaleFromPose(PoseStack poseStack, Matrix4f[] poses) {
			Matrix4f poseMat = poses[this.parentJoint.getId()];
			poseMat.getScale(SCALE);
			
			poseStack.translate(poseMat.m30(), poseMat.m31(), poseMat.m32());
			poseStack.scale(SCALE.x, SCALE.y, SCALE.z);
			poseStack.translate(-poseMat.m30(), -poseMat.m31(), -poseMat.m32());
		}
		
		@Override
		public void draw(PoseStack poseStack, VertexConsumer bufferBuilder, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay) {
			if (DRAW_OUTLINES) {
				this.drawOutline(poseStack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines()), Mesh.DrawingFunction.POSITION_COLOR_NORMAL, r, g, b, a);
			} else {
				this.drawParts(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay);
			}
			
			if (this.provider instanceof CompositeMesh compositeMesh) {
				poseStack.popPose();
				compositeMesh.getStaticMesh().draw(poseStack, bufferBuilder, drawingFunction, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, overlay);
				poseStack.pushPose();
			}
		}
		
		private static final Vector3f SCALER = new Vector3f();
		
		@Override
		public void drawPosed(PoseStack poseStack, VertexConsumer bufferBuilder, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay, Armature armature, Matrix4f[] poses) {
			if (DRAW_OUTLINES) {
				this.drawOutline(poseStack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines()), Mesh.DrawingFunction.POSITION_COLOR_NORMAL, r, g, b, a);
			} else {
				this.drawParts(poseStack, bufferBuilder, drawingFunction, packedLight, r, g, b, a, overlay);
			}
			
			if (DRAW_MESH_COLLIDERS && this.clothColliders != null) {
				for (Pair<Function<ClothSimulatable, Matrix4f>, ClothSimulator.ClothOBBCollider> entry : this.clothColliders) {
					entry.getSecond().draw(poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), 0xFFFFFFFF);
				}
			}
			
			// Remove entity inverted world translation while keeping the scale
			poseStack.last().pose().getScale(SCALER);
			float scaleX = SCALER.x;
			float scaleY = SCALER.y;
			float scaleZ = SCALER.z;
			
			poseStack.popPose();
			poseStack.last().pose().getScale(SCALER);
			float xDiv = scaleX / SCALER.x;
			float yDiv = scaleY / SCALER.y;
			float zDiv = scaleZ / SCALER.z;
			
			poseStack.scale(xDiv, yDiv, zDiv);
			
			if (this.provider instanceof CompositeMesh compositeMesh) {
				compositeMesh.getStaticMesh().drawPosed(poseStack, bufferBuilder, drawingFunction, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, overlay, armature, poses);
			}
			
			poseStack.pushPose();
		}
		
		public Vector3f getParticlePosition(int idx) {
			if (this.particles.containsKey(idx)) {
				return this.particles.get(idx).position;
			} else {
				return this.normalOffsetParticles.get(idx).position;
			}
		}
		
		private void captureModelPosition(Matrix4f objectTranslformInv) {
			for (Particle p : this.particles.values()) {
				Matrix4fUtils.transform3v(objectTranslformInv, p.position, p.modelPosition);
			}
		}
		
		public void drawParts(PoseStack poseStack, VertexConsumer bufferBuilder, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay) {
			SoftBodyTranslatable softBodyMesh = ClothObject.this.provider;
			float[] uvs = softBodyMesh.getOriginalMesh().uvs();
			
			for (MeshPart meshPart : softBodyMesh.getOriginalMesh().getAllParts()) {
				if (meshPart.isHidden()) {
					continue;
				}
				
				Vector4f color = meshPart.getColor(r, g, b, a);
				Matrix4f matrix4f = poseStack.last().pose();
				Matrix3f matrix3f = poseStack.last().normal();
				
				for (int i = 0; i < meshPart.getVertices().size(); i++) {
					if (!DRAW_NORMAL_OFFSET && i % 3 == 0) {
						if (i + 1 == meshPart.getVertices().size() || i + 2 == meshPart.getVertices().size()) {
							
						} else {
							VertexBuilder v1 = meshPart.getVertices().get(i);
							VertexBuilder v2 = meshPart.getVertices().get(i + 1);
							VertexBuilder v3 = meshPart.getVertices().get(i + 2);
							
							if ((!this.particles.containsKey(v1.position) || !this.particles.containsKey(v2.position) || !this.particles.containsKey(v3.position))) {
								i += 2;
								continue;
							}
						}
					}
					
					VertexBuilder vb = meshPart.getVertices().get(i);
					Vector3f particlePosition = this.getParticlePosition(vb.position);
					Vector3f poseNormal = this.particleNormals.get(vb.position).get(vb.normal);
					poseNormal.normalize();
					
					POSITION.set(particlePosition.x, particlePosition.y, particlePosition.z);
					NORMAL.set(poseNormal.x, poseNormal.y, poseNormal.z);
					POSITION.mul(matrix4f);
					NORMAL.mul(matrix3f);
					
					drawingFunction.draw(bufferBuilder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), packedLight, color.x, color.y, color.z, color.w, uvs[vb.uv * 2], uvs[vb.uv * 2 + 1], overlay);
				}
			}
		}
		
		public void drawOutline(PoseStack poseStack, VertexConsumer builder, Mesh.DrawingFunction drawingFunction, float r, float g, float b, float a) {

            for (MeshPart meshPart : this.provider.getOriginalMesh().getAllParts()) {
				if (meshPart.isHidden()) {
					continue;
				}
				
				Matrix4f matrix4f = poseStack.last().pose();
				Matrix3f matrix3f = poseStack.last().normal();
				
				for (int i = 0; i < meshPart.getVertices().size() / 3; i++) {
					VertexBuilder v1 = meshPart.getVertices().get(i * 3);
					VertexBuilder v2 = meshPart.getVertices().get(i * 3 + 1);
					VertexBuilder v3 = meshPart.getVertices().get(i * 3 + 2);
					
					if (!DRAW_NORMAL_OFFSET && (!this.particles.containsKey(v1.position) || !this.particles.containsKey(v2.position) || !this.particles.containsKey(v3.position))) {
						continue;
					}
					
					Vector3f pos1 = this.getParticlePosition(v1.position);
					Vector3f pos2 = this.getParticlePosition(v2.position);
					Vector3f pos3 = this.getParticlePosition(v3.position);
					
					POSITION.set(pos1.x, pos1.y, pos1.z);
					NORMAL.set(pos2.x - pos1.x, pos2.x - pos1.x, pos2.x - pos1.x);
					POSITION.mul(matrix4f);
					NORMAL.mul(matrix3f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
					POSITION.set(pos2.x, pos2.y, pos2.z);
					POSITION.mul(matrix4f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
					
					POSITION.set(pos2.x, pos2.y, pos2.z);
					NORMAL.set(pos3.x - pos2.x, pos3.x - pos2.x, pos3.x - pos2.x);
					POSITION.mul(matrix4f);
					NORMAL.mul(matrix3f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
					POSITION.set(pos3.x, pos3.y, pos3.z);
					POSITION.mul(matrix4f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
					
					POSITION.set(pos3.x, pos3.y, pos3.z);
					NORMAL.set(pos1.x - pos3.x, pos1.x - pos3.x, pos1.x - pos3.x);
					POSITION.mul(matrix4f);
					NORMAL.mul(matrix3f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
					POSITION.set(pos1.x, pos1.y, pos1.z);
					POSITION.mul(matrix4f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
				}
			}
		}
		
		public void drawNormals(PoseStack poseStack, VertexConsumer builder, Mesh.DrawingFunction drawingFunction, float r, float g, float b, float a) {
			if (!this.normalOffsetParticles.isEmpty()) {
				Matrix4f matrix4f = poseStack.last().pose();
				Matrix3f matrix3f = poseStack.last().normal();
				
				for (ClothPart.OffsetParticle offsetParticle : this.normalOffsetParticles.values()) {
					// Update offset positions
					Particle rootParticle = offsetParticle.rootParticle();
					Map<Integer, Vector3f> rootNormalMap = this.particleNormals.get(rootParticle.meshVertexId);
					
					if (rootNormalMap.size() < 2) {
						continue;
					}
					
					OFFSET.set(0.0F, 0.0F, 0.0F);
					
					for (Integer normIdx : offsetParticle.positionNormalMembers()) {
						OFFSET.add(rootNormalMap.get(normIdx).normalize());
					}
					
					OFFSET.mul(offsetParticle.length / OFFSET.length());
					
					Vector3f rootpos = this.getParticlePosition(rootParticle.meshVertexId);
					
					POSITION.set(rootpos.x, rootpos.y, rootpos.z);
					NORMAL.set(-OFFSET.x, -OFFSET.x, -OFFSET.x);
					POSITION.mul(matrix4f);
					NORMAL.mul(matrix3f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
					POSITION.set(rootpos.x - OFFSET.x, rootpos.y - OFFSET.y, rootpos.z - OFFSET.z);
					POSITION.mul(matrix4f);
					drawingFunction.draw(builder, POSITION.x, POSITION.y, POSITION.z, NORMAL.x(), NORMAL.y(), NORMAL.z(), -1, r, g, b, a, 0, 0, 0);
				}
			}
		}
		
		@Override
		public void initialize() {
		}
		
		@OnlyIn(Dist.CLIENT)
        static class Particle {
			final Vector3f position;
			final Vector3f modelPosition;
			final Vector3f velocity = new Vector3f();
			
			final float influence;
			final float rootDistance;
			final int meshVertexId;
			boolean collided;
			
			Particle(Vector3f position, float influence, float rootDistance, int meshVertexId) {
				this.position = position;
				this.modelPosition = new Vector3f(position);
				this.influence = influence;
				this.rootDistance = rootDistance;
				this.meshVertexId = meshVertexId;
				this.collided = false;
			}
			
			Particle copy() {
				return new Particle(new Vector3f(position), this.influence, this.rootDistance, this.meshVertexId);
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public class ClothPart {
			final List<Particle> particleList;
			final List<ConstraintList> constraints;
			final Multimap<Integer, Particle> spatialHash;
			final float selfCollision;
			final float particleMass;
			final int hashTableSize;
			
			private static final Vector3f AVERAGE = new Vector3f();
			
			ClothPart(SoftBodyTranslatable.ClothSimulationInfo clothInfo, float[] positions) {
				this.particleList = Lists.newArrayList();
				ImmutableList.Builder<ConstraintList> constraintsBuilder = ImmutableList.builder();
				
				this.selfCollision = clothInfo.selfCollision();
				this.particleMass = clothInfo.particleMass();

                for (int i = 0; i < clothInfo.particles().length / 2; i++) {
					int positionIndex = clothInfo.particles()[i * 2];
					int weightIndex = clothInfo.particles()[i * 2 + 1];
					float influence = clothInfo.weights()[weightIndex];
					float rootDistance = clothInfo.rootDistance()[i];
					float x = positions[positionIndex * 3];
					float y = positions[positionIndex * 3 + 1];
					float z = positions[positionIndex * 3 + 2];
					
					Particle particle = new Particle(new Vector3f(x, y, z), influence, rootDistance, positionIndex);
					ClothObject.this.particles.put(positionIndex, particle);
					this.particleList.add(particle);
				}
				
				this.hashTableSize = this.particleList.size() * 2;
				this.spatialHash = HashMultimap.create(this.hashTableSize, 2);
				int idx = 0;

                for (int[] constraints : clothInfo.constraints()) {
					float compliance = clothInfo.compliances()[idx];
					ConstraintType constraintType = clothInfo.constraintTypes()[idx];
					List<Constraint> constraintList;
					idx++;
					
					switch(constraintType) {
					case STRETCHING -> {
						constraintList = new ArrayList<> (constraints.length / 2);
						
						for (int i = 0; i < constraints.length / 2; i++) {
							int idx1 = constraints[i * 2];
							int idx2 = constraints[i * 2 + 1];
							
							constraintList.add(new StretchingConstraint(ClothObject.this.particles.get(idx1), ClothObject.this.particles.get(idx2)));
						}
						
						constraintsBuilder.add(new ConstraintList(compliance, constraintType, constraintList));
					}
					case SHAPING -> {
						constraintList = new ArrayList<> (constraints.length / 2);
						
						for (int i = 0; i < constraints.length / 2; i++) {
							int idx1 = constraints[i * 2];
							int idx2 = constraints[i * 2 + 1];
							
							constraintList.add(new ShapingConstraint(ClothObject.this.particles.get(idx1), ClothObject.this.particles.get(idx2)));
						}
						
						constraintsBuilder.add(new ConstraintList(compliance, constraintType, constraintList));
					}
					case BENDING -> {
						constraintList = new ArrayList<> (constraints.length / 4);
						
						for (int i = 0; i < constraints.length / 4; i++) {
							int idx1 = constraints[i * 4];
							int idx2 = constraints[i * 4 + 1];
							int idx3 = constraints[i * 4 + 2];
							int idx4 = constraints[i * 4 + 3];
							
							constraintList.add(new BendingConstraint(ClothObject.this.particles.get(idx1), ClothObject.this.particles.get(idx2), ClothObject.this.particles.get(idx3), ClothObject.this.particles.get(idx4)));
						}
						
						constraintsBuilder.add(new ConstraintList(compliance, constraintType, constraintList));
					}
					case VOLUME -> {
						constraintList = new ArrayList<> (constraints.length / 4);
						
						for (int i = 0; i < constraints.length / 4; i++) {
							int idx1 = constraints[i * 4];
							int idx2 = constraints[i * 4 + 1];
							int idx3 = constraints[i * 4 + 2];
							int idx4 = constraints[i * 4 + 3];
							
							constraintList.add(new VolumeConstraint(ClothObject.this.particles.get(idx1), ClothObject.this.particles.get(idx2), ClothObject.this.particles.get(idx3), ClothObject.this.particles.get(idx4)));
						}
						
						constraintsBuilder.add(new ConstraintList(compliance, constraintType, constraintList));
					}
					}
				}
				
				this.constraints = constraintsBuilder.build();

                if (clothInfo.normalOffsetMapping() != null) {
					for (int i = 0; i < clothInfo.normalOffsetMapping().length / 2; i++) {
						int rootParticle = clothInfo.normalOffsetMapping()[i * 2];
						int offsetParticleIdx = clothInfo.normalOffsetMapping()[i * 2 + 1];
						Vector3f offsetDirection = new Vector3f( positions[offsetParticleIdx * 3] - positions[rootParticle * 3]
														 , positions[offsetParticleIdx * 3 + 1] - positions[rootParticle * 3 + 1]
														 , positions[offsetParticleIdx * 3 + 2] - positions[rootParticle * 3 + 2]);
						
						List<Integer> positionNormalMembers = Lists.newArrayList();
						List<Integer> inverseNormals = Lists.newArrayList();
						OffsetParticle offsetParticle = new OffsetParticle(offsetParticleIdx, offsetDirection.length(), ClothObject.this.particles.get(rootParticle), new Vector3f(), positionNormalMembers, inverseNormals);
						offsetDirection.normalize();
						
						Map<Integer, Vector3f> rootNormalMap = particleNormals.get(rootParticle);
						List<Vector3f> rootNormals = new ArrayList<> (rootNormalMap.values());
						List<Set<Integer>> normalSubsets = new ArrayList<> (MathUtils.getSubset(IntStream.rangeClosed(0, rootNormals.size() - 1).boxed().toList()));
						int candidate = -1;
						int loopIdx = 0;
						float maxDot = -10000.0F;
						
						for (Set<Integer> subset : normalSubsets) {
							Set<Vector3f> rootNormal = subset.stream().map(rootNormals::get).collect(Collectors.toSet());
							VectorUtils.average(rootNormal, AVERAGE);
							AVERAGE.mul(-1.0F);
							AVERAGE.normalize();
							
							float dot = offsetDirection.dot(AVERAGE);
							if (maxDot < dot) {
								maxDot = dot;
								candidate = loopIdx;
							}
							
							loopIdx++;
						}
						
						normalSubsets.get(candidate).forEach((orderIdx) -> {
							int iterCount = 0;

                            for (Map.Entry<Integer, Vector3f> entry : rootNormalMap.entrySet())
                            {
                                if (orderIdx == iterCount)
                                {
                                    positionNormalMembers.add(entry.getKey());
                                    break;
                                }

                                iterCount++;
                            }
						});
						
						normalOffsetParticles.put(offsetParticleIdx, offsetParticle);
						
						for (Vector3f normal : particleNormals.get(offsetParticleIdx).values()) {
							int leastDotIdx = VectorUtils.getLeastAngleVectorIdx(normal, rootNormals.toArray(new Vector3f[0]));
							int iterCount = 0;

                            for (Map.Entry<Integer, Vector3f> entry : rootNormalMap.entrySet())
                            {
                                if (leastDotIdx == iterCount)
                                {
                                    inverseNormals.add(entry.getKey());
                                    break;
                                }

                                iterCount++;
                            }
						}
					}
				}
			}
			
			public void buildSpatialHash() {
				this.spatialHash.clear();
				
				// Create spatial hash map
				for (Particle p : this.particleList) {
					int hash = this.getHash(p.position.x, p.position.y, p.position.z);
					this.spatialHash.put(hash, p);
				}
			}
			
			// Storage vectors
			private static final Vector3f VEC3F = new Vector3f();
			private static final Vector4f POSITION = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
			private static final Vector3f DIFF = new Vector3f();
			
			// Setup root particles transform
			public void tick(Matrix4f objectTransform, Vector3f externalForce, Matrix4f[] poses) {
				for (Particle p : this.particleList) {
					p.velocity.mul(0.92F);
					
					p.velocity.add(
						  externalForce.x * p.rootDistance * p.influence * this.particleMass
						, externalForce.y * p.rootDistance * p.influence * this.particleMass
						, externalForce.z * p.rootDistance * p.influence * this.particleMass
					);
					
					if (p.collided) {
						VEC3F.set(p.modelPosition);
						Matrix4fUtils.transform3v(objectTransform, VEC3F, TRASNFORMED);
						p.position.set(TRASNFORMED);
					} else {
						float influenceInv = 1.0F - p.influence;
						
						// Apply animation transform
						if (influenceInv > 0.0F) {
							ClothObject.this.provider.getOriginalMesh().getVertexPosition(p.meshVertexId, POSITION, poses);
							VEC3F.set(POSITION.x, POSITION.y, POSITION.z);
							Matrix4fUtils.transform3v(objectTransform, VEC3F, TRASNFORMED);
							VectorUtils.lerp(p.position, TRASNFORMED, influenceInv, TRASNFORMED);
							p.position.set(TRASNFORMED);
						}
					}
				}
			}
			
			private static final Vector3f PARTIAL_VELOCITY = new Vector3f();
			
			// Apply external forces, constraints, self collision, and mesh collision
			public void substepTick(float substepGravity, float substepDeltaTime, int stepCount, List<Pair<Function<ClothSimulatable, Matrix4f>, ClothSimulator.ClothOBBCollider>> clothColliders) {
				for (Particle p : this.particleList) {
					p.position.y -= substepGravity * this.particleMass * p.influence;
					p.position.add(p.velocity.mul(PARTIAL_VELOCITY, new Vector3f()).mul(1.0F / SUB_STEPS));
				}
				
				for (ConstraintList constraintsBundle : this.constraints) {
					float alpha = constraintsBundle.compliance() / (substepDeltaTime * substepDeltaTime);
					
					for (Constraint c : constraintsBundle.constraints()) {
						c.solve(alpha, stepCount);
					}
				}
				
				if (stepCount == 1) {
					this.buildSpatialHash();
				}
				
				// Detect self collision
				for (Particle p1 : this.particleList) {
					int hash = this.getHash(p1.position.x, p1.position.y, p1.position.z);
					
					for (Particle p2 : this.spatialHash.get(hash)) {
						if (p1 == p2) {
							continue;
						}
						
						float influenceSum = p1.influence + p2.influence;
						
						if (influenceSum == 0.0F) {
							continue;
						}
						
						p1.position.sub(p2.position, VEC3F);
						float length = VEC3F.length();
						
						if (length < this.selfCollision) {
							float scale = (this.selfCollision - length) / this.selfCollision;
							float p1Move = p1.influence / influenceSum;
							float p2Move = p2.influence / influenceSum;
							VEC3F.mul(scale);
							
							p1.position.add(VEC3F.x * p1Move, VEC3F.y * p1Move, VEC3F.z * p1Move);
							p2.position.sub(VEC3F.x * p2Move, VEC3F.y * p2Move, VEC3F.z * p2Move);
						}
					}
				}
				
				// Detect collision with mesh collider
				if (clothColliders != null) {
					for (ConstraintList constraintList : this.constraints) {
						if (constraintList.constraintType() == ConstraintType.SHAPING) {
							@SuppressWarnings("unchecked")
							List<ShapingConstraint> constraints = (List<ShapingConstraint>)constraintList.constraints();
							List<ClothSimulator.ClothOBBCollider> colliders = Lists.newArrayList();
							List<Vector3f> destinations = Lists.newArrayList();
							
							for (ShapingConstraint constraint : constraints) {
								if (constraint.p1.influence == 0.0F && constraint.p2.influence == 0.0F) {
									continue;
								}
								
								for (Pair<Function<ClothSimulatable, Matrix4f>, ClothSimulator.ClothOBBCollider> entry : clothColliders) {
									ClothSimulator.ClothOBBCollider clothCollider = entry.getSecond();
									
									if (clothCollider.getOuterAABB(this.selfCollision * 0.5F).contains(constraint.p2.position.x, constraint.p2.position.y, constraint.p2.position.z)) {
										if (!clothCollider.doesPointCollide(new Vec3(constraint.p1.position), this.selfCollision * 0.5F)) {
											colliders.add(entry.getSecond());
										}
									}
								}
								
								for (ClothSimulator.ClothOBBCollider collider : colliders) {
									collider.pushIfPointInside(constraint.p2.position, constraint.p1.position, this.selfCollision * 0.5F, destinations, colliders);
									//collider.pushIfEdgeCollidesCircular(constraint, this.selfCollision * 0.5F, destinations, colliders);
								}

								int i = VectorUtils.getNearest(constraint.p2.position, destinations);
								constraint.p2.collided = i != -1;
								
								if (i != -1) {
									Vector3f nearest = destinations.get(i);
									nearest.sub(constraint.p2.position, DIFF);
									
									//constraint.p2.velocity.add(DIFF);
									constraint.p2.position.set(nearest);
								}
								
								colliders.clear();
								destinations.clear();
							}
						}
					}
				}
			}
			
			private int getHash(double x, double y, double z) {
				int xi = (int)Math.floor(x / SPATIAL_HASH_SPACING);
				int yi = (int)Math.floor(y / SPATIAL_HASH_SPACING);
				int zi = (int)Math.floor(z / SPATIAL_HASH_SPACING);
				int hash = (xi * 92837111) ^ (yi * 689287499) ^ (zi * 283923481);
				
				return Math.abs(hash) % this.hashTableSize;
			}
			
			@OnlyIn(Dist.CLIENT)
			public enum ConstraintType {
				STRETCHING, SHAPING, BENDING, VOLUME
			}
			
			@OnlyIn(Dist.CLIENT)
			public static record ConstraintList(float compliance, ConstraintType constraintType, List<? extends Constraint> constraints) {
			}
			
			@OnlyIn(Dist.CLIENT)
			public static record OffsetParticle(int offsetVertexId, float length, Particle rootParticle, Vector3f position, List<Integer> positionNormalMembers, List<Integer> inverseNormal) {
				public OffsetParticle copy() {
					return new OffsetParticle(this.offsetVertexId, this.length, this.rootParticle, this.position.mul(1, new Vector3f()), this.positionNormalMembers, this.inverseNormal);
				}
			}
			
			@OnlyIn(Dist.CLIENT)
			abstract static class Constraint {
				abstract void solve(float alpha, int stepcount);
			}
			
			/**
			 * A constraint that restricts stretching of two particles
			 */
			@OnlyIn(Dist.CLIENT)
            static
            class StretchingConstraint extends Constraint {
				final Particle p1;
				final Particle p2;
				final float restLength;
				
				// Storage vector
				static final Vector3f GRADIENT = new Vector3f();
				
				StretchingConstraint(Particle p1, Particle p2) {
					this.p1 = p1;
					this.p2 = p2;
					this.restLength = p1.position.distance(p2.position);
				}
				
				@Override
				void solve(float alpha, int stepcount) {
					float p1Influence = this.p1.influence;
					float p2Influence = this.p2.influence;
				    float influenceSum = p1Influence + p2Influence;
				    
				    if (influenceSum < 1E-8) {
				        return;
				    }
				    p2.position.sub(p1.position, GRADIENT);
				    float currentLength = GRADIENT.length();
				    
				    if (currentLength < 1E-8) {
				        return;
				    }
				    
				    // Normalize
				    GRADIENT.mul(1.0F / currentLength);
				    
				    float constraint = currentLength - this.restLength;
				    float force = constraint / (influenceSum + alpha);
				    float p1Move = force * p1Influence;
				    float p2Move = -force * p2Influence;
				    
				    this.p1.position.add(GRADIENT.x * p1Move, GRADIENT.y * p1Move, GRADIENT.z * p1Move);
				    this.p2.position.add(GRADIENT.x * p2Move, GRADIENT.y * p2Move, GRADIENT.z * p2Move);
				}
			}
			

			@OnlyIn(Dist.CLIENT)
            static
            class ShapingConstraint extends Constraint {
				final Particle p1;
				final Particle p2;
				final float restLength;
				
				// Storage vector
				static final Vector3f TOWARD = new Vector3f();
				
				ShapingConstraint(Particle p1, Particle p2) {
					this.p1 = p1;
					this.p2 = p2;
					this.restLength = p1.position.distance(p2.position);
				}
				
				@Override
				void solve(float alpha, int stepcount) {
					float p1Influence = (stepcount == SUB_STEPS && !this.p1.collided) ? 0.0F : this.p1.influence;
					float p2Influence = this.p2.influence;
					
				    float influenceSum = p1Influence + p2Influence;
				    
				    if (influenceSum < 1E-5) {
				        return;
				    }
				    p2.position.sub(p1.position, TOWARD);
				    float distanceLength = TOWARD.length();
				    
				    if (distanceLength == 0.0F) {
				        return;
				    }
				    
				    //Normalize
				    TOWARD.mul(1.0F / distanceLength);
				    float distanceGap = distanceLength - this.restLength;
				    float force = distanceGap / (influenceSum + alpha);
				    float p1Move = force * p1Influence;
				    float p2Move = -force * p2Influence;
				    
				    this.p1.position.add(TOWARD.x * p1Move, TOWARD.y * p1Move, TOWARD.z * p1Move);
				    this.p2.position.add(TOWARD.x * p2Move, TOWARD.y * p2Move, TOWARD.z * p2Move);
				}
			}

			@OnlyIn(Dist.CLIENT)
            static
            class BendingConstraint extends Constraint {
				final Particle p1;
				final Particle p2;
				final Particle p3;
				final Particle p4;
				final float restAngle;
				final float oppositeDistance;
				
				// Storage vector
				static final Vector3f[] GRADIENTS = { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
				static final Vector3f NORMAL_SUM = new Vector3f();
				static float STIFFNESS = 1.0F;
				
				BendingConstraint(Particle p1, Particle p2, Particle p3, Particle p4) {
					this.p1 = p1;
					this.p2 = p2;
					this.p3 = p3;
					this.p4 = p4;
					this.restAngle = this.getDihedralAngle();
					oppositeDistance = p1.position.distance(p4.position);
				}
				
				@Override
				void solve(float alpha, int stepcount) {
				    float influenceSum = this.p1.influence + this.p2.influence + this.p3.influence + this.p4.influence;
				    
				    if (influenceSum < 1E-8) {
				        return;
				    }
				    
					float currentAngle = this.getDihedralAngle();
				    float constraint = (this.restAngle - currentAngle);
				    
				    while (constraint > Math.PI) {
				    	constraint -= (float) (Math.PI * 2);
				    }
				    
				    while (constraint < -Math.PI) {
				    	constraint += (float) (Math.PI * 2);
				    }
				    
				    // radian angle * diameter
				    constraint = this.oppositeDistance * constraint;
				    
				    float edgeLength = EDGE.length();
				    
				    CROSS1.mul(edgeLength);
				    CROSS2.mul(edgeLength);
				    GRADIENTS[0].set(CROSS1);
				    GRADIENTS[3].set(CROSS2);

					CROSS1.add(CROSS2, NORMAL_SUM);
				    NORMAL_SUM.mul(0.5F);
				    GRADIENTS[1].set(NORMAL_SUM);
				    GRADIENTS[2].set(NORMAL_SUM);
				    
				    float weight = this.p1.influence * GRADIENTS[0].lengthSquared()
				    				+ this.p2.influence * GRADIENTS[1].lengthSquared()
				    				+ this.p3.influence * GRADIENTS[2].lengthSquared()
				    				+ this.p4.influence * GRADIENTS[3].lengthSquared();
				    
				    if (weight < 1E-8) {
						return;
					}
				    
				    float force = (-constraint * STIFFNESS) / (influenceSum + alpha);
				    
				    GRADIENTS[0].mul(force * this.p1.influence);
				    GRADIENTS[1].mul(force * this.p2.influence);
				    GRADIENTS[2].mul(force * this.p3.influence);
				    GRADIENTS[3].mul(force * this.p4.influence);

					p1.position.add(GRADIENTS[0]);
					p2.position.add(GRADIENTS[1]);
					p3.position.add(GRADIENTS[2]);
					p4.position.add(GRADIENTS[3]);
				}
				
				static final Vector3f P2P1 = new Vector3f();
				static final Vector3f P3P1 = new Vector3f();
				static final Vector3f P4P2 = new Vector3f();
				static final Vector3f P4P3 = new Vector3f();
				static final Vector3f EDGE = new Vector3f();
				static final Vector3f EDGE_NORM = new Vector3f();
				
				static final Vector3f CROSS1 = new Vector3f();
				static final Vector3f CROSS2 = new Vector3f();
				static final Vector3f CROSS3 = new Vector3f();
				
				public float getDihedralAngle() {
					p1.position.sub(p2.position, P2P1);
					p1.position.sub(p3.position, P3P1);
					p4.position.sub(p2.position, P4P2);
					p4.position.sub(p3.position, P4P3);
					p3.position.sub(p2.position, EDGE);

					P2P1.cross(P3P1, CROSS1);
					P4P3.cross(P4P2, CROSS2);
					CROSS1.normalize();
					CROSS2.normalize();
					EDGE.normalize(EDGE_NORM);


					float cos = CROSS1.dot(CROSS2);

					float sin = CROSS1.cross(CROSS2, CROSS3).dot(EDGE_NORM);;
					
					return Math.atan2(sin, cos);
				}
			}
			

			@OnlyIn(Dist.CLIENT)
            static
            class VolumeConstraint extends Constraint {
				final Particle[] particles;
				final float restVolume;
				
				static final float SUBDIVISION = 1.0F / 6.0F;
				static final int[][] VOLUME_ORDER = { {1, 3, 2}, {0, 2, 3}, {0, 3, 1}, {0, 1, 2} };
				static final Vector3f[] SHRINK_DIRECTIONS = { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
				
				// Storage vectors
				static final Vector3f P1_TO_P2 = new Vector3f();
				static final Vector3f P1_TO_P3 = new Vector3f();
				static final Vector3f P1_TO_P4 = new Vector3f();
				static final Vector3f TET_CROSS = new Vector3f();
				
				VolumeConstraint(Particle p1, Particle p2, Particle p3, Particle p4) {
					this.particles = new Particle[4];
					this.particles[0] = p1;
					this.particles[1] = p2;
					this.particles[2] = p3;
					this.particles[3] = p4;
					this.restVolume = this.getTetrahedralVolume();
				}

				@Override
				void solve(float alpha, int stepcount) {
					float weight = 0.0F;
					
					for (int i = 0; i < 4; i++) {
						Particle p1 = this.particles[VOLUME_ORDER[i][0]];
						Particle p2 = this.particles[VOLUME_ORDER[i][1]];
						Particle p3 = this.particles[VOLUME_ORDER[i][2]];

						p2.position.sub(p1.position, P1_TO_P2);
						p3.position.sub(p1.position, P1_TO_P3);
						P1_TO_P2.cross(P1_TO_P3, SHRINK_DIRECTIONS[i]);
						SHRINK_DIRECTIONS[i].mul(SUBDIVISION);

						
						weight += this.particles[i].influence * SHRINK_DIRECTIONS[i].lengthSquared();
					}
					
					if (weight < 1E-8) {
						return;
					}
					
					float constraint = this.restVolume - this.getTetrahedralVolume();
					float force = constraint / (weight + alpha);
					
					for (int i = 0; i < 4; i++) {
						SHRINK_DIRECTIONS[i].mul(force * this.particles[i].influence);
						particles[i].position.add(SHRINK_DIRECTIONS[i]);
					}
				}
				
				float getTetrahedralVolume() {
					particles[1].position.sub(particles[0].position, P1_TO_P2);
					particles[2].position.sub(particles[0].position, P1_TO_P3);
					particles[3].position.sub(particles[0].position, P1_TO_P4);
					P1_TO_P2.cross(P1_TO_P3, TET_CROSS);
					return TET_CROSS.dot(P1_TO_P4) / 6f;
					
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ClothOBBCollider extends OBBCollider {
		public ClothOBBCollider(double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
			super(vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
		}
		
		public AABB getOuterAABB(float particleRadius) {
			double maxX = -1000000.0D;
			double maxY = -1000000.0D;
			double maxZ = -1000000.0D;
			
			for (Vec3 rotated : this.rotatedVertices) {
				double xdistance = Math.abs(rotated.x);
				
				if (xdistance > maxX) {
					maxX = xdistance;
				}
				
				double ydistance = Math.abs(rotated.y);
				
				if (ydistance > maxY) {
					maxY = ydistance;
				}
				
				double zdistance = Math.abs(rotated.z);
				
				if (zdistance > maxZ) {
					maxZ = zdistance;
				}
			}
			
			maxX += particleRadius;
			maxY += particleRadius;
			maxZ += particleRadius;
			
			return new AABB(-maxX, -maxY, -maxZ, maxX, maxY, maxZ).move(this.worldCenter);
		}
		
		private boolean doesPointCollide(Vec3 point, float radius) {
			Vec3 toOpponent = point.subtract(this.worldCenter);
			
			for (Vec3 seperateAxis : this.rotatedNormals) {
				Vec3 maxProj = null;
				double maxDot = -1000000.0D;
				
				if (seperateAxis.dot(toOpponent) < 0.0D) {
					seperateAxis = seperateAxis.scale(-1.0D);
				}
				
				for (Vec3 vertexVector : this.rotatedVertices) {
					Vec3 toVertex = seperateAxis.dot(vertexVector) > 0.0D ? vertexVector : vertexVector.scale(-1.0D);
					double dot = seperateAxis.dot(toVertex);
					
					if (dot > maxDot || maxProj == null) {
						maxDot = dot;
						maxProj = toVertex;
					}
				}
				
				Vec3 opponentProjection = MathUtils.projectVector(toOpponent, seperateAxis);
				Vec3 vertexProjection = MathUtils.projectVector(maxProj, seperateAxis);
				
				if (opponentProjection.length() > vertexProjection.length() + radius) {
					return false;
				}
			}
			
			return true;
		}

		private static final Vector3f WORLD_CENTER = new Vector3f();
		private static final Vector3f TO_OPPONENT = new Vector3f();
		private static final Vector3f SEP_AXIS = new Vector3f();
		private static final Vector3f TO_VERTEX = new Vector3f();
		private static final Vector3f MAX_PROJ = new Vector3f();
		private static final Vector3f TO_OPPONENT_PROJECTION = new Vector3f();
		private static final Vector3f VERTEX_PROJECTION = new Vector3f();
		private static final Vector3f PROJECTION1 = new Vector3f();
		private static final Vector3f PROJECTION2 = new Vector3f();
		private static final Vector3f PROJECTION3 = new Vector3f();
		private static final Vector3f TO_PLANE1 = new Vector3f();
		private static final Vector3f TO_PLANE2 = new Vector3f();
		private static final Vector3f TO_PLANE3 = new Vector3f();
		
		private final Vector3f[] destinations = { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
		

		public void pushIfPointInside(Vector3f point, Vector3f root, float selfCollision, List<Vector3f> destnations, List<ClothSimulator.ClothOBBCollider> others) {
			WORLD_CENTER.set(this.worldCenter.toVector3f());
			point.sub(WORLD_CENTER, TO_OPPONENT);

			int order = 0;
			
			for (Vec3 seperateAxis : this.rotatedNormals) {
				SEP_AXIS.set(seperateAxis.toVector3f());
				float maxDot = -10000.0F;
				
				if (SEP_AXIS.dot(TO_OPPONENT) < 0.0D) {
					SEP_AXIS.mul(-1.0F);
				}
				
				for (Vec3 vertexVector : this.rotatedVertices) {
					TO_VERTEX.set(vertexVector.toVector3f());
					
					if (SEP_AXIS.dot(TO_VERTEX) < 0.0D) {
						TO_VERTEX.mul(-1.0F);
					}
					
					float dot = SEP_AXIS.dot(TO_VERTEX);
					
					if (dot > maxDot) {
						maxDot = dot;
						MAX_PROJ.set(TO_VERTEX);
					}
				}
				
				VectorUtils.project(TO_OPPONENT, SEP_AXIS, TO_OPPONENT_PROJECTION);
				VectorUtils.project(MAX_PROJ, SEP_AXIS, VERTEX_PROJECTION);

				if (TO_OPPONENT_PROJECTION.length() > VERTEX_PROJECTION.length() + selfCollision) {
					return;
				} else {
					float lerp = VERTEX_PROJECTION.length() + selfCollision / VERTEX_PROJECTION.length();
					switch (order) {
					case 0 -> {
						PROJECTION1.set(TO_OPPONENT_PROJECTION);
						VERTEX_PROJECTION.mul(TO_PLANE1).mul(lerp);
					}
					case 1 -> {
						PROJECTION2.set(TO_OPPONENT_PROJECTION);
						VERTEX_PROJECTION.mul(TO_PLANE2).mul(lerp);
					}
					case 2 -> {
						PROJECTION3.set(TO_OPPONENT_PROJECTION);
						VERTEX_PROJECTION.mul(TO_PLANE3).mul(lerp);
					}
					}
				}
				
				order++;
			}
			
			this.destinations[0].set(0.0F, 0.0F, 0.0F).add(PROJECTION1).add(PROJECTION2).add(TO_PLANE3).add(this.worldCenter.toVector3f());
			this.destinations[1].set(0.0F, 0.0F, 0.0F).add(PROJECTION2).add(PROJECTION3).add(TO_PLANE1).add(this.worldCenter.toVector3f());
			this.destinations[2].set(0.0F, 0.0F, 0.0F).add(PROJECTION3).add(PROJECTION1).add(TO_PLANE2).add(this.worldCenter.toVector3f());
			this.destinations[3].set(0.0F, 0.0F, 0.0F).add(PROJECTION1).add(PROJECTION2).sub(TO_PLANE3).add(this.worldCenter.toVector3f());
			this.destinations[4].set(0.0F, 0.0F, 0.0F).add(PROJECTION2).add(PROJECTION3).sub(TO_PLANE1).add(this.worldCenter.toVector3f());
			this.destinations[5].set(0.0F, 0.0F, 0.0F).add(PROJECTION3).add(PROJECTION1).sub(TO_PLANE2).add(this.worldCenter.toVector3f());
			
			Loop1:
			for (Vector3f dest : this.destinations) {
				for (ClothOBBCollider other : others) {
					if (other == this) {
						continue;
					}
					
					if (other.doesPointCollide(new Vec3(dest), selfCollision * 0.5F)) {
						VectorUtils.invalidate(dest);
						continue Loop1;
					}
				}
			}
			
			for (Vector3f dest : this.destinations) {
				if (VectorUtils.validate(dest)) {
					destnations.add(dest);
				}
			}
		}

    }
}
