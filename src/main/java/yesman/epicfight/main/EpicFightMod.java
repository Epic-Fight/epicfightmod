package yesman.epicfight.main;

import java.nio.file.Path;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.model.ItemSkins;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.screen.config.IngameConfigurationScreen;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.compat.*;
import yesman.epicfight.config.ConfigManager;
import yesman.epicfight.config.EpicFightOptions;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EpicFightDataSerializers;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.server.commands.PlayerModeCommand;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.server.commands.PlayerStaminaCommand;
import yesman.epicfight.server.commands.arguments.EpicFightCommandArgumentTypes;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.effect.EpicFightPotions;
import yesman.epicfight.world.entity.EpicFightEntities;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.decoration.EpicFightPaintingVariants;
import yesman.epicfight.world.gamerule.EpicFightGamerules;
import yesman.epicfight.world.item.EpicFightCreativeTabs;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;
import yesman.epicfight.world.level.block.EpicFightBlocks;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;

/**
 *  Changes from 20.9.5 -> 20.9.6
 *  
 *  1. Fixed skill book screen not showing up when Enhanced Visuals installed
 *  
 *  2. Fixed witches not throwing potion
 *  
 *  3. Fixed custom armors parts invisible
 *  
 *  4. Fixed a crash when selecting mob capability model in datapack editor
 *  
 *  Changes from 20.9.5 -> 20.10.1
 *  
 *  1. Enhanced the accuracy of tracing ability of the player when attacking
 *  
 *  2. Cloth simulation for cape
 *  
 *  3. Config to enable dummy cape (experimental version only)
 *  
 *  --- TO DO ---
 *  
 *  Update language files (always)
 *  
 *  Add an reach property to attack animation (idea)
 *  
 *  Add an alert function when an entity targeting the player tries grappling or execution attack
 *  
 *  Add UI for execution resistance
 *  
 *  Add functionality to blooming effect (resists wither effect)
 *  
 *  Add a screen for setting animation properties in datapack editor
 *  
 *  First person animation system by adding /data/ folder in the path, and few samples
 *  
 *  Enhance the stun system (maybe remove or barely leave knockback)
 *  
 *  Add toasts & achievements to guide beginners
 *  
 *  Add resource hashing for animation file to prevent client modifying animation by resource pack
 *  
 *  @author yesman
 */
@Mod("epicfight")
public class EpicFightMod {
	public static final String MODID = "epicfight";
	public static final String CONFIG_FILE_PATH = EpicFightMod.MODID + ".toml";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	public static EpicFightOptions CLIENT_CONFIGS;
	private static EpicFightMod instance;
	
	public static EpicFightMod getInstance() {
		return instance;
	}
	
	private Function<LivingEntityPatch<?>, Animator> animatorProvider;
	
    public EpicFightMod() {
    	instance = this;
    	
    	ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigManager.CLIENT_CONFIG);
		final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		bus.addListener(this::constructMod);
    	bus.addListener(this::doCommonStuff);
    	bus.addListener(this::addPackFindersEvent);
    	bus.addListener(this::buildCreativeTabWithSkillBooks);
    	bus.addListener(SkillManager::createSkillRegistry);
    	bus.addListener(SkillManager::registerSkills);
    	bus.addListener(EpicFightCapabilities::registerCapabilities);
    	bus.addListener(EpicFightEntities::onSpawnPlacementRegister);
    	
