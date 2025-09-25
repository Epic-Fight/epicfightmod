package yesman.epicfight.api.animation;

import yesman.epicfight.api.utils.ExtensibleEnum;
import yesman.epicfight.api.utils.ExtensibleEnumManager;

public interface LivingMotion extends ExtensibleEnum {
	ExtensibleEnumManager<LivingMotion> ENUM_MANAGER = new ExtensibleEnumManager<> ("living_motion");
	
	default boolean isSame(LivingMotion livingMotion) {
		if (this == LivingMotions.IDLE && livingMotion == LivingMotions.INACTION) {
			return true;
		} else if (this == LivingMotions.INACTION && livingMotion == LivingMotions.IDLE) {
			return true;
		}
		
		return this == livingMotion;
	}
}