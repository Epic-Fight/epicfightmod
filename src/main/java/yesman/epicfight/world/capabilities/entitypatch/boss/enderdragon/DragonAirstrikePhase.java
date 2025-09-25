package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.entity.AreaEffectBreath;

public class DragonAirstrikePhase extends PatchedDragonPhase {
	private Vec3 startpos;
	private boolean isActuallyAttacking;
	
	public DragonAirstrikePhase(EnderDragon dragon) {
		super(dragon);
	}
	
	@Override
	public void begin() {
		this.startpos = this.dragon.position();
		this.isActuallyAttacking = false;
		this.dragon.level().playLocalSound(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.dragon.getSoundSource(), 5.0F, 0.8F + this.dragon.getRandom().nextFloat() * 0.3F, false);
	}
	
	@Override
	public void end() {
		this.dragonpatch.setAttakTargetSync(null);
		
		if (this.dragonpatch.isLogicalClient()) {
			Minecraft.getInstance().getSoundManager().stop(EpicFightSounds.ENDER_DRAGON_BREATH.get().getLocation(), SoundSource.HOSTILE);
			this.dragon.level().playLocalSound(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), EpicFightSounds.ENDER_DRAGON_BREATH_FINALE.get(), this.dragon.getSoundSource(), 5.0F, 1.0F, false);
		}
	}
	
	@Override
	public void doClientTick() {
		super.doClientTick();
		Vec3 dragonpos = this.dragon.position();
		OpenMatrix4f mouthpos = this.dragonpatch.getArmature().getBoundTransformFor(this.dragonpatch.getAnimator().getPose(1.0F), Armatures.DRAGON.get().upperMouth);
		
		float f = (float)this.dragon.getLatencyPos(7, 1.0F)[0];
		float f1 = (float)(this.dragon.getLatencyPos(5, 1.0F)[1] - this.dragon.getLatencyPos(10, 1.0F)[1]);
		float f2 = MathUtils.rotWrap((this.dragon.getLatencyPos(5, 1.0F)[0] - this.dragon.getLatencyPos(10, 1.0F)[0]));
		OpenMatrix4f modelMatrix = MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f1, f1, f, f, 1.0F, 1.0F, 1.0F, 1.0F).rotateDeg(-f2 * 1.5F, Vec3f.Z_AXIS);
		mouthpos.mulFront(modelMatrix);
		
		if (this.dragon.getTarget() != null) {
			Vec3 vec31 = this.dragon.getTarget().position().add(0.0D, 12.0D, 0.0D);
			
			if (!this.isActuallyAttacking && vec31.subtract(this.dragon.position()).lengthSqr() < 900.0F) {
				this.dragon.level().playLocalSound(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), EpicFightSounds.ENDER_DRAGON_BREATH.get(), this.dragon.getSoundSource(), 5.0F, 1.0F, false);
				this.isActuallyAttacking = true;
			}
		}
		
		if (this.isActuallyAttacking) {
			for (int i = 0; i < 60; i++) {
				Vec3f particleDelta = new Vec3f(0.0F, -1.0F, 0.0F);
				float xDeg = this.dragon.getRandom().nextFloat() * 60.0F - 30.0F;
				float zDeg = this.dragon.getRandom().nextFloat() * 60.0F - 30.0F;
				float speed = Math.min((60.0F - (Math.abs(xDeg) + Math.abs(zDeg))) / 20.0F, 1.0F);
				
				particleDelta.rotate(xDeg, Vec3f.X_AXIS);
				particleDelta.rotate(zDeg, Vec3f.Z_AXIS);
				particleDelta.scale(speed);
				
				this.dragon.level().addAlwaysVisibleParticle(EpicFightParticles.BREATH_FLAME.get(), mouthpos.m30 + dragonpos.x, mouthpos.m31 + dragonpos.y, mouthpos.m32 + dragonpos.z, particleDelta.x, particleDelta.y, particleDelta.z);
			}
		}
	}
	
	@Override
	public void doServerTick() {
		LivingEntity target = this.dragon.getTarget();
		
		if (target == null) {
			this.dragon.getPhaseManager().setPhase(PatchedPhases.FLYING);
		} else {
			if (isValidTarget(target)) {
				Vec3 startToDragon = this.dragon.position().subtract(this.startpos);
				Vec3 startToTarget = target.position().subtract(this.startpos);
				
				if (startToDragon.horizontalDistanceSqr() < startToTarget.horizontalDistanceSqr()) {
					Vec3 vec31 = target.position().add(0.0D, 12.0D, 0.0D);
					
					if (!this.isActuallyAttacking && vec31.subtract(this.dragon.position()).lengthSqr() < 900.0F) {
						this.isActuallyAttacking = true;
					}
					
					double d8 = vec31.x - this.dragon.getX();
					double d9 = vec31.y - this.dragon.getY();
					double d10 = vec31.z - this.dragon.getZ();
					float f5 = this.getFlySpeed();
					double d4 = Math.sqrt(d8 * d8 + d10 * d10);
					
					if (d4 > 0.0D) {
						d9 = Mth.clamp(d9 / d4, -f5, f5);
					}
					
					this.dragon.setDeltaMovement(this.dragon.getDeltaMovement().add(0.0D, d9 * 0.1D, 0.0D));
					this.dragon.setYRot(Mth.wrapDegrees(this.dragon.getYRot()));
					Vec3 vec32 = vec31.subtract(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()).normalize();
					Vec3 vec33 = (new Vec3(Mth.sin(this.dragon.getYRot() * ((float) Math.PI / 180F)), this.dragon.getDeltaMovement().y, -Mth.cos(this.dragon.getYRot() * ((float) Math.PI / 180F)))).normalize();
					float f6 = Math.max(((float)vec33.dot(vec32) + 0.5F) / 1.5F, 0.0F);
					
					if (Math.abs(d8) > (double)1.0E-5F || Math.abs(d10) > (double)1.0E-5F) {
						double dx = target.getX() - this.dragon.getX();
						double dz = target.getZ() - this.dragon.getZ();
						float yRot = 180.0F - (float)Math.toDegrees(Mth.atan2(dx, dz));
						this.dragon.setYRot(MathUtils.rotlerp(this.dragon.getYRot(), yRot, 6.0F));
						double speed = (-0.5D - 1.0D / (1.0D + Math.pow(Math.E, -(d4 / 10.0D - 4.0F)))) * f6;
						Vec3 forward = this.dragon.getForward().scale(speed);
						this.dragon.move(MoverType.SELF, forward);
					}
					
					if (this.dragon.inWall) {
						this.dragon.move(MoverType.SELF, this.dragon.getDeltaMovement().scale(0.8F));
					} else {
						this.dragon.move(MoverType.SELF, this.dragon.getDeltaMovement());
					}
					
					Vec3 vec34 = this.dragon.getDeltaMovement().normalize();
					double d6 = 0.8D + 0.15D * (vec34.dot(vec33) + 1.0D) / 2.0D;
					this.dragon.setDeltaMovement(this.dragon.getDeltaMovement().multiply(d6, 0.91F, d6));
					
					if (this.isActuallyAttacking) {
						if (this.dragon.tickCount % 5 == 0) {
							Vec3 createpos = this.dragon.position().add(this.dragon.getLookAngle().scale(-4.5D));
							AreaEffectBreath breatharea = new AreaEffectBreath(this.dragon.level(), createpos.x, createpos.y, createpos.z);
							breatharea.setOwner(this.dragon);
							breatharea.setWaitTime(0);
							breatharea.setRadius(0.5F);
							breatharea.setDuration(15);
							breatharea.setRadiusPerTick(0.2F);
							breatharea.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
							breatharea.setDeltaMovement(0, -1, 0);
							this.dragon.level().addFreshEntity(breatharea);
						}
					}
				} else {
					this.dragon.getPhaseManager().setPhase(PatchedPhases.FLYING);
				}
			} else {
				this.dragon.getPhaseManager().setPhase(PatchedPhases.FLYING);
			}
		}
	}
	
	public boolean isActuallyAttacking() {
		return this.isActuallyAttacking;
	}
	
	@Override
	public float getFlySpeed() {
		return 2.0F;
	}
	
	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
		return PatchedPhases.AIRSTRIKE;
	}
}