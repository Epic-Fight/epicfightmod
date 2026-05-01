package yesman.epicfight.api.animation.types.grappling;

import yesman.epicfight.api.animation.types.LongHitAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;

public class GrapplingHitAnimation extends LongHitAnimation {
	public GrapplingHitAnimation(float convertTime, String path, AssetAccessor<? extends Armature> armature) {
		super(convertTime, path, armature);
	}
}