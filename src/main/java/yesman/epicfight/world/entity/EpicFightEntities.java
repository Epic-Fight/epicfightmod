package yesman.epicfight.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightEntities {
	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, EpicFightMod.MODID);
	
	public static final RegistryObject<EntityType<AreaEffectBreath>> AREA_EFFECT_BREATH = ENTITIES.register("area_effect_breath", () ->
		EntityType.Builder.<AreaEffectBreath>of(AreaEffectBreath::new, MobCategory.MISC)
			.fireImmune().sized(6.0F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).noSummon().build("area_effect_breath")
		);
	
	public static final RegistryObject<EntityType<DroppedNetherStar>> DROPPED_NETHER_STAR = ENTITIES.register("dropped_nether_star", () ->
		EntityType.Builder.<DroppedNetherStar>of(DroppedNetherStar::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(20).noSummon().build("dropped_nether_star")
		);
	
	public static final RegistryObject<EntityType<WitherSkeletonMinion>> WITHER_SKELETON_MINION = ENTITIES.register("wither_skeleton_minion", () ->
		EntityType.Builder.<WitherSkeletonMinion>of(WitherSkeletonMinion::new, MobCategory.MONSTER)
			.fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.7F, 2.4F).clientTrackingRange(8).build("wither_skeleton_minion")
		);
	
	public static final RegistryObject<EntityType<WitherGhostClone>> WITHER_GHOST_CLONE = ENTITIES.register("wither_ghost", () -> 
		EntityType.Builder.<WitherGhostClone>of(WitherGhostClone::new, MobCategory.MONSTER)
			.fireImmune().sized(0.9F, 3.5F).clientTrackingRange(10).build("wither_ghost")
		);
	
	public static final RegistryObject<EntityType<DeathHarvestOrb>> DEATH_HARVEST_ORB = ENTITIES.register("death_harvest_orb", () ->
		EntityType.Builder.<DeathHarvestOrb>of(DeathHarvestOrb::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(1).noSummon().noSave().build("death_harvest_orb")
		);
	
	public static final RegistryObject<EntityType<DodgeLocationIndicator>> DODGE_LOCATION_INDICATOR = ENTITIES.register("dodge_left", () ->
		EntityType.Builder.<DodgeLocationIndicator>of(DodgeLocationIndicator::new, MobCategory.MISC)
			.sized(0.0F, 0.0F).clientTrackingRange(6).updateInterval(1).noSummon().noSave().build("dodge_left")
		);
	
	public static void onSpawnPlacementRegister(final SpawnPlacementRegisterEvent event) {
		event.register(WITHER_SKELETON_MINION.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkAnyLightMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.OR);
    }
}