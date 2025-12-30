package yesman.epicfight.api.collider;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.AABB;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackAnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class MultiOBBCollider extends MultiCollider<OBBCollider> {
	public MultiOBBCollider(int arrayLength, double vertexX, double vertexY, double vertexZ, double centerX, double centerY, double centerZ) {
		super(arrayLength, centerX, centerY, centerZ, null);
		
		AABB aabb = OBBCollider.getInitialAABB(vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
		OBBCollider colliderForAll = new OBBCollider(aabb, vertexX, vertexY, vertexZ, centerX, centerY, centerZ);
		
		for (int i = 0; i < arrayLength; i++) {
			this.colliders.add(colliderForAll);
		}
	}
	
	public MultiOBBCollider(OBBCollider... colliders) {
		super(colliders);
	}
	
	@Override @ClientOnly
	public void draw(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, AttackAnimation animation, Joint joint, float prevElapsedTime, float elapsedTime, float partialTicks, float attackSpeed) {
		int colliderCount = Math.max(Math.round((this.numberOfColliders + animation.getProperty(AttackAnimationProperty.EXTRA_COLLIDERS).orElse(0)) * attackSpeed), this.numberOfColliders);
		float partialScale = 1.0F / (colliderCount - 1);
		float interpolation = 0.0F;
		Armature armature = entitypatch.getArmature();
		EntityState state = animation.getState(entitypatch, elapsedTime);
		EntityState prevState = animation.getState(entitypatch, prevElapsedTime);
		boolean attacking = prevState.attacking() || state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2);
		List<OBBCollider> colliders = Lists.newArrayList();
		float index = 0.0F;
		float interIndex = Math.min((float)(this.numberOfColliders - 1) / (colliderCount - 1), 1.0F);
		
		for (int i = 0; i < colliderCount; i++) {
			colliders.add(this.colliders.get((int)index).deepCopy());
			index += interIndex;
		}
		
		for (OBBCollider obbCollider : colliders) {
			float pt1 = prevElapsedTime + (elapsedTime - prevElapsedTime) * partialTicks;
			float pt2 = prevElapsedTime + (elapsedTime - prevElapsedTime) * interpolation;
			TransformSheet coordTransform = animation.getCoord();
			Vec3f p1 = coordTransform.getInterpolatedTranslation(pt1);
			Vec3f p2 = coordTransform.getInterpolatedTranslation(pt2);
			
			poseStack.pushPose();
			poseStack.translate(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z);
			
			Pose pose;
			
			if (armature.rootJoint.getName().equals(joint.getName())) {
				pose = new Pose();
				pose.putJointData("Root", JointTransform.empty());
				animation.modifyPose(animation, pose, entitypatch, elapsedTime, 1.0F);
			} else {
				pose = animation.getPoseByTime(entitypatch, pt2, 1.0F);
			}
			
			obbCollider.drawInternal(poseStack, buffer.getBuffer(this.getRenderType()), armature, joint, pose, pose, 1.0F, attacking ? 0xFFFF0000 : -1);
			poseStack.popPose();
			
			interpolation += partialScale;
		}
	}
	
	@Override
	public CompoundTag serialize(CompoundTag resultTag) {
		if (resultTag == null) {
			resultTag = new CompoundTag();
		}
		
		resultTag.putInt("number", this.numberOfColliders);
		
		ListTag center = new ListTag();
		
		center.add(DoubleTag.valueOf(this.modelCenter.x));
		center.add(DoubleTag.valueOf(this.modelCenter.y));
		center.add(DoubleTag.valueOf(this.modelCenter.z));
		
		resultTag.put("center", center);
		
		ListTag size = new ListTag();
		
		size.add(DoubleTag.valueOf(this.colliders.get(0).modelVertices[1].x));
		size.add(DoubleTag.valueOf(this.colliders.get(0).modelVertices[1].y));
		size.add(DoubleTag.valueOf(this.colliders.get(0).modelVertices[1].z));
		
		resultTag.put("size", size);
		
		return resultTag;
	}
	
	@Override @ClientOnly
	public RenderType getRenderType() {
		return this.colliders.get(0).getRenderType();
	}
}