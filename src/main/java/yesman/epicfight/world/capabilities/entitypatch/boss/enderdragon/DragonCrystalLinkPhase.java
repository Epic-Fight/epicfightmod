package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public class DragonCrystalLinkPhase extends PatchedDragonPhase {
	public static final float STUN_SHIELD_AMOUNT = 20.0F;
	public static final int CHARGING_TICK = 158;
	private int absorbCount;
	private EndCrystal absorbingCrystal;
	
	public DragonCrystalLinkPhase(EnderDragon enderdragon) {
		super(enderdragon);
	}
	
	@Override
	public void begin() {
		BlockPos blockpos = this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(new BlockPos(0, 0, 0)));
		List<EndCrystal> list = this.dragon.level().getEntitiesOfClass(EndCrystal.class, new AABB(blockpos).inflate(500.0D));
		EndCrystal nearestCrystal = null;
		double d0 = Double.MAX_VALUE;
		
		for (EndCrystal endcrystal : list) {
			double d1 = endcrystal.distanceToSqr(this.dragon);
			
			if (d1 < d0) {
				d0 = d1;
				nearestCrystal = endcrystal;
			}
		}
		
		this.absorbingCrystal = nearestCrystal;
		
		if (this.absorbingCrystal != null) {
			this.dragonpatch.getAnimator().playAnimation(Animations.DRAGON_CRYSTAL_LINK, 0.0F);
			this.dragon.level().playLocalSound(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), EpicFightSounds.ENDER_DRAGON_CRYSTAL_LINK.get(), this.dragon.getSoundSource(), 10.0F, 1.0F, false);
			this.absorbingCrystal.setInvulnerable(true);
			this.absorbCount = CHARGING_TICK;
			
			if (this.dragonpatch.isLogicalClient()) {
				double x = -45.0D;
				double z = 0.0D;
				Vec3 correction = this.dragon.getLookAngle().multiply(2.0D, 0.0D, 2.0D).subtract(0.0D, 2.0D, 0.0D);
				Vec3 spawnPosition = this.dragon.position().subtract(correction);
				
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 2; j++) {
						this.dragon.level().addAlwaysVisibleParticle(EpicFightParticles.FORCE_FIELD.get(), spawnPosition.x, spawnPosition.y, spawnPosition.z, x + 90.0D * i, Double.longBitsToDouble(this.dragon.getId()), z + 90.0D * j);
					}
				}
			} else {
				if (!this.dragonpatch.isLogicalClient()) {
					int shieldCorrection = this.getPlayersNearbyWithin(100.0D).size() - 1;
					float stunShield = STUN_SHIELD_AMOUNT + 15.0F * shieldCorrection;
					
					this.dragonpatch.setMaxStunShield(stunShield);
					this.dragonpatch.setStunShield(stunShield);
				}
			}
		} else {
			this.dragon.getPhaseManager().setPhase(PatchedPhases.GROUND_BATTLE);
		}
	}
	
	@Override
	public void end() {
		if (!this.dragonpatch.isLogicalClient() && this.absorbingCrystal != null) {
			BlockPos blockpos = this.absorbingCrystal.blockPosition();
			this.absorbingCrystal.setInvulnerable(false);
			this.dragon.level().explode(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 6.0F, Level.ExplosionInteraction.BLOCK);
		}
		
		this.dragon.nearestCrystal = null;
		this.absorbingCrystal = null;
	}
	
	@Override
	public float onHurt(DamageSource damagesource, float amount) {
		if (damagesource instanceof EpicFightDamageSource epicfightDamagesource) {
			this.dragonpatch.setStunShield(this.dragonpatch.getStunShield() - epicfightDamagesource.calculateImpact());
		}
		
		return amount;
	}
	
	@Override
	public void doClientTick() {
		super.doClientTick();
		this.dragon.growlTime = 200;
		this.absorbCount--;
		this.dragon.nearestCrystal = this.absorbingCrystal;
	}
	
	@Override
	public void doServerTick() {
		this.absorbCount--;
		this.dragon.ambientSoundTime = 0;
		
		if (this.absorbCount > 0) {
			this.dragon.setHealth(this.dragon.getHealth() + 0.5F);
		}
	}
	
	public int getChargingCount() {
		return this.absorbCount;
	}
	
	@Override
	public boolean isSitting() {
		return true;
	}
	
	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
		return PatchedPhases.CRYSTAL_LINK;
	}
}