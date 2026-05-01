package yesman.epicfight.api.collider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Collider {
	protected final Vec3 modelCenter;
	protected final AABB outerAABB;
	protected Vec3 worldCenter;
	
	public Collider(Vec3 center, @Nullable AABB outerAABB) {
		this.modelCenter = center;
		this.outerAABB = outerAABB;
		this.worldCenter = new Vec3(0.0D, 0.0D, 0.0D);
	}
	
	protected void transform(OpenMatrix4f mat) {
		this.worldCenter = OpenMatrix4f.transform(mat, this.modelCenter);
	}
	
	public List<Entity> updateAndSelectCollideEntity(LivingEntityPatch<?> entitypatch, AttackAnimation attackAnimation, float prevElapsedTime, float elapsedTime, Joint joint, float attackSpeed) {
		OpenMatrix4f transformMatrix;
		Armature armature = entitypatch.getArmature();
		
		if (armature.rootJoint.equals(joint)) {
			Pose rootPose = new Pose();
			rootPose.putJointData("Root", JointTransform.empty());
			attackAnimation.modifyPose(attackAnimation, rootPose, entitypatch, elapsedTime, 1.0F);
			transformMatrix = rootPose.orElseEmpty("Root").getAnimationBoundMatrix(armature.rootJoint, new OpenMatrix4f()).removeTranslation();
		} else {
			transformMatrix = armature.getBoundTransformFor(attackAnimation.getPoseByTime(entitypatch, elapsedTime, 1.0F), joint);
		}
		
		OpenMatrix4f toWorldCoord = OpenMatrix4f.createTranslation(-(float)entitypatch.getOriginal().getX(), (float)entitypatch.getOriginal().getY(), -(float)entitypatch.getOriginal().getZ());
		transformMatrix.mulFront(toWorldCoord.mulBack(entitypatch.getModelMatrix(1.0F)));
		this.transform(transformMatrix);
		
		return this.getCollideEntities(entitypatch.getOriginal());
	}
	
	public List<Entity> getCollideEntities(Entity entity) {
		List<Entity> list = entity.level().getEntities(entity, this.getHitboxAABB(), (e) -> {
			if (e instanceof PartEntity<?> partEntity) {
				if (partEntity.getParent().is(entity)) {
					return false;
				}
			}
			
			if (e.isSpectator()) {
				return false;
			}
			
			return this.isCollide(e);
		});
		
		return list;
	}
	
	/** Display on debug mode **/
    @ClientOnly
	public abstract void drawInternal(PoseStack poseStack, VertexConsumer vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int colliderColor);
	
	/** Display on debug mode **/
    @ClientOnly
	public void draw(PoseStack poseStack, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, AttackAnimation animation, Joint joint, float prevElapsedTime, float elapsedTime, float partialTicks, float attackSpeed) {
		Armature armature = entitypatch.getArmature();
		EntityState state = animation.getState(entitypatch, elapsedTime);
		EntityState prevState = animation.getState(entitypatch, prevElapsedTime);
		boolean attacking = prevState.attacking() || state.attacking() || (prevState.getLevel() < 2 && state.getLevel() > 2);
		Pose prevPose;
		Pose currentPose;
		
		if (joint.getName().equals(armature.rootJoint.getName())) {
			prevPose = new Pose();
			currentPose = new Pose();
			prevPose.putJointData("Root", JointTransform.empty());
			currentPose.putJointData("Root", JointTransform.empty());
			animation.modifyPose(animation, prevPose, entitypatch, prevElapsedTime, 0.0F);
			animation.modifyPose(animation, currentPose, entitypatch, elapsedTime, 1.0F);
		} else {
			prevPose = animation.getPoseByTime(entitypatch, prevElapsedTime, 0.0F);
			currentPose = animation.getPoseByTime(entitypatch, elapsedTime, 1.0F);
		}
		
		this.drawInternal(poseStack, buffer.getBuffer(this.getRenderType()), armature, joint, prevPose, currentPose, partialTicks, attacking ? 0xFFFF0000 : -1);
	}
	
	public abstract Collider deepCopy();
	
	public abstract boolean isCollide(Entity opponent);

    @ClientOnly
	public abstract RenderType getRenderType();
	
	protected AABB getHitboxAABB() {
		return this.outerAABB.move(-this.worldCenter.x, this.worldCenter.y, -this.worldCenter.z);
	}
	
	public CompoundTag serialize(CompoundTag resultTag) {
		return resultTag;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " center: " + this.modelCenter;
	}
}
