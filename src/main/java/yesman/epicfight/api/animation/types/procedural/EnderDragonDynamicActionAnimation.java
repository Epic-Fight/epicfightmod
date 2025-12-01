package yesman.epicfight.api.animation.types.procedural;

import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulatable;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator.BakedInverseKinematicsDefinition;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.Objects;

public class EnderDragonDynamicActionAnimation extends ActionAnimation {
	public EnderDragonDynamicActionAnimation(float transitionTime, AnimationAccessor<? extends EnderDragonDynamicActionAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		super(transitionTime, accessor, armature);
	}
	
	@Override
	public void putOnPlayer(AnimationPlayer animationPlayer, LivingEntityPatch<?> entitypatch) {
		super.putOnPlayer(animationPlayer, entitypatch);
		
		if (entitypatch instanceof InverseKinematicsSimulatable ikSimulatable) {
			Vec3 entitypos = ikSimulatable.toEntity().position();
			Matrix4f toWorld = new Matrix4f().setTranslation(entitypos.toVector3f()).mul(ikSimulatable.getModelMatrix(1.0f), new Matrix4f());
			//ikSimulatable.resetTipAnimations();
			TransformSheet movementAnimation = entitypatch.getAnimator().getVariables().getOrDefaultSharedVariable(ACTION_ANIMATION_COORD);
			
			this.getProperty(StaticAnimationProperty.BAKED_IK_DEFINITION).ifPresent((ikDefinitions) -> {
				for (BakedInverseKinematicsDefinition bakedIKInfo : ikDefinitions) {
					TransformSheet tipAnim = this.clipAnimation(bakedIKInfo.terminalBoneTransform(), bakedIKInfo);
					Keyframe[] keyframes = tipAnim.getKeyframes();
					Vector3f startpos = movementAnimation.getInterpolatedTranslation(0.0F);
					
					for (int i = 0; i < keyframes.length; i++) {
						Keyframe kf = keyframes[i];
						Vector3f dynamicpos = movementAnimation.getInterpolatedTranslation(kf.time()).sub(startpos);
						Matrix4fUtils.transform3v(new Matrix4f().rotate(Math.toRadians( -90.0F), 1, 0, 0), dynamicpos, dynamicpos).mul(-1.0F, 1.0F, -1.0F);
						Vector3f finalTargetpos;
						
						if (!bakedIKInfo.clipAnimation() || bakedIKInfo.touchingGround()[i]) {
							Vector3f clipStart = kf.transform().translation().mul(-1.0F, 1.0F, -1.0F, new Vector3f()).add(dynamicpos).add(0.0F, 2.5F, 0.0F);
							finalTargetpos = this.getRayCastedTipPosition(ikSimulatable, clipStart, toWorld, 2.5F, bakedIKInfo.rayLeastHeight());
						} else {
							Vector3f start = kf.transform().translation().mul(-1.0F, 1.0F, -1.0F, new Vector3f()).add(dynamicpos);
							finalTargetpos = Matrix4fUtils.transform3v(toWorld, start, new Vector3f());
						}
						
						kf.transform().translation().set(finalTargetpos);
					}
					
					ikSimulatable.getIKSimulator().runUntil(
						  bakedIKInfo.endJoint()
						, this
						, InverseKinematicsSimulator.InverseKinematicsBuilder.create(
							  keyframes[0].transform().translation()
							, tipAnim
							, bakedIKInfo)
						, () -> Objects.requireNonNull(entitypatch.getAnimator().getPlayer(this.getAccessor())).isPresent()
					);
				}
			});
		}
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		
		if (entitypatch instanceof InverseKinematicsSimulatable ikSimulatable) {
			float elapsedTime = Objects.requireNonNull(entitypatch.getAnimator().getPlayerFor(this.getAccessor())).getElapsedTime();
			
			this.getProperty(StaticAnimationProperty.BAKED_IK_DEFINITION).ifPresent((ikDefinitions) -> {
				for (BakedInverseKinematicsDefinition bakedIKInfo : ikDefinitions) {
					if (ikSimulatable.getIKSimulator().isRunning(bakedIKInfo.endJoint()) && bakedIKInfo.clipAnimation()) {
						Keyframe[] keyframes = this.getTransfroms().get(bakedIKInfo.endJoint().getName()).getKeyframes();
						float startTime = keyframes[bakedIKInfo.startFrame()].time();
						float endTime = keyframes[bakedIKInfo.endFrame() - 1].time();
						
						if (startTime <= elapsedTime && elapsedTime < endTime) {
							InverseKinematicsSimulator.InverseKinematicsObject tipAnim = ikSimulatable.getIKSimulator().getRunningObject(bakedIKInfo.endJoint()).get();
							
							if (!tipAnim.isOnWorking()) {
								this.startSimple(tipAnim);
							}
						}
					}
				}
			});
		}
	}
}