package yesman.epicfight.world.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DeathHarvestOrb extends Entity {
	private Player dest;
	private Vec3 randVec;
	private float value;
	
	public DeathHarvestOrb(EntityType<?> type, Level level) {
		super(type, level);
	}
	
	public DeathHarvestOrb(Player dest, double x, double y, double z, int value) {
		this(EpicFightEntityTypes.DEATH_HARVEST_ORB.get(), dest.level());
		this.setPos(x, y, z);
		this.dest = dest;
		this.value = value;
		
		Vec3 toContrast = this.dest.position().add(0.0D, this.dest.getBbHeight() * 0.5D, 0.0D).subtract(this.position()).scale(-1.0D);
		double randX = this.random.nextDouble() * (toContrast.x > 0 ? 1.0D : -1.0D);
		double randY = this.random.nextDouble() * (toContrast.y > 0 ? 0.75D : -0.75D);
		double randZ = this.random.nextDouble() * (toContrast.z > 0 ? 1.0D : -1.0D);
		
		this.randVec = new Vec3(randX, randY, randZ).normalize();
	}
	
	@Override
	public void tick() {
		this.noPhysics = true;
		
		if (!this.level().isClientSide()) {
			double scaleFactor = Math.pow(Math.max(0.0D, (this.tickCount - 10) / 10.0D), 2);
			Vec3 destCenter = this.dest.position().add(0.0D, this.dest.getBbHeight() * 0.5D, 0.0D);
			Vec3 v1 = destCenter.subtract(this.position()).scale(scaleFactor);
			Vec3 v2 = this.randVec.scale(1.0D - scaleFactor);
			this.move(MoverType.SELF, v1.add(v2).scale(0.23D));
			
			if (this.position().distanceToSqr(destCenter) < 0.5625D || this.tickCount > 40) {
				EpicFightCapabilities.getUnparameterizedEntityPatch(this.dest, ServerPlayerPatch.class).ifPresent(playerpatch -> {
					SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
					container.getSkill().setConsumptionSynchronize(container, container.getResource() + this.value);
				});
				
				this.discard();
			}
		} else {
			this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
		}
	}
	
	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
	}
	
	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
	}
	
	@Override
	protected void defineSynchedData(Builder builder) {
	}
}