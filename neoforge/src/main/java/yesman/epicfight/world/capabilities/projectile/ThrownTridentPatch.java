package yesman.epicfight.world.capabilities.projectile;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.weaponinnate.EverlastingAllegiance;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.*;

import java.util.List;

public class ThrownTridentPatch extends ProjectilePatch<ThrownTrident> {
	private boolean innateActivated;
	private int returnTick;
	private float independentXRotO;
	private float independentXRot;
	
	public float renderXRot;
	public float renderXRotO;
	public float renderYRot;
	public float renderYRotO;
	
	public ThrownTridentPatch(ThrownTrident original) {
		super(original);
	}
	
	@Override
	public void onStartTracking(ServerPlayer trackingPlayer) {
		if (this.innateActivated) {
			SPEntityPairingPacket packet = new SPEntityPairingPacket(this.original.getId(), EntityPairingPacketTypes.TRIDENT_THROWN);
			packet.buffer().writeInt(this.returnTick);
			packet.buffer().writeInt(this.original.tickCount);
			
			EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
		}
	}
	
	@Override @ClientOnly
	public void entityPairing(SPEntityPairingPacket packet) {
		super.entityPairing(packet);
		
		if (packet.pairingPacketType() == EntityPairingPacketTypes.TRIDENT_THROWN) {
			this.innateActivated = true;
			this.returnTick = packet.buffer().readInt();
			this.original.tickCount = packet.buffer().readInt();
		}
	}
	
	@Override
	protected void setMaxStrikes(ThrownTrident projectileEntity, int maxStrikes) {
		projectileEntity.setPierceLevel((byte)(maxStrikes - 1));
	}
	
	@Override
    public void onJoinWorld(ThrownTrident entity, Level level, boolean worldgenSpawn) {
        super.onJoinWorld(entity, level, worldgenSpawn);
		
		if (!this.isLogicalClient()) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(entity.getOwner(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
				
				if (container.getSkill() instanceof EverlastingAllegiance) {
					EverlastingAllegiance.setThrownTridentEntityId(container, entity.getId());
				}
			});
			
			this.armorNegation = 20.0F;
		}
	}
	
	public void tickEnd() {
		if (!this.isLogicalClient()) {
			if (this.original.dealtDamage) {
				EpicFightCapabilities.getUnparameterizedEntityPatch(this.original.getOwner(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
					SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
					
					if (container.getSkill() instanceof EverlastingAllegiance) {
						if (EverlastingAllegiance.getThrownTridentEntityId(container) > -1) {
							EverlastingAllegiance.setThrownTridentEntityId(container, -1);
						}
					}
				});
			}
			
			if (this.innateActivated) {
				List<Entity> entities = this.original.level().getEntities(this.original, this.original.getBoundingBox().inflate(1.0D, 1.0D, 1.0D));
				EpicFightDamageSource source =
					EpicFightDamageSources
						.trident(this.original.getOwner(), this.original)
						.setStunType(StunType.HOLD)
						.addRuntimeTag(EpicFightDamageTypeTags.WEAPON_INNATE)
						.addExtraDamage(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create())
						.setBaseArmorNegation(30.0F)
						.attachDamageModifier(ValueModifier.multiplier(1.4F));
				
				for (Entity entity : entities) {
					if (entity.is(this.original.getOwner())) {
						continue;
					}
					
					float f = 8.0F;
					
					if (entity instanceof LivingEntity livingentity) {
						f = EnchantmentHelper.modifyDamage((ServerLevel)this.original.level(), this.original.getPickupItemStackOrigin(), livingentity, source, f);
						
						if (entity.hurt(source, f)) {
							entity.playSound(EpicFightSounds.BLADE_HIT.get(), 1.0F, 1.0F);
							((ServerLevel)entity.level()).sendParticles(EpicFightParticles.HIT_BLADE.get(), entity.position().x, entity.position().y + entity.getBbHeight() * 0.5D, entity.position().z, 0, 0, 0, 0, 1.0D);
						}
					}
				}
			}
		}
		
		if (this.innateActivated) {
			int elapsedTicks = Math.max(this.original.tickCount - this.returnTick - 10, 0);
			Vec3 toOwner = this.original.getOwner().getEyePosition().subtract(this.original.position());
			double length = toOwner.length();
			double speed = Math.min(Math.pow(elapsedTicks, 2.0D) * 0.0005D + Math.abs(elapsedTicks * 0.05D), Math.min(10.0D, length));
			Vec3 toMaster = toOwner.normalize().scale(speed);
			this.original.setDeltaMovement(new Vec3(0, 0, 0));
			Vec3 pos = this.original.position();
			this.original.setPos(pos.x + toMaster.x, pos.y + toMaster.y, pos.z + toMaster.z);
			
			this.original.setXRot(0.0F);
			this.original.xRotO = 0.0F;
			
			this.original.setYRot(0.0F);
			this.original.yRotO = 0.0F;
			
			this.independentXRotO = this.independentXRot;
			this.independentXRot += 60.0F;
			
			this.original.xRotO = this.independentXRotO;
			this.original.setXRot(this.independentXRot);
			
			if (this.original.tickCount % 3 == 0) {
				this.original.playSound(EpicFightSounds.WHOOSH_ROD.get(), 3.0F, 1.0F);
			}
		}
	}
	
	public boolean isInnateActivated() {
		return this.innateActivated;
	}
	
	public void catchByPlayer(PlayerPatch<?> playerpatch) {
		playerpatch.playAnimationSynchronized(Animations.EVERLASTING_ALLEGIANCE_CATCH, 0.0F);
	}
	
	public void recalledBySkill() {
		this.original.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
		this.original.dealtDamage = true;
		this.innateActivated = true;
		this.independentXRot = this.original.getXRot();
		this.returnTick = this.original.tickCount;
		this.initialFirePosition = this.original.position();
	}

	@Override
	public EpicFightDamageSource createEpicFightDamageSource() {
		return EpicFightDamageSources.trident(this.original, this.original.getOwner())
				.setStunType(StunType.SHORT)
				.addRuntimeTag(DamageTypeTags.IS_PROJECTILE)
				.setBaseArmorNegation(this.armorNegation)
				.setBaseImpact(this.impact)
				.setInitialPosition(this.initialFirePosition);
	}
}