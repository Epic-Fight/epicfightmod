package yesman.epicfight.world.capabilities.item;

import java.util.Map;

import com.google.common.collect.Maps;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RangedWeaponCapability extends WeaponCapability {
	protected Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> rangeAnimationModifiers;
	protected ZoomInType zoomInType;
	
	protected RangedWeaponCapability(WeaponCapability.Builder builder) {
		super(builder);
		
		RangedWeaponCapability.Builder rangedBuilder = (RangedWeaponCapability.Builder)builder;
		this.rangeAnimationModifiers = rangedBuilder.rangeAnimationModifiers;
		this.zoomInType = rangedBuilder.zoomInType;
	}

	@Override
	public boolean availableOnHorse(LivingEntityPatch<?> entityPatch) {
		return true;
	}
	
	@Override
	public boolean canBePlacedOffhand() {
		return false;
	}
	
	@Override
	public ZoomInType getZoomInType() {
		return this.zoomInType;
	}
	
	public static RangedWeaponCapability.Builder builder() {
		return new RangedWeaponCapability.Builder();
	}
	
	public static class Builder extends WeaponCapability.Builder {
		private final Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> rangeAnimationModifiers;
		private ZoomInType zoomInType = ZoomInType.USE_TICK;
		
		protected Builder() {
			this.category = WeaponCategories.RANGED;
			this.constructor = RangedWeaponCapability::new;
			this.rangeAnimationModifiers = Maps.newHashMap();
		}

        public Builder zoomInType(ZoomInType zoomInType) {
			this.zoomInType = zoomInType;
			return this;
		}
	}
}