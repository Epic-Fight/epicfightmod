package yesman.epicfight.world.capabilities.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class BowCapability extends RangedWeaponCapability {
	protected BowCapability(CapabilityItem.Builder builder) {
		super(builder);
	}
	
	@Override
	public LivingMotion getLivingMotion(LivingEntityPatch<?> entityPatch, InteractionHand hand) {
		return entityPatch.getOriginal().isUsingItem() && entityPatch.getOriginal().getUseItem().getUseAnimation() == UseAnim.BOW ? LivingMotions.AIM : null;
	}
}