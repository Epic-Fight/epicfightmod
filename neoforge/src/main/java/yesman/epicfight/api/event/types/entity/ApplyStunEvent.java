package yesman.epicfight.api.event.types.entity;

import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

/// Use [StunnedEvent] to completely disable stun
public class ApplyStunEvent extends LivingEntityPatchEvent {
	private final StunType stunType;
    private AssetAccessor<? extends StaticAnimation> stunAnimation;
    private float stunTime;

	public ApplyStunEvent(LivingEntityPatch<?> entityPatch, StunType stunType, AssetAccessor<? extends StaticAnimation> stunAnimation, float stunTime) {
        super(entityPatch);

		this.stunType = stunType;
        this.stunAnimation = stunAnimation;
	}

	public final StunType getStunType() {
		return this.stunType;
	}

    public final AssetAccessor<? extends StaticAnimation> getStunAnimation() {
        return stunAnimation;
    }

    public void setSTunAnimation(AssetAccessor<? extends StaticAnimation> stunAnimation) {
        this.stunAnimation = stunAnimation;
    }

    public float getStunTime() {
        return stunTime;
    }

    public void setStunTime(final float stunTime) {
        this.stunTime = stunTime;
    }
}
