package yesman.epicfight.world.capabilities.provider;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.projectile.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.neoevent.EntityPatchRegistryEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.GlobalMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherGhostPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.*;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.projectile.*;
import yesman.epicfight.world.entity.WitherGhostClone;
import yesman.epicfight.world.entity.WitherSkeletonMinion;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class CommonEntityPatchProvider {
	public static final CommonEntityPatchProvider INSTANCE = new CommonEntityPatchProvider();
	
	private CommonEntityPatchProvider() {}
	
	private final Map<EntityType<?>, Function<Entity, EntityPatch<?>>> capabilities = new HashMap<> ();
	private final Map<EntityType<?>, Function<Entity, EntityPatch<?>>> datapackCapabilities = new HashMap<> ();
	private final Map<Class<? extends Projectile>, Function<Projectile, ProjectilePatch<?>>> typedCapabilities = new HashMap<> ();
	
	public void registerVanillaEntityPatches() {
		Map<EntityType<?>, Function<Entity, EntityPatch<?>>> registry = new HashMap<> ();
		registry.put(EntityType.PLAYER, entity -> new ServerPlayerPatch((ServerPlayer)entity));
		registry.put(EntityType.ZOMBIE, entity -> new ZombiePatch<> ((Zombie)entity));
		registry.put(EntityType.CREEPER, entity -> new CreeperPatch((Creeper)entity));
		registry.put(EntityType.ENDERMAN, entity -> new EndermanPatch((EnderMan)entity));
		registry.put(EntityType.SKELETON, entity -> new SkeletonPatch<Skeleton> ((Skeleton)entity));
		registry.put(EntityType.WITHER_SKELETON, entity -> new WitherSkeletonPatch<> ((WitherSkeleton)entity));
		registry.put(EntityType.STRAY, entity -> new StrayPatch<> ((Stray)entity));
		registry.put(EntityType.ZOMBIFIED_PIGLIN, entity -> new ZombifiedPiglinPatch((ZombifiedPiglin)entity));
		registry.put(EntityType.ZOMBIE_VILLAGER, entity -> new ZombiePatch<> ((ZombieVillager)entity));
		registry.put(EntityType.HUSK, entity -> new ZombiePatch<> ((Husk)entity));
		registry.put(EntityType.SPIDER, entity -> new SpiderPatch<> ((Spider)entity));
		registry.put(EntityType.CAVE_SPIDER, entity -> new CaveSpiderPatch<> ((CaveSpider)entity));
		registry.put(EntityType.IRON_GOLEM, entity -> new IronGolemPatch((IronGolem)entity));
		registry.put(EntityType.VINDICATOR, entity -> new VindicatorPatch<> ((Vindicator)entity));
		registry.put(EntityType.EVOKER, entity -> new EvokerPatch<> ((Evoker)entity));
		registry.put(EntityType.WITCH, entity -> new WitchPatch((Witch)entity));
		registry.put(EntityType.DROWNED, entity -> new DrownedPatch((Drowned)entity));
		registry.put(EntityType.PILLAGER, entity -> new PillagerPatch((Pillager)entity));
		registry.put(EntityType.RAVAGER, entity -> new RavagerPatch((Ravager)entity));
		registry.put(EntityType.VEX, entity -> new VexPatch((Vex)entity));
		registry.put(EntityType.PIGLIN, entity -> new PiglinPatch((Piglin)entity));
		registry.put(EntityType.PIGLIN_BRUTE, entity -> new PiglinBrutePatch((PiglinBrute)entity));
		registry.put(EntityType.HOGLIN, entity -> new HoglinPatch((Hoglin)entity));
		registry.put(EntityType.ZOGLIN, entity -> new ZoglinPatch((Zoglin)entity));
		registry.put(EntityType.ENDER_DRAGON, entity -> {
			if (entity instanceof EnderDragon enderdragon) {
				return new EnderDragonPatch(enderdragon);
			}
			return null;
		});
		registry.put(EntityType.WITHER, entity -> new WitherPatch((WitherBoss)entity));
		registry.put(EpicFightEntityTypes.WITHER_SKELETON_MINION.get(), entity -> new WitherSkeletonPatch<> ((WitherSkeletonMinion)entity));
		registry.put(EpicFightEntityTypes.WITHER_GHOST_CLONE.get(), entity -> new WitherGhostPatch((WitherGhostClone)entity));
		registry.put(EntityType.ARROW, entity -> new ArrowPatch((Arrow)entity));
		registry.put(EntityType.SPECTRAL_ARROW, entity -> new ArrowPatch((SpectralArrow)entity));
		registry.put(EntityType.WITHER_SKULL, entity -> new WitherSkullPatch((WitherSkull)entity));
		registry.put(EntityType.DRAGON_FIREBALL, entity -> new DragonFireballPatch((DragonFireball)entity));
		registry.put(EntityType.TRIDENT, entity -> new ThrownTridentPatch((ThrownTrident)entity));
		
		this.typedCapabilities.put(AbstractArrow.class, entity -> new ArrowPatch((AbstractArrow)entity));
		
		EntityPatchRegistryEvent entitypatchRegistryEvent = new EntityPatchRegistryEvent(registry);
		ModLoader.postEvent(entitypatchRegistryEvent);
		
		this.capabilities.putAll(registry);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void registerClientPlayerPatches() {
		this.capabilities.put(EntityType.PLAYER, entity -> {
			if (entity instanceof LocalPlayer localPlayer) {
				return new LocalPlayerPatch(localPlayer);
			} else if (entity instanceof RemotePlayer remotePlayer) {
				return new AbstractClientPlayerPatch<RemotePlayer> (remotePlayer);
			} else if (entity instanceof ServerPlayer serverPlayer) {
				return new ServerPlayerPatch (serverPlayer);
			} else {
				return null;
			}
		});
	}
	
	public void clearDatapackEntities() {
		this.datapackCapabilities.clear();
	}
	
	public void putCustomEntityPatch(EntityType<?> entityType, Function<Entity, EntityPatch<?>> entitypatchProvider) {
		this.datapackCapabilities.put(entityType, entitypatchProvider);
	}
	
	public Function<Entity, EntityPatch<?>> get(String registryName) {
		ResourceLocation rl = ResourceLocation.parse(registryName);
		EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(rl);
		
		return this.capabilities.get(entityType);
	}
	
	public List<EntityType<?>> getPatchedEntities() {
		List<EntityType<?>> list = new ArrayList<> ();
		list.add(null);
		this.capabilities.keySet().stream().filter((type) -> type.getCategory() != MobCategory.MISC).sorted((type$1, type$2) -> EntityType.getKey(type$1).compareTo(EntityType.getKey(type$2))).forEach(list::add);
		
		return list;
	}
	
	@SuppressWarnings("rawtypes")
	public @Nullable EntityPatch getCapability(Entity entity) {
		Function<Entity, EntityPatch<?>> provider = this.datapackCapabilities.getOrDefault(entity.getType(), this.capabilities.get(entity.getType()));
		
		if (provider != null) {
			try {
				return provider.apply(entity);
			} catch (Exception e) {
				EpicFightMod.stacktraceIfDevSide("Can't apply entity patch provider", (s) -> e);
			}
		} else if (entity instanceof Mob mob && EpicFightGameRules.GLOBAL_STUN.getRuleValue(entity.level())) {
			return new GlobalMobPatch(mob);
		}
		
		return null;
	}
}