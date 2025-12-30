package yesman.epicfight.api.animation.types.procedural;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class EnderDragonDeathAnimation extends LongHitAnimation {
	public EnderDragonDeathAnimation(float convertTime, AnimationAccessor<? extends EnderDragonDeathAnimation> accessor, AssetAccessor<? extends Armature> armature) {
		super(convertTime, accessor, armature);
	}
	
	@Override
	public void loadAnimation() {
		this.animationClip = AnimationManager.getInstance().loadAnimationClip(this, JsonAssetLoader::loadAllJointsClipForAnimation);
	}
	
	@Override
	public void modifyPose(DynamicAnimation animation, Pose pose, LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		
	}
}