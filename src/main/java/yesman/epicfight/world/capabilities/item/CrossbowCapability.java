package yesman.epicfight.world.capabilities.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CrossbowCapability extends RangedWeaponCapability {
	protected CrossbowCapability(CapabilityItem.Builder builder) {
		super(builder);
	}
	
	@Override
	public LivingMotion getLivingMotion(LivingEntityPatch<?> entitypatch, InteractionHand hand) {
		return entitypatch.getEntityState().canUseItem() &&
				entitypatch.getOriginal().getMainHandItem().getItem() instanceof ProjectileWeaponItem &&
				CrossbowItem.isCharged(entitypatch.getOriginal().getMainHandItem())
				? LivingMotions.AIM : null;
	}
}