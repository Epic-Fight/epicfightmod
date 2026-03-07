package yesman.epicfight.world.capabilities.item;

import java.util.Map;

import com.google.common.collect.Maps;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class RangedWeaponCapability extends WeaponCapability {
    protected ZoomInType zoomInType;
	
	protected RangedWeaponCapability(WeaponCapability.Builder builder) {
		super(builder);
		
		RangedWeaponCapability.Builder rangedBuilder = (RangedWeaponCapability.Builder)builder;
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
        private ZoomInType zoomInType = ZoomInType.USE_TICK;
		
		protected Builder() {
			this.category = WeaponCategories.RANGED;
			this.constructor = RangedWeaponCapability::new;
        }

        public Builder zoomInType(ZoomInType zoomInType) {
			this.zoomInType = zoomInType;
			return this;
		}
	}
}