package yesman.epicfight.world.capabilities.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.forgeevent.EntityPatchRegistryEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.GlobalMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherGhostPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.WitherPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.CaveSpiderPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.CreeperPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.DrownedPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.EndermanPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.EvokerPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.HoglinPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.IronGolemPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.PiglinBrutePatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.PiglinPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.PillagerPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.RavagerPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.SkeletonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.SpiderPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.StrayPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.VexPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.VindicatorPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.WitchPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.WitherSkeletonPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.ZoglinPatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.ZombiePatch;
import yesman.epicfight.world.capabilities.entitypatch.mob.ZombifiedPiglinPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.projectile.ArrowPatch;
import yesman.epicfight.world.capabilities.projectile.DragonFireballPatch;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;
import yesman.epicfight.world.capabilities.projectile.WitherSkullPatch;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class EntityPatchProvider implements ICapabilityProvider, NonNullSupplier<EntityPatch<?>> {
	private static final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> CAPABILITIES = Maps.newHashMap();
	private static final Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> CUSTOM_CAPABILITIES = Maps.newHashMap();
	
	private static final Map<Class<? extends Projectile>, Supplier<ProjectilePatch<?>>> BY_CLASS = new HashMap<Class<? extends Projectile>, Supplier<ProjectilePatch<?>>> ();
	
	public static void registerEntityPatches() {
		Map<EntityType<?>, Function<Entity, Supplier<EntityPatch<?>>>> registry = Maps.newHashMap();
		registry.put(EntityType.PLAYER, (entityIn) -> ServerPlayerPatch::new);
		registry.put(EntityType.ZOMBIE, (entityIn) -> ZombiePatch<Zombie>::new);
		registry.put(EntityType.CREEPER, (entityIn) -> CreeperPatch::new);
		registry.put(EntityType.ENDERMAN, (entityIn) -> EndermanPatch::new);
		registry.put(EntityType.SKELETON, (entityIn) -> SkeletonPatch<Skeleton>::new);
		registry.put(EntityType.WITHER_SKELETON, (entityIn) -> WitherSkeletonPatch::new);
		registry.put(EntityType.STRAY, (entityIn) -> StrayPatch::new);
		registry.put(EntityType.ZOMBIFIED_PIGLIN, (entityIn) -> ZombifiedPiglinPatch::new);
		registry.put(EntityType.ZOMBIE_VILLAGER, (entityIn) -> ZombiePatch<ZombieVillager>::new);
		registry.put(EntityType.HUSK, (entityIn) -> ZombiePatch<Husk>::new);
		registry.put(EntityType.SPIDER, (entityIn) -> SpiderPatch::new);
		registry.put(EntityType.CAVE_SPIDER, (entityIn) -> CaveSpiderPatch::new);
		registry.put(EntityType.IRON_GOLEM, (entityIn) -> IronGolemPatch::new);
		registry.put(EntityType.VINDICATOR, (entityIn) -> VindicatorPatch::new);
		registry.put(EntityType.EVOKER, (entityIn) -> EvokerPatch::new);
		registry.put(EntityType.WITCH, (entityIn) -> WitchPatch::new);
		registry.put(EntityType.DROWNED, (entityIn) -> DrownedPatch::new);
		registry.put(EntityType.PILLAGER, (entityIn) -> PillagerPatch::new);
		registry.put(EntityType.RAVAGER, (entityIn) -> RavagerPatch::new);
		registry.put(EntityType.VEX, (entityIn) -> VexPatch::new);
		registry.put(EntityType.PIGLIN, (entityIn) -> PiglinPatch::new);
		registry.put(EntityType.PIGLIN_BRUTE, (entityIn) -> PiglinBrutePatch::new);
		registry.put(EntityType.HOGLIN, (entityIn) -> HoglinPatch::new);
		registry.put(EntityType.ZOGLIN, (entityIn) -> ZoglinPatch::new);
		registry.put(EntityType.ENDER_DRAGON, (entityIn) -> {
			if (entityIn instanceof EnderDragon) {
				return EnderDragonPatch::new;
			}
			return () -> null;
		});
		registry.put(EntityType.WITHER, (entityIn) -> WitherPatch::new);
		registry.put(EpicFightEntities.WITHER_SKELETON_MINION.get(), (entityIn) -> WitherSkeletonPatch::new);
		registry.put(EpicFightEntities.WITHER_GHOST_CLONE.get(), (entityIn) -> WitherGhostPatch::new);
		registry.put(EntityType.ARROW, (entityIn) -> ArrowPatch::new);
		registry.put(EntityType.SPECTRAL_ARROW, (entityIn) -> ArrowPatch::new);
		registry.put(EntityType.WITHER_SKULL, (entityIn) -> WitherSkullPatch::new);
		registry.put(EntityType.DRAGON_FIREBALL, (entityIn) -> DragonFireballPatch::new);
		registry.put(EntityType.TRIDENT, (entityIn) -> ThrownTridentPatch::new);
		
		BY_CLASS.put(AbstractArrow.class, ArrowPatch::new);
		
		EntityPatchRegistryEvent entitypatchRegistryEvent = new EntityPatchRegistryEvent(registry);
		ModLoader.get().postEvent(entitypatchRegistryEvent);
		
		CAPABILITIES.putAll(registry);
	}
	
	public static void registerEntityPatchesClient() {
		CAPABILITIES.put(EntityType.PLAYER, (entityIn) -> {
			if (entityIn instanceof LocalPlayer) {
				return LocalPlayerPatch::new;
			} else if (entityIn instanceof RemotePlayer) {
				return AbstractClientPlayerPatch<RemotePlayer>::new;
			} else if (entityIn instanceof ServerPlayer) {
				return ServerPlayerPatch::new;
			} else {
				return () -> null;
			}
		});
	}
	
	public static void clear() {
		CUSTOM_CAPABILITIES.clear();
	}
	
	public static void putCustomEntityPatch(EntityType<?> entityType, Function<Entity, Supplier<EntityPatch<?>>> entitypatchProvider) {
		CUSTOM_CAPABILITIES.put(entityType, entitypatchProvider);
	}
	
	public static Function<Entity, Supplier<EntityPatch<?>>> get(String registryName) {
		ResourceLocation rl = ResourceLocation.parse(registryName);
		EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(rl);
		
		return CAPABILITIES.get(entityType);
	}
	
	public static List<EntityType<?>> getPatchedEntities() {
		List<EntityType<?>> list = Lists.newArrayList();
		list.add(null);
		CAPABILITIES.keySet().stream().filter((type) -> type.getCategory() != MobCategory.MISC).sorted((type$1, type$2) -> EntityType.getKey(type$1).compareTo(EntityType.getKey(type$2))).forEach(list::add);
		
		return list;
	}
	
	private EntityPatch<?> capability;
	private final LazyOptional<EntityPatch<?>> optional = LazyOptional.of(this);
	
	public EntityPatchProvider(Entity entity) {
		Function<Entity, Supplier<EntityPatch<?>>> provider = CUSTOM_CAPABILITIES.getOrDefault(entity.getType(), CAPABILITIES.get(entity.getType()));
		
		if (provider != null) {
			try {
				this.capability = provider.apply(entity).get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (entity instanceof Mob && EpicFightGameRules.GLOBAL_STUN.getRuleValue(entity.level())) {
			this.capability = new GlobalMobPatch();
		}
	}
	
	public boolean hasCapability() {
		return capability != null;
	}
	
	@Override
	public EntityPatch<?> get() {
		return this.capability;
	}
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return cap == EpicFightCapabilities.CAPABILITY_ENTITY ? this.optional.cast() :  LazyOptional.empty();
	}
}