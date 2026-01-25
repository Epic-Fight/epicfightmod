package yesman.epicfight.events;

import com.google.common.collect.Multimap;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.forgeevent.EntityStunEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.mixin.common.MixinProjectile;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPPotion;
import yesman.epicfight.network.server.SPPotion.Action;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.eventlistener.DealDamageEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.PlayerKilledEvent;
import yesman.epicfight.world.entity.eventlistener.ProjectileHitEvent;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID)
public class EntityEvents {
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void spawnEvent(EntityJoinLevelEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EntityPatch.class).ifPresent(entitypatch -> {
			if (!entitypatch.isInitialized()) {
				entitypatch.onJoinWorld(event.getEntity(), event);
			}
		});
	}
	
	@SubscribeEvent
	public static void updateEvent(LivingEvent.LivingTickEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.getOriginal() != null) {
				entitypatch.tick(event);
			}
		});
	}
	
	@SubscribeEvent
	public static void deathEvent(LivingDeathEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.onDeath(event);
		});
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.getEventListener().triggerEvents(EventType.PLAYER_KILLED_EVENT, new PlayerKilledEvent(playerpatch, event.getEntity(), event.getSource()));
		});
		
		/** Chicken explosion code 
		if (event.getEntity() instanceof Chicken) {
			Vec3 pos = event.getEntity().position();
			
			for (int i = -1; i <= 1; i+=2) {
				for (int j = -1; j <= 1; j+=2) {
					for (int k = 0; k < 8; k++) {
						float power = 0.4F;
						float powerX = event.getEntityLiving().getRandom().nextFloat() * power;
						float powerY = (event.getEntityLiving().getRandom().nextFloat() + 0.5F) * power;
						float powerZ = event.getEntityLiving().getRandom().nextFloat() * power;
						
						event.getEntity().level.addParticle( EpicFightParticles.FEATHER.get(), pos.x, pos.y, pos.z
								                           , i * powerX, powerY, j * powerZ);
					}
				}
			}
		}**/
	}
	
	@SubscribeEvent
	public static void knockBackEvent(LivingKnockBackEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.shouldCancelKnockback()) {
				event.setCanceled(true);
			}
		});
	}
	
	@SubscribeEvent
	public static void hurtEvent(LivingHurtEvent event) {
		EpicFightDamageSource epicfightDamageSource = event.getSource() instanceof EpicFightDamageSource ? (EpicFightDamageSource)event.getSource(): null;
		ValueModifier.ResultCalculator damageCalculator = ValueModifier.calculator();
		Entity causingEntity = event.getSource().getEntity();
		LivingEntity hitEntity = event.getEntity();
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
			TakeDamageEvent.Hurt hurtEvent = new TakeDamageEvent.Hurt(serverplayerpatch, event.getSource(), damageCalculator, event.getAmount());
			serverplayerpatch.getEventListener().triggerEvents(EventType.TAKE_DAMAGE_EVENT_HURT, hurtEvent);
		});
		
		if (causingEntity != null) {
			LivingEntityPatch<?> attackerentitypatch = EpicFightCapabilities.getEntityPatch(causingEntity, LivingEntityPatch.class);
			
			if (attackerentitypatch != null) {
				event.setAmount(attackerentitypatch.getModifiedBaseDamage(event.getAmount()));
			}
            
			if (epicfightDamageSource != null) {
				if (attackerentitypatch instanceof ServerPlayerPatch playerpatch) {
					DealDamageEvent.Hurt dealDamageHurt = new DealDamageEvent.Hurt(playerpatch, hitEntity, epicfightDamageSource, event);
					playerpatch.getEventListener().triggerEvents(EventType.DEAL_DAMAGE_EVENT_HURT, dealDamageHurt);
				}
				
				if (epicfightDamageSource.is(EpicFightDamageTypeTags.EXECUTION)) {
					EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, LivingEntityPatch.class).ifPresentOrElse(entitypatch -> {
						int executionResistance = entitypatch.getExecutionResistance();
						
						if (executionResistance > 0) {
							entitypatch.setExecutionResistance(executionResistance - 1);
						} else {
							event.setAmount(EpicFightSharedConstants.EXECUTION_DAMAGE);
						}
					}, () -> {
						event.setAmount(EpicFightSharedConstants.EXECUTION_DAMAGE);
					});
				}
			}
		}
		
		if (Float.compare(EpicFightSharedConstants.EXECUTION_DAMAGE, event.getAmount()) != 0) {
			if (epicfightDamageSource != null) {
				epicfightDamageSource.attachDamageModifier(damageCalculator);
				float result = epicfightDamageSource.calculateDamageAgainst(causingEntity, hitEntity, event.getAmount());
				event.setAmount(result);
			} else {
				float result = damageCalculator.getResult(event.getAmount());
				event.setAmount(result);
			}
		}
		
		if (Float.compare(event.getAmount(), 0.0F) == 1 && epicfightDamageSource != null && !epicfightDamageSource.is(EpicFightDamageTypeTags.NO_STUN)) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, HurtableEntityPatch.class).ifPresent(hitentitypatch -> {
				StunType stunType = epicfightDamageSource.getStunType();
				float stunTime = 0.0F;
				float knockBackAmount = 0.0F;
				float stunShield = hitentitypatch.getStunShield();
				float impact = epicfightDamageSource.calculateImpact();
				
				if (stunShield > impact) {
					if (stunType == StunType.SHORT || stunType == StunType.LONG) {
						stunType = StunType.NONE;
					}
				}
				
				EntityStunEvent entityStunEvent = new EntityStunEvent(epicfightDamageSource, hitentitypatch, stunType);
				
				if (MinecraftForge.EVENT_BUS.post(entityStunEvent)) {
					return;
				}
				
				hitentitypatch.damageStunShield(event.getAmount(), impact);
				
				switch (stunType) {
				case SHORT:
					// Solution by Cyber2049(github): Fix stun immunity
					stunType = StunType.NONE;
					
					if (!hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY.get()) && (hitentitypatch.getStunShield() == 0.0F)) {
						float totalStunTime = (0.25F + impact * 0.1F) * (1.0F - hitentitypatch.getStunReduction());
						
						if (totalStunTime >= 0.075F) {
							stunTime = totalStunTime - 0.1F;
							boolean isLongStun = totalStunTime >= 0.83F;
							stunTime = isLongStun ? 0.83F : stunTime;
							stunType = isLongStun ? StunType.LONG : StunType.SHORT;
							knockBackAmount = Math.min(isLongStun ? impact * 0.05F : totalStunTime, 2.0F);
						}
						
						stunTime *= 1.0F - hitEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
					}
					break;
				case LONG:
					stunType = hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY.get()) ? StunType.NONE : StunType.LONG;
					knockBackAmount = Math.min(impact * 0.05F, 5.0F);
					stunTime = 0.83F;
					break;
				case HOLD:
					stunType = StunType.SHORT;
					stunTime = impact * 0.25F;
					break;
				case KNOCKDOWN:
					stunType = hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY.get()) ? StunType.NONE : StunType.KNOCKDOWN;
					knockBackAmount = Math.min(impact * 0.05F, 5.0F);
					stunTime = 2.0F;
					break;
				case NEUTRALIZE:
					stunType = StunType.NEUTRALIZE;
					hitentitypatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
					EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(((ServerLevel)hitEntity.level()), hitEntity, event.getSource().getDirectEntity());
					knockBackAmount = 0.0F;
					stunTime = 2.0F;
				default:
					break;
				}
				
				Vec3 sourcePosition = epicfightDamageSource.getInitialPosition();
				hitentitypatch.setStunReductionOnHit(stunType);
				boolean stunApplied = hitentitypatch.applyStun(stunType, stunTime);
				
				if (sourcePosition != null) {
					if (!(hitEntity instanceof Player) && stunApplied) {
						hitEntity.lookAt(EntityAnchorArgument.Anchor.FEET, sourcePosition);
					}
					
					if (knockBackAmount > 0.0F) {
						knockBackAmount *= 40.0F / hitentitypatch.getWeight();
						
						hitentitypatch.knockBackEntity(sourcePosition, knockBackAmount);
					}
				}
			});
		}
		
		if (event.getSource().is(DamageTypes.FALL) && event.getAmount() > 1.0F && EpicFightGameRules.HAS_FALL_ANIMATION.getRuleValue(event.getEntity().level())) {
			LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
			
			if (entitypatch != null && !entitypatch.getEntityState().inaction()) {
				AssetAccessor<? extends StaticAnimation> fallAnimation = entitypatch.getAnimator().getLivingAnimation(LivingMotions.LANDING_RECOVERY, entitypatch.getHitAnimation(StunType.FALL));
				
				if (fallAnimation != null) {
					entitypatch.playAnimationSynchronized(fallAnimation, 0);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void damageEvent(LivingDamageEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			if (event.getSource() instanceof EpicFightDamageSource epicFightDamageSource) {
				playerpatch.getEventListener().triggerEvents(EventType.DEAL_DAMAGE_EVENT_DAMAGE, new DealDamageEvent.Damage(playerpatch, event.getEntity(), epicFightDamageSource, event));
			}
		});
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.getEventListener().triggerEvents(EventType.TAKE_DAMAGE_EVENT_DAMAGE, new TakeDamageEvent.Damage(playerpatch, event.getSource(), event.getAmount()));
		});
	}
	
	@SubscribeEvent
	public static void attackEvent(LivingAttackEvent event) {
		if (event.getEntity().level().isClientSide()) {
			return;
		}
		
		if (event.getEntity().isInvulnerableTo(event.getSource())) {
			return;
		}
		
		if (event.getEntity().invulnerableTime > 10 && event.getAmount() <= event.getEntity().lastHurt) {
			return;
		}
		
		if (event.getEntity().getHealth() <= 0.0F) {
			return;
		}
		
		if (event.getSource() instanceof EpicFightDamageSource epicfightDamagesource && event.getSource().getEntity() instanceof ServerPlayer serverplayer) {
			ServerPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(serverplayer, ServerPlayerPatch.class);
			DealDamageEvent.Attack dealDamageAttack = new DealDamageEvent.Attack(playerpatch, event.getEntity(), epicfightDamagesource, event);
			playerpatch.getEventListener().triggerEvents(EventType.DEAL_DAMAGE_EVENT_ATTACK, dealDamageAttack);
			
			if (dealDamageAttack.isCanceled()) {
				event.setCanceled(true);
				return;
			}
		}
		
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
		AttackResult result = entitypatch != null ? entitypatch.tryHurt(event.getSource(), event.getAmount()) : AttackResult.success(event.getAmount());
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().getEntity(), LivingEntityPatch.class).ifPresent(attackerentitypatch -> {
			attackerentitypatch.setLastAttackResult(result);
		});
		
		if (!result.resultType.dealtDamage()) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void shieldEvent(ShieldBlockEvent event) {
		EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(event.getEntity(), LivingEntity.class, LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.playAnimationSynchronized(Animations.BIPED_HIT_SHIELD, 0.0F);
		});
	}
	
	@SubscribeEvent
	public static void dropEvent(LivingDropsEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.onDrop(event)) {
				event.setCanceled(true);
			}
		});
	}
	
	@SubscribeEvent
	public static void projectileImpactEvent(ProjectileImpactEvent event) {
		ProjectilePatch<?> projectilepatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), ProjectilePatch.class);
		
		if (projectilepatch != null) {
			if (projectilepatch.onProjectileImpact(event)) {
				event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
			}
		}
		
		if (event.getImpactResult() != ProjectileImpactEvent.ImpactResult.SKIP_ENTITY) {
			if (event.getRayTraceResult() instanceof EntityHitResult entityHitResult) {
				if (entityHitResult.getEntity() != null) {
					EpicFightCapabilities.getUnparameterizedEntityPatch(entityHitResult.getEntity(), PlayerPatch.class).ifPresent(playerpatch -> {
						playerpatch.getEntityState().setProjectileImpactResult(event);
						
						// Fix later: since ProjectileImpactEvent fired both in client and server it needs to fire both in client and server
						if (event.getImpactResult() == ProjectileImpactEvent.ImpactResult.DEFAULT && playerpatch instanceof ServerPlayerPatch serverplayerpatch) {
							boolean canceled = playerpatch.getEventListener().triggerEvents(EventType.PROJECTILE_HIT_EVENT, new ProjectileHitEvent(serverplayerpatch, event));
							
							if (canceled) {
								event.setImpactResult(ImpactResult.SKIP_ENTITY);
							}
						}
					});
					
					if (event.getProjectile().getOwner() != null) {
						if (entityHitResult.getEntity().equals(event.getProjectile().getOwner().getVehicle())) {
							event.setImpactResult(ImpactResult.SKIP_ENTITY);
						}
						
						if (entityHitResult.getEntity() instanceof PartEntity<?> partEntity) {
							Entity parent = partEntity.getParent();
							
							if (event.getProjectile().getOwner().is(parent)) {
								event.setImpactResult(ImpactResult.SKIP_ENTITY);
							}
						}
					}
					
					if (EpicFightEntities.DODGE_LOCATION_INDICATOR.get().equals(entityHitResult.getEntity().getType())) {
						if (event.getEntity() instanceof Projectile projectile) {
							((MixinProjectile)projectile).invoke_onHitEntity(entityHitResult);
						}
						
						event.setImpactResult(ImpactResult.SKIP_ENTITY);
					}
				}
			}
		}
		
		if (projectilepatch != null && event.getImpactResult() == ImpactResult.DEFAULT) {
			projectilepatch.setHit(true);
		}
	}
	
	@SubscribeEvent
	public static void itemAttributeModifierEvent(ItemAttributeModifierEvent event) {
		CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(event.getItemStack());
		
		if (!itemCap.isEmpty()) {
			Multimap<Attribute, AttributeModifier> multimap = itemCap.getAttributeModifiers(event.getSlotType(), null);
			
			for (Attribute key : multimap.keys()) {
				for (AttributeModifier modifier : multimap.get(key)) {
					event.addModifier(key, modifier);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void equipChangeEvent(LivingEquipmentChangeEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class).ifPresent(hurtableEntitypatch -> {
			hurtableEntitypatch.setDefaultStunReduction(event.getSlot(), event.getFrom(), event.getTo());
		});
		
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
		CapabilityItem fromCap = EpicFightCapabilities.getItemStackCapability(event.getFrom());
		CapabilityItem toCap = EpicFightCapabilities.getItemStackCapability(event.getTo());
		
		if (event.getSlot() != EquipmentSlot.OFFHAND) {
			if (fromCap != null) {
				event.getEntity().getAttributes().removeAttributeModifiers(fromCap.getAttributeModifiers(event.getSlot(), entitypatch));
			}
			
			if (toCap != null) {
				event.getEntity().getAttributes().addTransientAttributeModifiers(toCap.getAttributeModifiers(event.getSlot(), entitypatch));
			}
		}
		
		if (entitypatch != null && entitypatch.getOriginal() != null) {
			if (event.getSlot().getType() == EquipmentSlot.Type.HAND) {
				InteractionHand hand = event.getSlot() == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
				entitypatch.updateHeldItem(fromCap, toCap, event.getFrom(), event.getTo(), hand);
			} else if (event.getSlot().getType() == EquipmentSlot.Type.ARMOR) {
				entitypatch.updateArmor(fromCap, toCap, event.getSlot());
			}
		}
	}
	
	@SuppressWarnings("removal")
    @SubscribeEvent
	public static void sizingEvent(EntityEvent.Size event) {
		if (event.getEntity() instanceof EnderDragon) {
			event.setNewSize(EntityDimensions.scalable(5.0F, 3.0F));
		}
	}
	
	@SubscribeEvent
	public static void effectAddEvent(MobEffectEvent.Added event) {
		if (!event.getEntity().level().isClientSide()) {
			EpicFightNetworkManager.sendToAll(new SPPotion(event.getEffectInstance(), Action.ACTIVATE, event.getEntity().getId()));
		}
	}
	
	@SubscribeEvent
	public static void effectRemoveEvent(MobEffectEvent.Remove event) {
		if (!event.getEntity().level().isClientSide() && event.getEffectInstance() != null) {
			EpicFightNetworkManager.sendToAll(new SPPotion(event.getEffectInstance(), Action.REMOVE, event.getEntity().getId()));
		}
	}
	
	@SubscribeEvent
	public static void effectExpiryEvent(MobEffectEvent.Expired event) {
		if (!event.getEntity().level().isClientSide()) {
			EpicFightNetworkManager.sendToAll(new SPPotion(event.getEffectInstance(), Action.REMOVE, event.getEntity().getId()));
		}
	}
	
	@SubscribeEvent
	public static void mountEvent(EntityMountEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntityMounting(), HumanoidMobPatch.class).ifPresent(humanoidMobPatch -> {
			if (!event.getLevel().isClientSide() && humanoidMobPatch.getOriginal() != null) {
				if (event.getEntityBeingMounted() instanceof Mob) {
					humanoidMobPatch.onMount(event.isMounting(), event.getEntityBeingMounted());
				}
			}
		});
	}
	
	@SubscribeEvent
	public static void tpEvent(EntityTeleportEvent.EnderEntity event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EndermanPatch.class).ifPresent(enderManPatch -> {
			if (enderManPatch.getEntityState().inaction()) {
				for (Entity collideEntity : enderManPatch.getOriginal().level().getEntitiesOfClass(Entity.class, enderManPatch.getOriginal().getBoundingBox().inflate(0.2D, 0.2D, 0.2D))) {
					if (collideEntity instanceof Projectile) {
                    	return;
                    }
                }
				
				event.setCanceled(true);
			} else if (enderManPatch.isRaging()) {
				event.setCanceled(true);
			}
		});
	}
	
	@SubscribeEvent
	public static void jumpEvent(LivingJumpEvent event) {
		EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(event.getEntity(), LivingEntity.class, LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.isLogicalClient()) {
				if (!entitypatch.getEntityState().inaction() && !event.getEntity().isInWater()) {
					AssetAccessor<? extends StaticAnimation> jumpAnimation = entitypatch.getClientAnimator().getJumpAnimation();
					entitypatch.playAnimationInClientSide(jumpAnimation, 0.0F);
				}
			}
		});
	}
}
