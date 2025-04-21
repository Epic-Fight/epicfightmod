package yesman.epicfight.main;

import java.nio.file.Path;
import java.util.function.BiConsumer;
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
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.SynchedAnimationVariableKeys;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.screen.config.IngameConfigurationScreen;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.compat.AzureLibArmorCompat;
import yesman.epicfight.compat.AzureLibCompat;
import yesman.epicfight.compat.CuriosCompat;
import yesman.epicfight.compat.FirstPersonCompat;
import yesman.epicfight.compat.GeckolibCompat;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.IRISCompat;
import yesman.epicfight.compat.IceAndFireCompat;
import yesman.epicfight.compat.PlayerAnimatorCompat;
import yesman.epicfight.compat.SkinLayer3DCompat;
import yesman.epicfight.compat.VampirismCompat;
import yesman.epicfight.compat.WerewolvesCompat;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.config.ServerConfig;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EpicFightDataSerializers;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.server.commands.AnimatorCommand;
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
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.item.EpicFightCreativeTabs;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;
import yesman.epicfight.world.level.block.EpicFightBlocks;
import yesman.epicfight.world.level.block.entity.EpicFightBlockEntities;

/**
 *  Major version changes
 *  20.9.7 -> 20.10.1
 *  
 *  Cloth simulation for cape
 *  Added arrow trails
 *  Added first person animations for some animations
 *  
 *  Fixed witches not throwing potion
 *  Fixed custom armors parts invisible
 *  Fixed a crash when selecting mob capability model in datapack editor
 *  Fixed a crash with better nether when player equips armors
 *  Fixed skill book screen not showing up when Enhanced Visuals installed
 *  Fixed player model jitters when flying with elytra
 *  Fixed guard skill activates rapidly when blocking vanilla attacks
 *  Enhanced the accuracy of tracing ability of attack animations
 *  
 *  ***************************************************************
 *  
 *  Minor version changes
 *  20.10.310 -> 20.10.401
 *  
 *  Added first-person animations for drink and eat animation
 *  Fixed guard consuming stamina rapidly when players block vanilla attacks
 *  Fixed arrow trail remaining after hitting a player in creative mode
 *  Corrected the shield's location when in the player's back
 *  Added {@link SkillCreateEvent.class} to modify skill builder before instantiate skills
 *  Better null check for {@link AnimationVariables.class}
 *  
 *  ***************************************************************
 *  
 *  20.10.401 -> 20.10.403
 *  
 *  Fixed multiple datapack editor crashes
 *  Fixed tooltips being hidden by the skill book screen
 *  Fixed zombies attacking each other
 *  Fixed {@link SkillCreateEvent.class} to follow generic event rule
 *  Added to register custom available weapon categories in {@link SwordmasterSkill$Builder.class}
 *  
 *  --- TO DO ---
 *  
 *  Update language files (always)
 *  Add an reach property to attack animation (idea)
 *  Add an alert function when an entity targeting the player tries grappling or execution attack
 *  Add UI for execution resistance
 *  Add functionality to blooming effect (resists wither effect)
 *  Add a screen for setting animation properties in datapack editor
 *  First person animation system by adding /data/ folder in the path, and few samples
 *  Enhance the stun system (maybe remove or barely leave knockback)
 *  Add toasts & achievements to guide beginners
 *  Add resource hashing for animation file to prevent client modifying animation by resource pack
 *  
 *  @author yesman
 */
@Mod(EpicFightMod.MODID)
public class EpicFightMod {
	public static final String MODID = "epicfight";
	public static final String EPICSKINS_MODID = "epicskins";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static void logAndStacktraceIfDevSide(BiConsumer<Logger, String> logFunction, String message, Function<String, Throwable> exceptionProvider) {
		logAndStacktraceIfDevSide(logFunction, message, exceptionProvider, message);
	}
	
	public static void logAndStacktraceIfDevSide(BiConsumer<Logger, String> logFunction, String message, Function<String, Throwable> exceptionProvider, String stackTraceMessage) {
		logFunction.accept(LOGGER, message);
		stacktraceIfDevSide(message, exceptionProvider, stackTraceMessage);
	}
	
	public static void stacktraceIfDevSide(String message, Function<String, Throwable> exceptionProvider) {
		stacktraceIfDevSide(message, exceptionProvider, message);
	}
	
	public static void stacktraceIfDevSide(String message, Function<String, Throwable> exceptionProvider, String stackTraceMessage) {
		if (exceptionProvider != null && EpicFightSharedConstants.IS_DEV_ENV) {
			exceptionProvider.apply(stackTraceMessage).printStackTrace();
		}
	}
	
