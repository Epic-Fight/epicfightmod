package yesman.epicfight.api.animation.types;

import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.online.cosmetics.Emote;

/// Animations for [Emote]. All emote objects must target an animation instance that inherits this animation class
public class EmoteAnimation extends StaticAnimation {
    public EmoteAnimation(AnimationManager.AnimationAccessor<? extends EmoteAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(false, accessor, armature);
    }

    public EmoteAnimation(float transitionTime, AnimationManager.AnimationAccessor<? extends EmoteAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, false, accessor, armature);
    }

    public EmoteAnimation(boolean loops, AnimationManager.AnimationAccessor<? extends EmoteAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(loops, accessor, armature);
    }

    public EmoteAnimation(float transitionTime, boolean loops, AnimationManager.AnimationAccessor<? extends EmoteAnimation> accessor, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, loops, accessor, armature);
    }

    /* Resourcepack animations */
    public EmoteAnimation(float transitionTime, boolean loops, String path, AssetAccessor<? extends Armature> armature) {
        super(transitionTime, loops, path, armature);
    }
}
