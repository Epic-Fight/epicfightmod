package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.mob.WitherSkeletonPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.WitherSkeletonMinion;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class WitherSkullPatch extends ProjectilePatch<WitherSkull> {
	@Override
	public void onJoinWorld(WitherSkull projectileEntity, EntityJoinLevelEvent event) {
		super.onJoinWorld(projectileEntity, event);
		
		this.impact = 1.0F;
	}
	
	@Override
	protected void setMaxStrikes(WitherSkull projectileEntity, int maxStrikes) {
	}
	
	@Override
	public boolean onProjectileImpact(ProjectileImpactEvent event) {
		if (event.getProjectile().level().isClientSide())
		{
			return false;
		}
		if (!(event.getRayTraceResult() instanceof EntityHitResult entityHitResult)) {
			if (event.getProjectile().level() instanceof ServerLevel serverLevel && Math.random() < 0.2D) {
				Vec3 location = event.getRayTraceResult().getLocation();
				BlockPos blockpos = new BlockPos.MutableBlockPos(location.x, location.y, location.z);
				Projectile projectile = event.getProjectile();
				EntityType<?> entityType = EpicFightEntities.WITHER_SKELETON_MINION.get();
				
				if (
					NaturalSpawner.isSpawnPositionOk(SpawnPlacements.getPlacementType(entityType), serverLevel, blockpos, entityType) &&
					SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.REINFORCEMENT, blockpos, serverLevel.random) &&
					!EpicFightGameRules.NO_MOBS_IN_BOSSFIGHT.getRuleValue(serverLevel)
				) {
					WitherBoss summoner = (projectile.getOwner() instanceof WitherBoss) ? ((WitherBoss)projectile.getOwner()) : null;
					WitherSkeletonMinion witherskeletonminion = new WitherSkeletonMinion(serverLevel, summoner, projectile.getX(), projectile.getY() + 0.1D, projectile.getZ());
					witherskeletonminion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockpos), MobSpawnType.REINFORCEMENT, null, null);
					witherskeletonminion.setYRot(projectile.getYRot() - 180.0F);
					serverLevel.addFreshEntity(witherskeletonminion);
					
					EpicFightCapabilities.<WitherSkeletonMinion, WitherSkeletonPatch<WitherSkeletonMinion>>getParameterizedEntityPatch(witherskeletonminion, WitherSkeletonMinion.class, WitherSkeletonPatch.class)
						.ifPresent(witherskeletonpatch -> witherskeletonpatch.playAnimationInstantly(Animations.WITHER_SKELETON_SPECIAL_SPAWN));
				}
			}
		} else {
			return entityHitResult.getEntity() instanceof WitherSkeletonMinion;
		}
		
		return false;
	}
	
	@Override
	public EpicFightDamageSource createEpicFightDamageSource() {
		return EpicFightDamageSources.witherSkull(this.original, this.original.getOwner())
				.setStunType(StunType.SHORT)
				.addRuntimeTag(DamageTypeTags.IS_PROJECTILE)
				.setBaseArmorNegation(this.armorNegation)
				.setBaseImpact(this.impact)
				.setInitialPosition(this.initialFirePosition);
	}
}