    public EpicFightMod(FMLJavaModLoadingContext context) {
    	if (EpicFightSharedConstants.isPhysicalClient()) {
    		context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    	}
    	
    	if (!EpicFightSharedConstants.isPhysicalClient()) {
    		context.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    	}
    	
    	context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    	context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(IngameConfigurationScreen::new));
		context.registerExtensionPoint(EpicFightExtensions.class, () -> new EpicFightExtensions(EpicFightCreativeTabs.ITEMS.get()));
    	
		final IEventBus bus = context.getModEventBus();
		
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
		SynchedAnimationVariableKeys.SYNCHED_ANIMATION_VARIABLE_KEYS.register(bus);
		EpicFightPaintingVariants.PAINTING_VARIANTS.register(bus);
		EpicFightCommandArgumentTypes.COMMAND_ARGUMENT_TYPES.register(bus);
        
    	if (ModList.get().isLoaded("geckolib")) {
			ICompatModule.loadCompatModule(context, GeckolibCompat.class);
		}
		
		if (ModList.get().isLoaded("azurelib")) {
			ICompatModule.loadCompatModule(context, AzureLibCompat.class);
		}
		
		if (ModList.get().isLoaded("azurelibarmor")) {
			ICompatModule.loadCompatModule(context, AzureLibArmorCompat.class);
		}
		
		if (ModList.get().isLoaded("firstperson")) {
			ICompatModule.loadCompatModule(context, FirstPersonCompat.class);
		}
		
		if (ModList.get().isLoaded("skinlayers3d")) {
			ICompatModule.loadCompatModule(context, SkinLayer3DCompat.class);
		}
		
		if (ModList.get().isLoaded("oculus")) {
			ICompatModule.loadCompatModule(context, IRISCompat.class);
		}
		
		if (ModList.get().isLoaded("vampirism")) {
			ICompatModule.loadCompatModule(context, VampirismCompat.class);
		}
        
        if (ModList.get().isLoaded("werewolves")) {
			ICompatModule.loadCompatModule(context, WerewolvesCompat.class);
		}
        
        if (ModList.get().isLoaded("iceandfire")) {
			ICompatModule.loadCompatModule(context, IceAndFireCompat.class);
		}
        
        if (ModList.get().isLoaded("curios")) {
			ICompatModule.loadCompatModule(context, CuriosCompat.class);
		}

		if (ModList.get().isLoaded("playeranimator")) {
			ICompatModule.loadCompatModule(context, PlayerAnimatorCompat.class);
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
    	event.enqueueWork(() -> {
			AnimationRegistryEvent animationregistryevent = new AnimationRegistryEvent();
    		ModLoader.get().postEvent(animationregistryevent);
    		animationregistryevent.getBuilders().stream().sorted((b1, b2) -> b1.namespace().compareTo(b2.namespace())).forEach((builder) -> builder.task().accept(builder));
    	});
    }
    
	private void doCommonStuff(final FMLCommonSetupEvent event) {
		event.enqueueWork(Armatures::registerEntityTypes);
		event.enqueueWork(EpicFightCommandArgumentTypes::registerArgumentTypes);
		event.enqueueWork(EpicFightPotions::addRecipes);
		event.enqueueWork(EpicFightNetworkManager::registerPackets);
		event.enqueueWork(ItemCapabilityProvider::registerWeaponTypesByClass);
		event.enqueueWork(EntityPatchProvider::registerEntityPatches);
		event.enqueueWork(EpicFightGameRules::registerGameRules);
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
		AnimatorCommand.register(event.getDispatcher());
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
    		event.enqueueWork(EntityPatchProvider::registerEntityPatchesClient);
    		event.enqueueWork(SkillBookScreen::registerIconItems);
    		event.enqueueWork(EpicFightItemProperties::registerItemProperties);
        }
        
        @SubscribeEvent
        public static void registerResourcepackReloadListnerEvent(final RegisterClientReloadListenersEvent event) {
    		event.registerReloadListener(new JointMaskReloadListener());
    		event.registerReloadListener(Meshes.INSTANCE);
    		event.registerReloadListener(AnimationManager.getInstance());
    		event.registerReloadListener(ItemSkinsReloadListener.INSTANCE);
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
					SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && skill.getCreativeTab() == null && skill.getRegistryName().getNamespace() == modid).forEach((rl) -> {
						ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
						SkillBookItem.setContainingSkill(rl.toString(), stack);
						event.accept(stack);
					});
				}
			}, () -> {
				if (event.getTab() == EpicFightCreativeTabs.ITEMS.get()) {
					SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && skill.getCreativeTab() == null && skill.getRegistryName().getNamespace() == modid).forEach((rl) -> {
						ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
						SkillBookItem.setContainingSkill(rl.toString(), stack);
						event.accept(stack);
					});
				}
			});
		});
		
		SkillManager.getSkillNames((skill) -> skill.getCategory().learnable() && skill.getCreativeTab() == event.getTab()).forEach((rl) -> {
			ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
			SkillBookItem.setContainingSkill(rl.toString(), stack);
			event.accept(stack);
		});
	}
}