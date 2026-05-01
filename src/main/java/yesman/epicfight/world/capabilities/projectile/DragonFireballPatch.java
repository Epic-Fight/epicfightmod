package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public class DragonFireballPatch extends ProjectilePatch<DragonFireball> {
	public DragonFireballPatch(DragonFireball original) {
		super(original);
	}

    @Override
    public void onJoinWorld(DragonFireball entity, Level level, boolean worldgenSpawn) {
        super.onJoinWorld(entity, level, worldgenSpawn);

		this.impact = 1.0F;
        entity.accelerationPower *= 2.0D;
	}
	
	@Override
	protected void setMaxStrikes(DragonFireball projectileEntity, int maxStrikes) {
	}
	
	@Override
	public boolean onProjectileImpact(HitResult hitResult) {
		if (hitResult instanceof EntityHitResult entityHitResult) {
			Entity entity = entityHitResult.getEntity();
			
			if (!entity.level().isClientSide() && !entity.is(this.original.getOwner())) {
				entity.hurt(entity.level().damageSources().indirectMagic(this.original, this.original.getOwner()), 8.0F);
			}
		}
		
		return false;
	}
	
	@Override
	public EpicFightDamageSource createEpicFightDamageSource() {
		return null;
	}
}