    	MinecraftForge.EVENT_BUS.addListener(this::command);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListnerEvent);
    	
    	LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, LivingMotions.class);
    	SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillCategories.class);
    	SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillSlots.class);
    	Style.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, Styles.class);
    	WeaponCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, WeaponCategories.class);
    	
    	EpicFightMobEffects.EFFECTS.register(bus);
    	EpicFightPotions.POTIONS.register(bus);
        EpicFightAttributes.ATTRIBUTES.register(bus);
        EpicFightCreativeTabs.TABS.register(bus);
        EpicFightItems.ITEMS.register(bus);
        EpicFightParticles.PARTICLES.register(bus);
        EpicFightEntities.ENTITIES.register(bus);
        EpicFightBlocks.BLOCKS.register(bus);
        EpicFightBlockEntities.BLOCK_ENTITIES.register(bus);
		EpicFightLootTables.LOOT_MODIFIERS.register(bus);
		EpicFightSounds.SOUNDS.register(bus);
		EpicFightDataSerializers.ENTITY_DATA_SERIALIZER.register(bus);
		EpicFightConditions.CONDITIONS.register(bus);
		SkillDataKeys.DATA_KEYS.register(bus);
		EpicFightPaintingVariants.PAINTING_VARIANTS.register(bus);
		EpicFightCommandArgumentTypes.COMMAND_ARGUMENT_TYPES.register(bus);
        
        ConfigManager.loadConfig(ConfigManager.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MODID + "-client.toml").toString());
        ConfigManager.loadConfig(ConfigManager.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE_PATH).toString());
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(IngameConfigurationScreen::new));
        ModLoadingContext.get().registerExtensionPoint(EpicFightExtensions.class, () -> new EpicFightExtensions(EpicFightCreativeTabs.ITEMS.get()));
        
    	if (ModList.get().isLoaded("geckolib")) {
			ICompatModule.loadCompatModule(GeckolibCompat.class);
		}
		
		if (ModList.get().isLoaded("azurelib")) {
			ICompatModule.loadCompatModule(AzureLibCompat.class);
		}
		
		if (ModList.get().isLoaded("azurelibarmor")) {
			ICompatModule.loadCompatModule(AzureLibArmorCompat.class);
		}
		
		if (ModList.get().isLoaded("firstperson")) {
			ICompatModule.loadCompatModule(FirstPersonCompat.class);
		}
		
		if (ModList.get().isLoaded("skinlayers3d")) {
			ICompatModule.loadCompatModule(SkinLayer3DCompat.class);
		}
		
		if (ModList.get().isLoaded("oculus")) {
			ICompatModule.loadCompatModule(IRISCompat.class);
		}
		
		if (ModList.get().isLoaded("vampirism")) {
			ICompatModule.loadCompatModule(VampirismCompat.class);
		}
        
        if (ModList.get().isLoaded("werewolves")) {
			ICompatModule.loadCompatModule(WerewolvesCompat.class);
		}
        
        if (ModList.get().isLoaded("iceandfire")) {
			ICompatModule.loadCompatModule(IceAndFireCompat.class);
		}

		if (ModList.get().isLoaded("playeranimator")) {
			ICompatModule.loadCompatModule(PlayerAnimatorCompat.class);
		}
	}
    
    /**
     * FML Lifecycle Events
     */
    private void constructMod(final FMLConstructModEvent event) {
    	event.enqueueWork(LivingMotion.ENUM_MANAGER::loadEnum);
    	event.enqueueWork(SkillCategory.ENUM_MANAGER::loadEnum);
    	event.enqueueWork(SkillSlot.ENUM_MANAGER::loadEnum);
    	event.enqueueWork(Style.ENUM_MANAGER::loadEnum);
    	event.enqueueWork(WeaponCategory.ENUM_MANAGER::loadEnum);
    }
    
	private void doCommonStuff(final FMLCommonSetupEvent event) {
		event.enqueueWork(EpicFightCommandArgumentTypes::registerArgumentTypes);
		event.enqueueWork(EpicFightPotions::addRecipes);
		event.enqueueWork(EpicFightNetworkManager::registerPackets);
		event.enqueueWork(ItemCapabilityProvider::registerWeaponTypesByClass);
		event.enqueueWork(EntityPatchProvider::registerEntityPatches);
		event.enqueueWork(EpicFightGamerules::registerRules);
		event.enqueueWork(WeaponTypeReloadListener::registerDefaultWeaponTypes);
		event.enqueueWork(EpicFightMobEffects::addOffhandModifier);
		event.enqueueWork(EpicFightLootTables::registerLootItemFunctionType);
    }
	
	/**
	 * Register Etc
	 */
	private void command(final RegisterCommandsEvent event) {
		PlayerModeCommand.register(event.getDispatcher());
		PlayerSkillCommand.register(event.getDispatcher());
		PlayerStaminaCommand.register(event.getDispatcher());
    }
	
	public void addPackFindersEvent(AddPackFindersEvent event) {
		if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path resourcePath = ModList.get().getModFileById(EpicFightMod.MODID).getFile().findResource("packs/epicfight_legacy");
            PathPackResources pack = new PathPackResources(ModList.get().getModFileById(EpicFightMod.MODID).getFile().getFileName() + ":" + resourcePath, resourcePath, false);
            Pack.ResourcesSupplier resourcesSupplier = (string) -> pack;
            Pack.Info info = Pack.readPackInfo("epicfight_legacy", resourcesSupplier);
            
            if (info != null) {
                event.addRepositorySource((source) ->
    			source.accept(Pack.create("epicfight_legacy", Component.translatable("pack.epicfight_legacy.title"), false, resourcesSupplier, info, PackType.CLIENT_RESOURCES, Pack.Position.TOP, false, PackSource.BUILT_IN)));
            }
        }
    }
	
	private void addReloadListnerEvent(final AddReloadListenerEvent event) {
		event.addListener(new ColliderPreset());
		event.addListener(new SkillManager());
		event.addListener(new WeaponTypeReloadListener());
		event.addListener(new ItemCapabilityReloadListener());
		event.addListener(new MobPatchReloadListener());
	}
	
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        	CLIENT_CONFIGS = new EpicFightOptions();
        	new ClientEngine();
        	
        	EpicFightMod.getInstance().animatorProvider = ClientAnimator::getAnimator;
    		EntityPatchProvider.registerEntityPatchesClient();
    		SkillBookScreen.registerIconItems();
    		EpicFightItemProperties.registerItemProperties();
        }
        
        @SubscribeEvent
        public static void registerResourcepackReloadListnerEvent(final RegisterClientReloadListenersEvent event) {
    		event.registerReloadListener(new JointMaskReloadListener());
    		event.registerReloadListener(Meshes.INSTANCE);
    		event.registerReloadListener(AnimationManager.getInstance());
    		event.registerReloadListener(ItemSkins.INSTANCE);
    	}
    }
	
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
    public static class ServerModEvents {
		@SubscribeEvent
		public static void doServerStuff(final FMLDedicatedServerSetupEvent event) {
			EpicFightMod.getInstance().animatorProvider = ServerAnimator::getAnimator;
		}
    }
	
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
    public static class ServerForgeEvents {
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void addReloadListnerEvent(final AddReloadListenerEvent event) {
			event.addListener(AnimationManager.getInstance());
		}
    }
	
	private void buildCreativeTabWithSkillBooks(final BuildCreativeModeTabContentsEvent event) {
		/**
		 * Accept learnable skills for each mod by {@link EpicFightExtensions#skillBookCreativeTab}.
		 * If the extension doesn't exist, add them to {@link EpicFightCreativeTabs.ITEMS} tab.
		 */
		SkillManager.getNamespaces().forEach((modid) -> {
			ModList.get().getModContainerById(modid).flatMap((mc) -> mc.getCustomExtension(EpicFightExtensions.class)).ifPresentOrElse((extension) -> {
				if (extension.skillBookCreativeTab() == event.getTab()) {
					SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && skill.getRegistryName().getNamespace() == modid).forEach((rl) -> {
						ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
						SkillBookItem.setContainingSkill(rl.toString(), stack);
						event.accept(stack);
					});
				}
			}, () -> {
				if (event.getTab() == EpicFightCreativeTabs.ITEMS.get()) {
					SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && skill.getRegistryName().getNamespace() == modid).forEach((rl) -> {
						ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
						SkillBookItem.setContainingSkill(rl.toString(), stack);
						event.accept(stack);
					});
				}
			});
		});
	}
	
	/**
	 * Epic Fight utils
	 */
	public static Animator getAnimator(LivingEntityPatch<?> entitypatch) {
		return EpicFightMod.getInstance().animatorProvider.apply(entitypatch);
	}
	
	public static boolean isPhysicalClient() {
    	return FMLEnvironment.dist == Dist.CLIENT;
    }
}