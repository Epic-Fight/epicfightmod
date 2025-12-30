package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;

public class ArrowPatch<T extends AbstractArrow> extends ProjectilePatch<T> {
	public ArrowPatch(T original) {
		super(original);
	}
	
	@Override
	protected void setMaxStrikes(T projectileEntity, int maxStrikes) {
		projectileEntity.setPierceLevel((byte)(maxStrikes - 1));
	}
	
	@Override
	public EpicFightDamageSource createEpicFightDamageSource() {
		return EpicFightDamageSources.arrow(this.original, this.original.getOwner())
				.setStunType(StunType.SHORT)
				.addRuntimeTag(DamageTypeTags.IS_PROJECTILE)
				.setBaseArmorNegation(this.armorNegation)
				.setBaseImpact(this.impact)
				.setInitialPosition(this.initialFirePosition);
	}
}