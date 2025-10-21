package yesman.epicfight.client.events;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.particle.AirBurstParticle;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.particle.AshDirectionalParticle;
import yesman.epicfight.client.particle.BladeRushParticle;
import yesman.epicfight.client.particle.BloodParticle;
import yesman.epicfight.client.particle.CatharsisParticle;
import yesman.epicfight.client.particle.CutParticle;
import yesman.epicfight.client.particle.DustParticle;
import yesman.epicfight.client.particle.EnderParticle;
import yesman.epicfight.client.particle.EntityAfterimageParticle;
import yesman.epicfight.client.particle.EviscerateParticle;
import yesman.epicfight.client.particle.FeatherParticle;
import yesman.epicfight.client.particle.ForceFieldEndParticle;
import yesman.epicfight.client.particle.ForceFieldParticle;
import yesman.epicfight.client.particle.GroundSlamParticle;
import yesman.epicfight.client.particle.HitBluntParticle;
import yesman.epicfight.client.particle.HitCutParticle;
import yesman.epicfight.client.particle.LaserParticle;
import yesman.epicfight.client.particle.ProjectileTrailParticle;
import yesman.epicfight.client.particle.TsunamiSplashParticle;
import yesman.epicfight.client.renderer.blockentity.FractureBlockRenderer;
import yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.client.renderer.entity.WitherSkeletonMinionRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightBlockEntities;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.skill.SkillCategory;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public final class ClientModBusEvent {
	private ClientModBusEvent() {}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void epicfight$registerParticleProviders(final RegisterParticleProvidersEvent event) {
    	event.registerSpriteSet(EpicFightParticles.ENDERMAN_DEATH_EMIT.get(), EnderParticle.EndermanDeathEmitProvider::new);
    	event.registerSpriteSet(EpicFightParticles.HIT_BLUNT.get(), HitBluntParticle.Provider::new);
    	event.registerSpecial(EpicFightParticles.HIT_BLADE.get(), new HitCutParticle.Provider());
    	event.registerSpriteSet(EpicFightParticles.CUT.get(), CutParticle.Provider::new);
    	event.registerSpriteSet(EpicFightParticles.NORMAL_DUST.get(), DustParticle.NormalDustProvider::new);
    	event.registerSpriteSet(EpicFightParticles.DUST_EXPANSIVE.get(), DustParticle.ExpansiveDustProvider::new);
    	event.registerSpriteSet(EpicFightParticles.DUST_CONTRACTIVE.get(), DustParticle.ContractiveDustProvider::new);
    	event.registerSpecial(EpicFightParticles.EVISCERATE.get(), new EviscerateParticle.Provider());
    	event.registerSpriteSet(EpicFightParticles.BLOOD.get(), BloodParticle.Provider::new);
    	event.registerSpriteSet(EpicFightParticles.BLADE_RUSH_SKILL.get(), BladeRushParticle.Provider::new);
    	event.registerSpecial(EpicFightParticles.GROUND_SLAM.get(), new GroundSlamParticle.Provider());
    	event.registerSpriteSet(EpicFightParticles.BREATH_FLAME.get(), EnderParticle.BreathFlameProvider::new);
    	event.registerSpecial(EpicFightParticles.FORCE_FIELD.get(), new ForceFieldParticle.Provider());
    	event.registerSpecial(EpicFightParticles.FORCE_FIELD_END.get(), new ForceFieldEndParticle.Provider());
    	event.registerSpecial(EpicFightParticles.ADRENALINE_PLAYER_BEATING.get(), new EntityAfterimageParticle.AdrenalineParticleProvider());
    	event.registerSpecial(EpicFightParticles.WHITE_AFTERIMAGE.get(), new EntityAfterimageParticle.WhiteAfterimageProvider());
    	event.registerSpecial(EpicFightParticles.LASER.get(), new LaserParticle.Provider());
    	event.registerSpecial(EpicFightParticles.NEUTRALIZE.get(), new DustParticle.ExpansiveMetaParticle.Provider());
    	event.registerSpecial(EpicFightParticles.BOSS_CASTING.get(), new DustParticle.ContractiveMetaParticle.Provider());
    	event.registerSpriteSet(EpicFightParticles.TSUNAMI_SPLASH.get(), TsunamiSplashParticle.Provider::new);
    	event.registerSpecial(EpicFightParticles.SWING_TRAIL.get(), new AnimationTrailParticle.Provider());
    	event.registerSpecial(EpicFightParticles.PROJECTILE_TRAIL.get(), new ProjectileTrailParticle.Provider());
    	event.registerSpriteSet(EpicFightParticles.FEATHER.get(), FeatherParticle.Provider::new);
    	event.registerSpecial(EpicFightParticles.AIR_BURST.get(), new AirBurstParticle.Provider());
    	event.registerSpriteSet(EpicFightParticles.ASH_DIRECTIONAL.get(), AshDirectionalParticle.Provider::new);
    	event.registerSpriteSet(EpicFightParticles.CATHARSIS.get(), CatharsisParticle.Provider::new);
    }
	
	@SubscribeEvent
	public static void epicfight$registerRenderers(RegisterRenderers event) {
		event.registerEntityRenderer(EpicFightEntityTypes.AREA_EFFECT_BREATH.get(), NoopRenderer::new);
		event.registerEntityRenderer(EpicFightEntityTypes.DROPPED_NETHER_STAR.get(), DroppedNetherStarRenderer::new);
		event.registerEntityRenderer(EpicFightEntityTypes.DEATH_HARVEST_ORB.get(), NoopRenderer::new);
		event.registerEntityRenderer(EpicFightEntityTypes.DODGE_LOCATION_INDICATOR.get(), NoopRenderer::new);
		event.registerEntityRenderer(EpicFightEntityTypes.WITHER_GHOST_CLONE.get(), WitherGhostRenderer::new);
		event.registerEntityRenderer(EpicFightEntityTypes.WITHER_SKELETON_MINION.get(), WitherSkeletonMinionRenderer::new);
		
		event.registerBlockEntityRenderer(EpicFightBlockEntities.FRACTURE.get(), FractureBlockRenderer::new);
	}
	
	/**
	 * Not directly related, but used this method to initialize {@link RenderItemBase#itemRenderer} and {@link RenderItemBase#itemInHandRenderer} because the event called right after gameRenerer created
	 * @param event
	 */
	@SubscribeEvent
	public static void epicfight$registerStage(RenderLevelStageEvent.RegisterStageEvent event) {
		RenderItemBase.initItemRenderers(Minecraft.getInstance());
	}
	
	@SubscribeEvent
	public static void epicfight$addLayers(EntityRenderersEvent.AddLayers event) {
		WearableItemLayer.clearModels();
		SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS.removeIf(ClothSimulatable::invalid);
		
		for (ClothSimulatable simOwner : SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS) {
			simOwner.getClothSimulator().getAllRunningObjects().forEach((entry) -> {
				simOwner.getClothSimulator().restart(entry.getKey());
			});
		}
	}
	
	@SubscribeEvent
	public static void epicfight$registerGuiOverlaysEvent(RegisterGuiLayersEvent event) {
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "stamina_bar"), RenderEngine.getInstance().battleModeHUD::renderStaminaBar);
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skills"), RenderEngine.getInstance().battleModeHUD::renderNormalSkills);
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "weapon_innate"), RenderEngine.getInstance().battleModeHUD::renderWeaponInnateSkill);
		event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "charging_bar"), RenderEngine.getInstance().battleModeHUD::renderChargingBar);
	}
	
	private static ResourceLocation wrapItemModelPath(ResourceLocation rl) {
		return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "item/" + rl.getPath());
	}
	
	@SubscribeEvent
	public static void registerAdditionalEvent(ModelEvent.RegisterAdditional event) {
		SkillCategory.ENUM_MANAGER.universalValues().stream().filter(skillCategory -> !skillCategory.bookIcon().equals(SkillCategory.DEFAULT_BOOK_ICON)).forEach(skillCategory -> {
			event.register(new ModelResourceLocation(wrapItemModelPath(skillCategory.bookIcon()), ModelResourceLocation.STANDALONE_VARIANT));
		});
	}
	
	@SubscribeEvent
	public static void modifyBakingResultEvent(ModelEvent.ModifyBakingResult event) {
		ModelResourceLocation skillbookLocation = new ModelResourceLocation(SkillCategory.DEFAULT_BOOK_ICON, ModelResourceLocation.INVENTORY_VARIANT);
		
		if (event.getModels().containsKey(skillbookLocation)) {
			List<ItemOverrides.BakedOverride> skillCategoryOverrides = new ArrayList<> ();
			
			SkillCategory.ENUM_MANAGER.universalValues().stream().filter(skillCategory -> !skillCategory.bookIcon().equals(SkillCategory.DEFAULT_BOOK_ICON)).sorted((c1, c2) -> {
				return Integer.compare(c2.universalOrdinal(), c1.universalOrdinal());
			}).forEach(skillCategory -> {
				ModelResourceLocation model = new ModelResourceLocation(wrapItemModelPath(skillCategory.bookIcon()), ModelResourceLocation.STANDALONE_VARIANT);
				ItemOverrides.PropertyMatcher[] propertyMatchers = new ItemOverrides.PropertyMatcher[1];
				propertyMatchers[0] = new ItemOverrides.PropertyMatcher(0, skillCategory.universalOrdinal());
				BakedModel bakedModel = event.getModelBakery().getBakedTopLevelModels().get(model);
				skillCategoryOverrides.add(new ItemOverrides.BakedOverride(propertyMatchers, bakedModel));
			});
			
			ItemOverrides overrides = event.getModels().get(skillbookLocation).getOverrides();
			overrides.overrides = skillCategoryOverrides.toArray(i -> new ItemOverrides.BakedOverride[i]);
			overrides.properties = new ResourceLocation[] {ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skill")};
		}
	}
}