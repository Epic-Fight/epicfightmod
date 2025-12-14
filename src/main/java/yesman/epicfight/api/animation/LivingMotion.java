package yesman.epicfight.api.animation;

import yesman.epicfight.api.utils.ExtendableEnum;
import yesman.epicfight.api.utils.ExtendableEnumManager;

public interface LivingMotion extends ExtendableEnum {
	ExtendableEnumManager<LivingMotion> ENUM_MANAGER = new ExtendableEnumManager<> ("living_motion");
	
	default boolean isSame(LivingMotion livingMotion) {
		if (this == LivingMotions.IDLE && livingMotion == LivingMotions.INACTION) {
			return true;
		} else if (this == LivingMotions.INACTION && livingMotion == LivingMotions.IDLE) {
			return true;
		}
		
		return this == livingMotion;
	}
}