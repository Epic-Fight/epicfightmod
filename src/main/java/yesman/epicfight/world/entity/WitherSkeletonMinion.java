package yesman.epicfight.world.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.level.Level;

public class WitherSkeletonMinion extends WitherSkeleton {
	private WitherBoss summoner;
	
	public WitherSkeletonMinion(EntityType<? extends WitherSkeletonMinion> p_34166_, Level p_34167_) {
		super(p_34166_, p_34167_);
	}
	
	public WitherSkeletonMinion(Level level, WitherBoss summoner, double x, double y, double z) {
		super(EpicFightEntities.WITHER_SKELETON_MINION.get(), level);
		this.setPosRaw(x, y, z);
		this.summoner = summoner;
		
		if (this.summoner != null && this.summoner.isAlive()) {
			this.setTarget((LivingEntity)this.summoner.level().getEntity(this.summoner.getAlternativeTarget(0)));
		}
	}
	
	@Override
	public boolean canBeAffected(MobEffectInstance p_70687_1_) {
		return p_70687_1_.getEffect() != MobEffects.WITHER && super.canBeAffected(p_70687_1_);
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, (livingentity) -> (livingentity.getMobType() != MobType.UNDEAD && livingentity.attackable())));
	}
	
	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.summoner != null && source.getEntity() == this.summoner) {
			return false;
		}
		
		return super.hurt(source, amount);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (this.level().isClientSide()) {
			this.level().addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextGaussian() * (double) 0.3F, this.getEyeY() + this.random.nextGaussian() * (double) 0.3F, this.getZ() + this.random.nextGaussian() * (double) 0.3F, 0.0D, 0.0D, 0.0D);
		} else {
			if (this.tickCount > 200 && this.tickCount % 30 == 0) {
				this.hurt(this.level().damageSources().wither(), 1.0F);
			}
			
			if (this.summoner != null && !this.summoner.isAlive()) {
				this.setHealth(0.0F);
			}
		}
	}
}