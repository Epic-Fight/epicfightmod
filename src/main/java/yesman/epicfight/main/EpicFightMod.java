package yesman.epicfight.main;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.screen.config.IngameConfigurationScreen;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.compat.AzureLibArmorCompat;
import yesman.epicfight.compat.AzureLibCompat;
import yesman.epicfight.compat.CuriosCompat;
import yesman.epicfight.compat.FirstPersonCompat;
import yesman.epicfight.compat.GeckolibCompat;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.IRISCompat;
import yesman.epicfight.compat.IceAndFireCompat;
import yesman.epicfight.compat.PlayerAnimatorCompat;
import yesman.epicfight.compat.PlayerReviveCompat;
import yesman.epicfight.compat.SkinLayer3DCompat;
import yesman.epicfight.compat.VampirismCompat;
import yesman.epicfight.compat.WerewolvesCompat;
import yesman.epicfight.compat.betterthirdperson.BetterThirdPersonCompat;
import yesman.epicfight.compat.fgm.WildfireFGMCompat;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.config.ServerConfig;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.EntityPairingPacketType;
import yesman.epicfight.network.EntityPairingPacketTypes;
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
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
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
 *  --- Future list ---
 *  
 *  Update language files (always)
 *  Add an alert function when an entity targeting the player tries grappling or execution attack
 *  Add UI for execution resistance
 *  Add functionality to blooming effect (resists wither effect)
 *  Add a screen for setting animation properties in datapack editor
 *  Enhance the stun system (maybe remove or barely leave knockback)
 *  
 *  @author yesman
 */
@Mod(EpicFightMod.MODID)
public class EpicFightMod {
	public static final String MODID = "epicfight";
	public static final String EPICSKINS_MODID = "epicskins";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	
	public static String prefix(String s) {
		return String.format("%s:%s", MODID, s);
	}
	
	public static String format(String s) {
		return String.format(s, MODID);
	}
	
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
		context.registerExtensionPoint(EpicFightExtensions.class, () -> new EpicFightExtensions(EpicFightCreativeTabs.ITEMS));
    	
		final IEventBus bus = context.getModEventBus();
		
		bus.addListener(this::constructMod);
    	bus.addListener(this::doCommonStuff);
    	bus.addListener(this::addPackFindersEvent);
    	bus.addListener(this::buildCreativeTabWithSkillBooks);
    	bus.addListener(SkillManager::createSkillRegistry);
    	bus.addListener(SkillManager::registerSkills);
    	bus.addListener(EpicFightCapabilities::registerCapabilities);
    	bus.addListener(EpicFightEntities::onSpawnPlacementRegister);
    	
    	if (EpicFightSharedConstants.isPhysicalClient()) {
			bus.addListener(ComputeShaderProvider::epicfight$registerComputeShaders);
		}
    	
    	MinecraftForge.EVENT_BUS.addListener(this::command);
        MinecraftForge.EVENT_BUS.addListener(this::addReloadListnerEvent);
    	
    	LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, LivingMotions.class);
    	SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillCategories.class);
    	SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, SkillSlots.class);
    	Style.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, Styles.class);
    	WeaponCategory.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, WeaponCategories.class);
    	Faction.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, Factions.class);
    	EntityPairingPacketType.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, EntityPairingPacketTypes.class);
        if (EpicFightSharedConstants.isPhysicalClient()) {
            InputAction.ENUM_MANAGER.registerEnumCls(EpicFightMod.MODID, EpicFightInputAction.class);
            InputAction.ENUM_MANAGER.registerEnumCls("minecraft", MinecraftInputAction.class);
        }
    	
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
            String test = ModList.get().getModFileById("azurelib").versionString();
            int dotIndex = test.indexOf(".");
            int majorVersion = Integer.parseInt(test.substring(0, dotIndex));
            LogUtils.getLogger().debug(test);
            if (majorVersion < 3)
                ICompatModule.loadCompatModule(context, AzureLibCompat.class);
            else
                LogUtils.getLogger().warn("Azure Lib version {} is not supported yet.", majorVersion);
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

        if (ModList.get().isLoaded("betterthirdperson")) {
            ICompatModule.loadCompatModule(context, BetterThirdPersonCompat.class);
        }
        
        if (ModList.get().isLoaded("playerrevive")) {
        	ICompatModule.loadCompatModule(context, PlayerReviveCompat.class);
        }

		if (ModList.get().isLoaded("wildfire_gender"))
		{
			ICompatModule.loadCompatModule(context, WildfireFGMCompat.class);
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
    	event.enqueueWork(Faction.ENUM_MANAGER::loadEnum);
    	event.enqueueWork(EntityPairingPacketType.ENUM_MANAGER::loadEnum);
        if (EpicFightSharedConstants.isPhysicalClient()) {
            event.enqueueWork(InputAction.ENUM_MANAGER::loadEnum);
        }
    	event.enqueueWork(() -> {
    		AnimationManager.addNoWarningModId(EPICSKINS_MODID);
			AnimationRegistryEvent animationregistryevent = new AnimationRegistryEvent();
    		ModLoader.get().postEvent(animationregistryevent);
    		animationregistryevent.getBuilders().stream().sorted(Comparator.comparing(AnimationManager.AnimationBuilder::namespace)).forEach((builder) -> builder.task().accept(builder));
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
		event.addListener(new ItemKeywordReloadListener());
		event.addListener(new ItemCapabilityReloadListener());
		event.addListener(new MobPatchReloadListener());
	}
	
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        	event.enqueueWork(ComputeShaderProvider::checkIfSupports);
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
				if (extension.skillBookCreativeTab().get() == event.getTab()) {
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

	/// Creates an identifier that points to an Epic Fight resource.
	///
	/// This was called `identifier` and not `resourceLocation` since [Mojang renamed `ResourceLocation` to `Identifier` in 1.21.11](https://neoforged.net/news/21.11release/#renaming-of-resourcelocation-to-identifier).
	public static @NotNull ResourceLocation identifier(@NotNull String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	/// @deprecated Use [#identifier(String)] instead. [Mojang renamed `ResourceLocation` to `Identifier` in 1.21.11](https://neoforged.net/news/21.11release/#renaming-of-resourcelocation-to-identifier).
	@Deprecated(forRemoval = true)
	public static @NotNull ResourceLocation rl(@NotNull String path) {
		return identifier(path);
	}
}