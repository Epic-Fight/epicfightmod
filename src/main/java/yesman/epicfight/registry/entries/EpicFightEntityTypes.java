package yesman.epicfight.registry.entries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.entity.AreaEffectBreath;
import yesman.epicfight.world.entity.DeathHarvestOrb;
import yesman.epicfight.world.entity.DodgeLocationIndicator;
import yesman.epicfight.world.entity.DroppedNetherStar;
import yesman.epicfight.world.entity.WitherGhostClone;
import yesman.epicfight.world.entity.WitherSkeletonMinion;

public final class EpicFightEntityTypes {
	private EpicFightEntityTypes() {}
	
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, EpicFightMod.MODID);
	
	public static final DeferredHolder<EntityType<?>, EntityType<AreaEffectBreath>> AREA_EFFECT_BREATH = REGISTRY.register("area_effect_breath", () ->
		EntityType.Builder.<AreaEffectBreath>of(AreaEffectBreath::new, MobCategory.MISC)
			.fireImmune().sized(6.0F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).noSummon().build("area_effect_breath")
		);
	
	public static final DeferredHolder<EntityType<?>, EntityType<DroppedNetherStar>> DROPPED_NETHER_STAR = REGISTRY.register("dropped_nether_star", () ->
		EntityType.Builder.<DroppedNetherStar>of(DroppedNetherStar::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(20).noSummon().build("dropped_nether_star")
		);
	
	public static final DeferredHolder<EntityType<?>, EntityType<WitherSkeletonMinion>> WITHER_SKELETON_MINION = REGISTRY.register("wither_skeleton_minion", () ->
		EntityType.Builder.<WitherSkeletonMinion>of(WitherSkeletonMinion::new, MobCategory.MONSTER)
			.fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.7F, 2.4F).clientTrackingRange(8).build("wither_skeleton_minion")
		);
	
	public static final DeferredHolder<EntityType<?>, EntityType<WitherGhostClone>> WITHER_GHOST_CLONE = REGISTRY.register("wither_ghost", () -> 
		EntityType.Builder.<WitherGhostClone>of(WitherGhostClone::new, MobCategory.MONSTER)
			.fireImmune().sized(0.9F, 3.5F).clientTrackingRange(10).build("wither_ghost")
		);
	
	public static final DeferredHolder<EntityType<?>, EntityType<DeathHarvestOrb>> DEATH_HARVEST_ORB = REGISTRY.register("death_harvest_orb", () ->
		EntityType.Builder.<DeathHarvestOrb>of(DeathHarvestOrb::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(1).noSummon().noSave().build("death_harvest_orb")
		);
	
	public static final DeferredHolder<EntityType<?>, EntityType<DodgeLocationIndicator>> DODGE_LOCATION_INDICATOR = REGISTRY.register("dodge_left", () ->
		EntityType.Builder.<DodgeLocationIndicator>of(DodgeLocationIndicator::new, MobCategory.MISC)
			.sized(0.0F, 0.0F).clientTrackingRange(6).updateInterval(1).noSummon().noSave().build("dodge_left")
		);
	
	public static void registerSpawnPlacementsEvent(final RegisterSpawnPlacementsEvent event) {
		event.register(WITHER_SKELETON_MINION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules, RegisterSpawnPlacementsEvent.Operation.OR);
    }
}