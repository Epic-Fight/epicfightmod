package yesman.epicfight.platform.neoforge.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.api.event.impl.VanillaItemEventHooks;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public final class NeoForgeEntityEvent {
    @SubscribeEvent
	public static void epicfight$entityConstructing(EntityEvent.EntityConstructing event) {
        VanillaEntityEventHooks.onConstruct(event.getEntity());
	}

	@SubscribeEvent
	public static void epicfight$entityJoinLevel(EntityJoinLevelEvent event) {
        VanillaEntityEventHooks.onJoinLevel(event.getEntity(), event.getLevel(), event.loadedFromDisk());

        // Cancel spawning enderman on the main island where Ender Dragon exists
        if (event.getEntity().getType() == EntityType.ENDERMAN) {
            if (VanillaEntityEventHooks.onEnderManSapwns((EnderMan) event.getEntity())) {
                event.setCanceled(true);
            }
        }
	}

	@SubscribeEvent
	public static void epicfight$entityTickPre(EntityTickEvent.Pre event) {
        VanillaEntityEventHooks.preTick(event.getEntity());
	}

	@SubscribeEvent
	public static void epicfight$entityTickPost(EntityTickEvent.Post event) {
        VanillaEntityEventHooks.postTick(event.getEntity());
	}

	@SubscribeEvent
	public static void epicfight$LivingDeath(LivingDeathEvent event) {
        VanillaEntityEventHooks.onLivingDeath(event.getEntity(), event.getSource());
	}

	@SubscribeEvent
	public static void epicfight$livingKnockBack(LivingKnockBackEvent event) {
        if (VanillaEntityEventHooks.onKnockedBack(event.getEntity())) {
            event.setCanceled(true);
        }
	}

	@SubscribeEvent
	public static void epicfight$livingIncomingDamage(LivingIncomingDamageEvent event) {
        if (VanillaEntityEventHooks.onDamageIncomes(event.getEntity(), event.getSource(), event.getAmount())) {
            event.setCanceled(true);
        }
	}

	@SubscribeEvent
	public static void epicfight$livingDamagePre(LivingDamageEvent.Pre event) {
        VanillaEntityEventHooks.onCalculateDamagePre(event.getEntity(), event.getSource(), event.getNewDamage(), event::setNewDamage);
	}

	@SubscribeEvent
	public static void epicfight$livingDamagePost(LivingDamageEvent.Post event) {
        VanillaEntityEventHooks.onCalculateDamagePost(event.getEntity(), event.getSource(), event.getNewDamage());
	}

	@SubscribeEvent
	public static void epicfight$livingShieldBlock(LivingShieldBlockEvent event) {
        VanillaEntityEventHooks.onBlockAttacksWithShield(event.getEntity());
	}

	@SubscribeEvent
	public static void epicfight$livingDropItems(LivingDropsEvent event) {
        if (VanillaEntityEventHooks.onDropItems(event.getEntity(), event.getSource(), event.getDrops())) {
            event.setCanceled(true);
        }
	}

	@SubscribeEvent
	public static void epicfight$projectileImpact(ProjectileImpactEvent event) {
        if (VanillaEntityEventHooks.onProjectileImpacts(event.getRayTraceResult(), event.getProjectile())) {
            event.setCanceled(true);
        }
	}

	@SubscribeEvent
	public static void epicfight$itemAttributeModifier(ItemAttributeModifierEvent event) {
        VanillaItemEventHooks.onModifyItemAttribute(event.getItemStack(), event::addModifier);
	}

	@SubscribeEvent
	public static void epicfight$equipChange(LivingEquipmentChangeEvent event) {
        VanillaEntityEventHooks.onEquipmentChanged(event.getEntity(), event.getFrom(), event.getTo(), event.getSlot());
	}

	@SubscribeEvent
	public static void epicfight$entitySizing(EntityEvent.Size event) {
        VanillaEntityEventHooks.onSizingEntity(event.getEntity(), event::setNewSize);
	}

	@SubscribeEvent
	public static void epicfight$mobEffectAdded(MobEffectEvent.Added event) {
        VanillaEntityEventHooks.onMobEffectAdded(event.getEffectInstance(), event.getEntity());
	}

	@SubscribeEvent
	public static void epicfight$mobEffectRemoved(MobEffectEvent.Remove event) {
        VanillaEntityEventHooks.onMobEffectRemoved(event.getEffectInstance(), event.getEntity());
	}

	@SubscribeEvent
	public static void epicfight$mobEffectExpired(MobEffectEvent.Expired event) {
        VanillaEntityEventHooks.onMobEffectExpired(event.getEffectInstance(), event.getEntity());
	}

	@SubscribeEvent
	public static void epicfight$entityMount(EntityMountEvent event) {
        VanillaEntityEventHooks.onEntityMount(event.getEntityMounting(), event.getEntityBeingMounted(), event.isMounting());
	}

	@SubscribeEvent
	public static void epicfight$endermanTeleport(EntityTeleportEvent.EnderEntity event) {
        if (event.getEntityLiving() instanceof EnderMan enderMan && VanillaEntityEventHooks.onEndermanTeleports(enderMan)) {
            event.setCanceled(true);
        }
	}

	@SubscribeEvent
	public static void epicfight$livingJump(LivingJumpEvent event) {
        VanillaEntityEventHooks.onJump(event.getEntity());
	}

	private NeoForgeEntityEvent() {}
}
