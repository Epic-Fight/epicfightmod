package yesman.epicfight.world.capabilities.item;

import java.util.Map;

import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class MapCapability extends CapabilityItem {
	public MapCapability(CapabilityItem.Builder<?> builder) {
		super(builder);
	}
	
	@Override
	public Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifier(LivingEntityPatch<?> playerpatch, InteractionHand hand) {
		Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> livingModifier = super.getLivingMotionModifier(playerpatch, hand);
		AnimationAccessor<? extends StaticAnimation> holdAnimation;
		AnimationAccessor<? extends StaticAnimation> holdMoveAnimation;
		
		if (hand == InteractionHand.MAIN_HAND) {
			if (playerpatch.getOriginal().getOffhandItem().isEmpty()) {
				holdAnimation = Animations.BIPED_HOLD_MAP_TWOHAND;
				holdMoveAnimation = Animations.BIPED_HOLD_MAP_TWOHAND_MOVE;
			} else {
				holdAnimation = Animations.BIPED_HOLD_MAP_MAINHAND;
				holdMoveAnimation = Animations.BIPED_HOLD_MAP_MAINHAND_MOVE;
			}
		} else {
			holdAnimation = Animations.BIPED_HOLD_MAP_OFFHAND;
			holdMoveAnimation = Animations.BIPED_HOLD_MAP_OFFHAND_MOVE;
		}
		
		livingModifier.put(LivingMotions.IDLE, holdAnimation);
		livingModifier.put(LivingMotions.KNEEL, holdAnimation);
		livingModifier.put(LivingMotions.WALK, holdMoveAnimation);
		livingModifier.put(LivingMotions.CHASE, holdMoveAnimation);
		livingModifier.put(LivingMotions.RUN, holdMoveAnimation);
		livingModifier.put(LivingMotions.SNEAK, holdMoveAnimation);
		livingModifier.put(LivingMotions.SWIM, holdMoveAnimation);
		livingModifier.put(LivingMotions.FLOAT, holdMoveAnimation);
		livingModifier.put(LivingMotions.SIT, holdMoveAnimation);
		livingModifier.put(LivingMotions.MOUNT, holdMoveAnimation);
		
		return livingModifier;
	}
	
	public static CapabilityItem.Builder<?> builder() {
		return CapabilityItem.builder().constructor(MapCapability::new);
	}
}