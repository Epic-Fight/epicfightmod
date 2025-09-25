package yesman.epicfight.events;

import com.google.common.collect.Multimap;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerFlyableFallEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.neoevent.EntityRemoveEvent;
import yesman.epicfight.api.neoevent.EntityStunEvent;
import yesman.epicfight.api.neoevent.playerpatch.DealDamageEvent;
import yesman.epicfight.api.neoevent.playerpatch.PatchedProjectileHitEvent;
import yesman.epicfight.api.neoevent.playerpatch.PlayerKilledEvent;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.api.neoevent.playerpatch.StartActionEvent;
import yesman.epicfight.api.neoevent.playerpatch.TakeDamageEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.mixin.common.MixinProjectile;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPMobEffectControl;
import yesman.epicfight.network.server.SPMobEffectControl.Action;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.registry.entries.EpicFightMobEffects;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.ArmorCapability;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public final class EntityEvents {
	private EntityEvents() {}
	
	@SubscribeEvent
	public static void epicfight$entityConstructing(EntityEvent.EntityConstructing event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EntityPatch.class).ifPresent(entitypatch -> {
			if (!entitypatch.isInitialized()) {
				entitypatch.onConstructed(event);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void epicfight$entityJoinLevel(EntityJoinLevelEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EntityPatch.class).ifPresent(entitypatch -> {
			if (!entitypatch.isInitialized()) {
				entitypatch.onJoinWorld(event.getEntity(), event);
			}
		});
	}
	
	@SubscribeEvent
	public static void epicfight$entityTickPre(EntityTickEvent.Pre event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.getOriginal() != null) {
				entitypatch.preTick(event);
				
				if (entitypatch.isLogicalClient()) {
					entitypatch.preTickClient(event);
				} else {
					entitypatch.preTickServer(event);
				}
			}
		});
	}
	
	@SubscribeEvent
	public static void epicfight$entityTickPost(EntityTickEvent.Post event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), EntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.getOriginal() != null) {
				entitypatch.postTick(event);
				
				if (entitypatch.isLogicalClient()) {
					entitypatch.postTickClient(event);
				} else {
					entitypatch.postTickServer(event);
				}
			}
		});
	}
	
	@SubscribeEvent
	public static void epicfight$LivingDeath(LivingDeathEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.onDeath(event);
		});
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			PlayerPatchEvent.postAndFireSkillListeners(new PlayerKilledEvent(playerpatch, event.getEntity(), event.getSource()));
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
	public static void epicfight$EntityRemoved(EntityRemoveEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.onRemoved(event);
		});
	}
	
	@SubscribeEvent
	public static void epicfight$livingKnockBack(LivingKnockBackEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.shouldCancelKnockback()) {
				event.setCanceled(true);
			}
		});
	}
	
	@SubscribeEvent
	public static void epicfight$livingIncomingDamage(LivingIncomingDamageEvent event) {
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
			DealDamageEvent.Income dealDamageAttack = new DealDamageEvent.Income(playerpatch, event.getEntity(), epicfightDamagesource, event);
			
			if (PlayerPatchEvent.postAndFireSkillListeners(dealDamageAttack).isCanceled()) {
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
	public static void epicfight$livingDamagePre(LivingDamageEvent.Pre event) {
		EpicFightDamageSource epicfightDamageSource = event.getSource() instanceof EpicFightDamageSource ? (EpicFightDamageSource)event.getSource() : null;
		ValueModifier.ResultCalculator damageCalculator = ValueModifier.calculator();
		Entity causingEntity = event.getSource().getEntity();
		LivingEntity hitEntity = event.getEntity();
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
			TakeDamageEvent.Pre takeDamagePost = new TakeDamageEvent.Pre(serverplayerpatch, event.getSource(), damageCalculator, event.getNewDamage());
			PlayerPatchEvent.postAndFireSkillListeners(takeDamagePost);
		});
		
		if (causingEntity != null) {
			LivingEntityPatch<?> attackerentitypatch = EpicFightCapabilities.getEntityPatch(causingEntity, LivingEntityPatch.class);
			
			if (attackerentitypatch != null) {
				event.setNewDamage(attackerentitypatch.getModifiedBaseDamage(event.getNewDamage()));
			}
            
			if (epicfightDamageSource != null) {
				if (attackerentitypatch instanceof ServerPlayerPatch playerpatch) {
					DealDamageEvent.Pre dealDamageHurt = new DealDamageEvent.Pre(playerpatch, hitEntity, epicfightDamageSource, event);
					PlayerPatchEvent.postAndFireSkillListeners(dealDamageHurt);
				}
				
				if (epicfightDamageSource.is(EpicFightDamageTypeTags.EXECUTION)) {
					EpicFightCapabilities.getUnparameterizedEntityPatch(hitEntity, LivingEntityPatch.class).ifPresentOrElse(entitypatch -> {
						int executionResistance = entitypatch.getAssassinationResistance();
						
						if (executionResistance > 0) {
							entitypatch.setExecutionResistance(executionResistance - 1);
						} else {
							event.setNewDamage(EpicFightSharedConstants.EXECUTION_DAMAGE);
						}
					}, () -> {
						event.setNewDamage(EpicFightSharedConstants.EXECUTION_DAMAGE);
					});
				}
			}
		}
		
		if (Float.compare(EpicFightSharedConstants.EXECUTION_DAMAGE, event.getNewDamage()) != 0) {
			if (epicfightDamageSource != null) {
				epicfightDamageSource.attachDamageModifier(damageCalculator);
				float result = epicfightDamageSource.calculateDamageAgainst(causingEntity, hitEntity, event.getNewDamage());
				event.setNewDamage(result);
			} else {
				float result = damageCalculator.getResult(event.getNewDamage());
				event.setNewDamage(result);
			}
		}
		
		// Apply stun & knockback
		if (Float.compare(event.getOriginalDamage(), 0.0F) == 1 && epicfightDamageSource != null && !epicfightDamageSource.is(EpicFightDamageTypeTags.NO_STUN)) {
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
				
				if (NeoForge.EVENT_BUS.post(entityStunEvent).isCanceled()) {
					return;
				}
				
				hitentitypatch.setStunShield(stunShield - impact);
				
				switch (stunType) {
				case SHORT -> {
					// Solution by Cyber2049(github): Fix stun immunity
					stunType = StunType.NONE;
					
					if (!hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY) && Float.compare(hitentitypatch.getStunShield(), 0.0F) == 0) {
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
				}
				case LONG -> {
					stunType = hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY) ? StunType.NONE : StunType.LONG;
					knockBackAmount = Math.min(impact * 0.05F, 5.0F);
					stunTime = 0.83F;
				}
				case HOLD -> {
					stunType = StunType.SHORT;
					stunTime = impact * 0.25F;
				}
				case KNOCKDOWN -> {
					stunType = hitEntity.hasEffect(EpicFightMobEffects.STUN_IMMUNITY) ? StunType.NONE : StunType.KNOCKDOWN;
					knockBackAmount = Math.min(impact * 0.05F, 5.0F);
					stunTime = 2.0F;
				}
				case NEUTRALIZE -> {
					stunType = StunType.NEUTRALIZE;
					hitentitypatch.playSound(EpicFightSounds.NEUTRALIZE_MOBS.get(), 3.0F, 0.0F, 0.1F);
					EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(((ServerLevel)hitEntity.level()), hitEntity, event.getSource().getDirectEntity());
					knockBackAmount = 0.0F;
					stunTime = 2.0F;
				}
				default -> {
				}
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
		
		if (event.getSource().is(DamageTypes.FALL) && event.getNewDamage() > 1.0F && EpicFightGameRules.HAS_FALL_ANIMATION.getRuleValue(event.getEntity().level())) {
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
	public static void epicfight$livingDamagePost(LivingDamageEvent.Post event) {
		if (event.getSource() instanceof EpicFightDamageSource epicFightDamageSource) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(event.getSource().getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				PlayerPatchEvent.postAndFireSkillListeners(new DealDamageEvent.Post(playerpatch, event.getEntity(), epicFightDamageSource, event));
			});
		}
		
		if (event.getSource().getEntity() != null) {
			EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				PlayerPatchEvent.postAndFireSkillListeners(new TakeDamageEvent.Post(playerpatch, event.getSource(), event.getNewDamage()));
			});
		}
	}

	@SubscribeEvent
	public static void epicfight$livingShieldBlock(LivingShieldBlockEvent event) {
		EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(event.getEntity(), LivingEntity.class, LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.playAnimationSynchronized(Animations.BIPED_HIT_SHIELD, 0.0F);
		});
	}
	
	@SubscribeEvent
	public static void epicfight$livingDropItems(LivingDropsEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.onDrop(event)) {
				event.setCanceled(true);
			}
		});
	}
	
	@SubscribeEvent
	public static void epicfight$projectileImpact(ProjectileImpactEvent event) {
		ProjectilePatch<?> projectilepatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), ProjectilePatch.class);
		
		if (projectilepatch != null) {
			if (projectilepatch.onProjectileImpact(event)) {
				event.setCanceled(true);
			}
		}
		
		if (!event.isCanceled()) {
			if (event.getRayTraceResult() instanceof EntityHitResult rayresult) {
				if (rayresult.getEntity() != null) {
					EpicFightCapabilities.getUnparameterizedEntityPatch(rayresult.getEntity(), PlayerPatch.class).ifPresent(playerpatch -> {
						playerpatch.getEntityState().setProjectileImpactResult(event);
						boolean canceled = PlayerPatchEvent.postAndFireSkillListeners(new PatchedProjectileHitEvent(playerpatch, event)).isCanceled();
						
						if (canceled) {
							event.setCanceled(true);
						}
					});
					
					if (event.getProjectile().getOwner() != null) {
						if (rayresult.getEntity().equals(event.getProjectile().getOwner().getVehicle())) {
							event.setCanceled(true);
						}
						
						if (rayresult.getEntity() instanceof PartEntity<?> partEntity) {
							Entity parent = partEntity.getParent();
							
							if (event.getProjectile().getOwner().is(parent)) {
								event.setCanceled(true);
							}
						}
					}
					
					if (EpicFightEntityTypes.DODGE_LOCATION_INDICATOR.get().equals(rayresult.getEntity().getType())) {
						if (event.getEntity() instanceof Projectile projectile) {
							((MixinProjectile)projectile).invoke_onHitEntity(rayresult);
						}
						
						event.setCanceled(true);
					}
				}
			}
		}
		
		if (projectilepatch != null && !event.isCanceled()) {
			projectilepatch.setHit(true);
		}
	}
	
	@SubscribeEvent
	public static void epicfight$itemAttributeModifier(ItemAttributeModifierEvent event) {
		CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(event.getItemStack());
		
		if (!itemCap.isEmpty()) {
			Multimap<Holder<Attribute>, AttributeModifier> multimap = itemCap.getAttributeModifiers(null);
			
			for (Holder<Attribute> key : multimap.keys()) {
				for (AttributeModifier modifier : multimap.get(key)) {
					event.addModifier(key, modifier, EquipmentSlotGroup.MAINHAND);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void epicfight$equipChange(LivingEquipmentChangeEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), HurtableEntityPatch.class).ifPresent(hurtableEntitypatch -> {
			hurtableEntitypatch.setDefaultStunReduction(event.getSlot(), event.getFrom(), event.getTo());
		});
		
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LivingEntityPatch.class);
		CapabilityItem fromCap = EpicFightCapabilities.getItemStackCapability(event.getFrom());
		CapabilityItem toCap = EpicFightCapabilities.getItemStackCapability(event.getTo());
		
		if (event.getSlot() != EquipmentSlot.OFFHAND) {
			if (fromCap != null) {
				event.getEntity().getAttributes().removeAttributeModifiers(fromCap.getAttributeModifiers(entitypatch));
			}
			
			if (toCap != null) {
				event.getEntity().getAttributes().addTransientAttributeModifiers(toCap.getAttributeModifiers(entitypatch));
			}
		}
		
		if (entitypatch != null && entitypatch.getOriginal() != null) {
			if (event.getSlot().getType() == EquipmentSlot.Type.HAND) {
				InteractionHand hand = event.getSlot() == EquipmentSlot.MAINHAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
				entitypatch.updateHeldItem(fromCap, toCap, event.getFrom(), event.getTo(), hand);
			} else if (event.getSlot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				if (fromCap instanceof ArmorCapability armorCapFrom && toCap instanceof ArmorCapability armorCapTo) {
					entitypatch.updateArmor(armorCapFrom, armorCapTo, event.getSlot());
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void epicfight$entitySizing(EntityEvent.Size event) {
		if (event.getEntity() instanceof EnderDragon) {
			event.setNewSize(EntityDimensions.scalable(5.0F, 3.0F));
		}
	}
	
	@SubscribeEvent
	public static void epicfight$mobEffectAdded(MobEffectEvent.Added event) {
		if (!event.getEntity().level().isClientSide()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPMobEffectControl(Action.ACTIVATE, event.getEffectInstance().getEffect(), event.getEntity().getId()), event.getEntity());
		}
	}
	
	@SubscribeEvent
	public static void epicfight$mobEffectRemoved(MobEffectEvent.Remove event) {
		if (!event.getEntity().level().isClientSide() && event.getEffectInstance() != null) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPMobEffectControl(Action.REMOVE, event.getEffectInstance().getEffect(), event.getEntity().getId()), event.getEntity());
		}
	}
	
	@SubscribeEvent
	public static void epicfight$mobEffectExpired(MobEffectEvent.Expired event) {
		if (!event.getEntity().level().isClientSide()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPMobEffectControl(Action.REMOVE, event.getEffectInstance().getEffect(), event.getEntity().getId()), event.getEntity());
		}
	}
	
	@SubscribeEvent
	public static void epicfight$entityMount(EntityMountEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntityMounting(), HumanoidMobPatch.class).ifPresent(humanoidMobPatch -> {
			if (!event.getLevel().isClientSide() && humanoidMobPatch.getOriginal() != null) {
				if (event.getEntityBeingMounted() instanceof Mob) {
					humanoidMobPatch.onMount(event.isMounting(), event.getEntityBeingMounted());
				}
			}
		});
	}
	
	@SubscribeEvent
	public static void epicfight$endermanTeleport(EntityTeleportEvent.EnderEntity event) {
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
	public static void epicfight$livingJump(LivingJumpEvent event) {
		EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(event.getEntity(), LivingEntity.class, LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.isLogicalClient()) {
				if (!entitypatch.getEntityState().inaction() && !event.getEntity().isInWater()) {
					AssetAccessor<? extends StaticAnimation> jumpAnimation = entitypatch.getClientAnimator().getJumpAnimation();
					entitypatch.playAnimationInClientSide(jumpAnimation, 0.0F);
				}
			}
		});
	}

	@SubscribeEvent
	public static void epicfight$livingFall(LivingFallEvent event) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(event.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.onFall(event);
		});
	}
	
	@SubscribeEvent
	public static void epicfight$flyablePlayerFall(PlayerFlyableFallEvent event) {
		EpicFightCapabilities.<Player, PlayerPatch<Player>>getParameterizedEntityPatch(event.getEntity(), Player.class, PlayerPatch.class).ifPresent(entitypatch -> {
			entitypatch.onFall(new LivingFallEvent(event.getEntity(), event.getDistance(), event.getMultiplier()));
		});
	}
	
	@SubscribeEvent
	public static void epicfight$startAction(StartActionEvent event) {
		event.runOnServer(PlayerPatch::resetActionTick);
	}
	
	@SubscribeEvent
	public static void epicfight$dealDamagePost(DealDamageEvent.Post event) {
		event.runOnServer(playerpatch -> {
			if (event.getDamageSource().shouldChargeWeapon()) {
				SkillContainer container = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
				ItemStack mainHandItem = playerpatch.getOriginal().getMainHandItem();
				
				if (!container.isFull() && !container.isActivated() && container.hasSkill(EpicFightCapabilities.getItemStackCapability(mainHandItem).getInnateSkill(playerpatch, mainHandItem))) {
					float value = container.getResource() + event.getNeoForgeEvent().getNewDamage();
					
					if (value > 0.0F) {
						container.getSkill().setConsumptionSynchronize(container, value);
					}
				}
			}
		});
	}
}
