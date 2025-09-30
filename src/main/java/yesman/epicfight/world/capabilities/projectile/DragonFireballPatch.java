package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public class DragonFireballPatch extends ProjectilePatch<DragonFireball> {
	@Override
	public void onJoinWorld(DragonFireball projectileEntity, EntityJoinLevelEvent event) {
		super.onJoinWorld(projectileEntity, event);
		this.impact = 1.0F;
		projectileEntity.xPower *= 2.0D;
		projectileEntity.yPower *= 2.0D;
		projectileEntity.zPower *= 2.0D;
	}
	
	@Override
	protected void setMaxStrikes(DragonFireball projectileEntity, int maxStrikes) {
	}
	
	@Override
	public boolean onProjectileImpact(ProjectileImpactEvent event) {
		if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
			Entity entity = entityHitResult.getEntity();
			
			if (!entity.level().isClientSide() && !entity.is(event.getProjectile().getOwner())) {
				entity.hurt(entity.level().damageSources().indirectMagic(event.getProjectile(), event.getProjectile().getOwner()), 8.0F);
			}
		}
		
		return false;
	}
	
	@Override
	public EpicFightDamageSource createEpicFightDamageSource() {
		return null;
	}
}