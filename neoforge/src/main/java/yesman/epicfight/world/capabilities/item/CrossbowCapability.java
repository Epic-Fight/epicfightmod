package yesman.epicfight.world.capabilities.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CrossbowCapability extends RangedWeaponCapability {
	public CrossbowCapability(RangedWeaponCapability.Builder builder) {
		super(builder);
	}
	
	@Override
	public LivingMotion getLivingMotion(LivingEntityPatch<?> entityPatch, InteractionHand interactionHand) {
		return entityPatch.getEntityState().canUseItem() &&
				entityPatch.getOriginal().getMainHandItem().getItem() instanceof ProjectileWeaponItem &&
				CrossbowItem.isCharged(entityPatch.getOriginal().getMainHandItem())
				? LivingMotions.AIM : null;
	}
}