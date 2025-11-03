package yesman.epicfight.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.client.online.EpicFightServerConnectionHelper;
import yesman.epicfight.api.utils.CirculatableEnum;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.ScreenCalculations.AlignDirection;
import yesman.epicfight.client.gui.ScreenCalculations.HorizontalBasis;
import yesman.epicfight.client.gui.ScreenCalculations.VerticalBasis;
import yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen;
import yesman.epicfight.client.gui.widgets.ColorSlider;
import yesman.epicfight.main.AuthenticationHelper.AuthenticationProvider;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	// Graphic Configurations
	public static final BooleanValue SHOW_TARGET_INDICATOR = BUILDER.define("ingame.show_target_indicator", () -> true);
	public static final EnumValue<HealthBarVisibility> HEALTH_BAR_VISIBILITY = BUILDER.defineEnum("ingame.health_bar_show_option", HealthBarVisibility.HURT);
	public static final IntValue MAX_STUCK_PROJECTILES = BUILDER.defineInRange("ingame.max_hit_projectiles", 30, 0, 30);
	public static final DoubleValue AIM_HELPER_COLOR = BUILDER.defineInRange("ingame.laser_pointer_color", 0.328125D, 0.0D, 1.0D);
	public static final BooleanValue ENABLE_AIM_HELPER = BUILDER.define("ingame.enable_laser_pointer", () -> true);
	public static final BooleanValue BLOOD_EFFECTS = BUILDER.define("ingame.blood_effects", () -> true);
	public static final BooleanValue AIMING_POV_CORRECTION = BUILDER.define("ingame.aiming_correction", () -> true);
	public static final BooleanValue SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP = BUILDER.define("ingame.show_epicfight_attributes", () -> true);
	public static final BooleanValue ACTIVATE_COMPUTE_SHADER = BUILDER.define("ingame.use_compute_shader", () -> false);
	public static final BooleanValue ENABLE_ANIMATED_FIRST_PERSON_MODEL = BUILDER.define("ingame.first_person_model", () -> true);
	public static final EnumValue<BlockGuideOptions> MINE_BLOCK_GUIDE_OPTION = BUILDER.defineEnum("ingame.mine_block_guide_option", BlockGuideOptions.CROSSHAIR);
	public static final BooleanValue ENABLE_TARGET_ENTITY_GUIDE = BUILDER.define("ingame.enable_target_entity_guide", () -> true);
	public static final BooleanValue ENABLE_POV_ACTION = BUILDER.define("ingame.enable_pov_action", () -> true);
	public static final BooleanValue ENABLE_COSMETICS = BUILDER.define("ingame.enable_cosmetics", () -> true);
	public static final BooleanValue ENABLE_PLAYER_VANILLA_MODEL = BUILDER.define("ingame.enable_player_vanilla_model", () -> true);
	
	// Control Configurations
	public static final IntValue LONG_PRESS_COUNTER = BUILDER.defineInRange("ingame.long_press_count", 2, 1, 10);
	public static final BooleanValue AUTO_SWITCH_CAMERA = BUILDER.define("ingame.camera_auto_switch", () -> false);
	public static final EnumValue<KeyConflictResolveScope> KEY_CONFLICT_RESOLVE_SCOPE = BUILDER.defineEnum("ingame.key_conflict_resolve_scope", KeyConflictResolveScope.INTERACTION);
	public static final EnumValue<PreferenceWork> PREFERENCE_WORK = BUILDER.defineEnum("ingame.preference_work", PreferenceWork.ADAPTIVE);
	public static final ConfigValue<List<? extends String>> BATTLE_MODE_SWITCHING_ITEMS = BUILDER.defineList("ingame.combat_preferred_items", Lists.newArrayList(), (element) -> {
		if (element instanceof String str) {
			return str.contains(":");
		}
		
		return false;
	});
	public static final ConfigValue<List<? extends String>> MINING_MODE_SWITCHING_ITEMS = BUILDER.defineList("ingame.mining_preferred_items", Lists.newArrayList(), (element) -> {
		if (element instanceof String str) {
			return str.contains(":");
		}
		
		return false;
	});
	
	// UI Configurations
	public static final ConfigValue<Integer> STAMINA_BAR_X = BUILDER.define("ingame.ui.stamina_bar_x", 120);
	public static final ConfigValue<Integer> STAMINA_BAR_Y = BUILDER.define("ingame.ui.stamina_bar_y", 10);
	public static final EnumValue<HorizontalBasis> STAMINA_BAR_BASE_X = BUILDER.defineEnum("ingame.ui.stamina_bar_x_base", HorizontalBasis.RIGHT);
	public static final EnumValue<VerticalBasis> STAMINA_BAR_BASE_Y = BUILDER.defineEnum("ingame.ui.stamina_bar_y_base", VerticalBasis.BOTTOM);
	
	public static final ConfigValue<Integer> WEAPON_INNATE_X = BUILDER.define("ingame.ui.weapon_innate_x", 42);
	public static final ConfigValue<Integer> WEAPON_INNATE_Y = BUILDER.define("ingame.ui.weapon_innate_y", 48);
	public static final EnumValue<HorizontalBasis> WEAPON_INNATE_BASE_X = BUILDER.defineEnum("ingame.ui.weapon_innate_x_base", HorizontalBasis.RIGHT);
	public static final EnumValue<VerticalBasis> WEAPON_INNATE_BASE_Y = BUILDER.defineEnum("ingame.ui.weapon_innate_y_base", VerticalBasis.BOTTOM);
	
	public static final ConfigValue<Integer> PASSIVE_X = BUILDER.define("ingame.ui.passives_x", 70);
	public static final ConfigValue<Integer> PASSIVE_Y = BUILDER.define("ingame.ui.passives_y", 36);
	public static final EnumValue<HorizontalBasis> PASSIVE_BASE_X = BUILDER.defineEnum("ingame.ui.passives_x_base", HorizontalBasis.RIGHT);
	public static final EnumValue<VerticalBasis> PASSIVE_BASE_Y = BUILDER.defineEnum("ingame.ui.passives_y_base", VerticalBasis.BOTTOM);
	public static final EnumValue<AlignDirection> PASSIVE_ALIGN_DIRECTION = BUILDER.defineEnum("ingame.ui.passives_align_direction", AlignDirection.HORIZONTAL);
	
	public static final ConfigValue<Integer> CHARGING_BAR_X = BUILDER.define("ingame.ui.charging_bar_x", -119);
	public static final ConfigValue<Integer> CHARGING_BAR_Y = BUILDER.define("ingame.ui.charging_bar_y", 60);
	public static final EnumValue<HorizontalBasis> CHARGING_BAR_BASE_X = BUILDER.defineEnum("ingame.ui.charging_bar_x_base", HorizontalBasis.CENTER);
	public static final EnumValue<VerticalBasis> CHARGING_BAR_BASE_Y = BUILDER.defineEnum("ingame.ui.charging_bar_y_base", VerticalBasis.CENTER);
	
	// Epic Skins Tokens
	public static final ForgeConfigSpec.ConfigValue<String> ACCESS_TOKEN = BUILDER.comment("Login information for epic fight patron server. Do not change these values manually").define("access_token", "");
	public static final ForgeConfigSpec.ConfigValue<String> REFRESH_TOKNE = BUILDER.define("refresh_token", "");
	public static final ForgeConfigSpec.EnumValue<AuthenticationProvider> PROVIDER = BUILDER.defineEnum("provider", AuthenticationProvider.NULL);
	
	// Config Spec
	public static final ForgeConfigSpec SPEC = BUILDER.build();
	
	// Graphic Config Values
	public static int maxStuckProjectiles;
	public static double aimHelperColor;
	public static int aimHelperPackedColor = 0xFFFFFFFF;
	public static boolean enableAimHelper;
	public static boolean bloodEffects;
	public static boolean aimingPovCorrection;
	public static boolean showEpicFightAttributesInTooltip;
	public static boolean activateComputeShader;
	public static boolean enableAnimatedFirstPersonModel;
	public static BlockGuideOptions mineBlockGuideOption;
	public static boolean enableTargetEntityGuide;
	public static boolean enablePovAction;
	public static boolean enableCosmetics;
	public static boolean enableOriginalModel;
	
	// Control Config Values
	public static int longPressCounter;
	public static boolean authSwitchCamera;
	public static KeyConflictResolveScope keyConflictResolveScope;
	public static PreferenceWork preferenceWork;
	
	public static Set<Item> combatPreferredItems;
	public static Set<Item> miningPreferredItems;
	
	// UI Config value
	public static boolean showTargetIndicator;
	public static HealthBarVisibility healthBarVisibility;
	public static int staminaBarX;
	public static int staminaBarY;
	public static HorizontalBasis staminaBarBaseX;
	public static VerticalBasis staminaBarBaseY;
	public static int weaponInnateX;
	public static int weaponInnateY;
	public static HorizontalBasis weaponInnateBaseX;
	public static VerticalBasis weaponInnateBaseY;
	public static int passiveX;
	public static int passiveY;
	public static HorizontalBasis passiveBaseX;
	public static VerticalBasis passiveBaseY;
	public static AlignDirection passiveAlignDirection;
	public static int chargingBarX;
	public static int chargingBarY;
	public static HorizontalBasis chargingBarBaseX;
	public static VerticalBasis chargingBarBaseY;
	
	@SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
		if (event.getConfig().getType() != ModConfig.Type.CLIENT) {
			return;
		}
		
		maxStuckProjectiles = MAX_STUCK_PROJECTILES.get();
		aimHelperColor = AIM_HELPER_COLOR.get();
		aimHelperPackedColor = ColorSlider.rgbColor(aimHelperColor);
		enableAimHelper = ENABLE_AIM_HELPER.get();
		bloodEffects = BLOOD_EFFECTS.get();
		aimingPovCorrection = AIMING_POV_CORRECTION.get();
		showEpicFightAttributesInTooltip = SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get();
		activateComputeShader = ACTIVATE_COMPUTE_SHADER.get();
		enableAnimatedFirstPersonModel = ENABLE_ANIMATED_FIRST_PERSON_MODEL.get();
		mineBlockGuideOption = MINE_BLOCK_GUIDE_OPTION.get();
		enableTargetEntityGuide = ENABLE_TARGET_ENTITY_GUIDE.get();
		enablePovAction = ENABLE_POV_ACTION.get();
		enableCosmetics = ENABLE_COSMETICS.get();
		enableOriginalModel = ENABLE_PLAYER_VANILLA_MODEL.get();
		
		longPressCounter = LONG_PRESS_COUNTER.get();
		authSwitchCamera = AUTO_SWITCH_CAMERA.get();
		keyConflictResolveScope = KEY_CONFLICT_RESOLVE_SCOPE.get();
		preferenceWork = PREFERENCE_WORK.get();
		
		combatPreferredItems = BATTLE_MODE_SWITCHING_ITEMS.get().stream()
				.map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
				.collect(Collectors.toSet());
		miningPreferredItems = MINING_MODE_SWITCHING_ITEMS.get().stream()
				.map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
				.collect(Collectors.toSet());
		
		if (combatPreferredItems.isEmpty() && miningPreferredItems.isEmpty()) {
			ItemsPreferenceScreen.resetItems();
		}
		
		showTargetIndicator = SHOW_TARGET_INDICATOR.get();
		healthBarVisibility = HEALTH_BAR_VISIBILITY.get();
		staminaBarX = STAMINA_BAR_X.get();
		staminaBarY = STAMINA_BAR_Y.get();
		staminaBarBaseX = STAMINA_BAR_BASE_X.get();
		staminaBarBaseY = STAMINA_BAR_BASE_Y.get();
		weaponInnateX = WEAPON_INNATE_X.get();
		weaponInnateY = WEAPON_INNATE_Y.get();
		weaponInnateBaseX = WEAPON_INNATE_BASE_X.get();
		weaponInnateBaseY = WEAPON_INNATE_BASE_Y.get();
		passiveX = PASSIVE_X.get();
		passiveY = PASSIVE_Y.get();
		passiveBaseX = PASSIVE_BASE_X.get();
		passiveBaseY = PASSIVE_BASE_Y.get();
		passiveAlignDirection = PASSIVE_ALIGN_DIRECTION.get();
		chargingBarX = CHARGING_BAR_X.get();
		chargingBarY = CHARGING_BAR_Y.get();
		chargingBarBaseX = CHARGING_BAR_BASE_X.get();
		chargingBarBaseY = CHARGING_BAR_BASE_Y.get();
		
		if (EpicFightServerConnectionHelper.init(event.getConfig().getFullPath().getParent().toString())) {
			EpicFightMod.LOGGER.info("Epic Fight web server connection helper: supported");
			
    		try {
    			// Try loading epic skins code dynamically
    			Class.forName("yesman.epicfight.epicskins.user.AuthenticationHelperImpl");
    		} catch (Exception e) {
    			EpicFightMod.LOGGER.info("Epic Fight web server status: Failed at initializing Authentication provider: " + e);
    		}
		} else {
			EpicFightMod.LOGGER.info("Epic Fight web server connection helper: unsupported");
		}
		
		if (EpicFightServerConnectionHelper.supported() && ClientEngine.getInstance().getAuthHelper().valid()) {
			ClientEngine.getInstance().getAuthHelper().initialize(ACCESS_TOKEN, REFRESH_TOKNE, PROVIDER);
		}
    }
	
	public static void saveChanges() {
		if (maxStuckProjectiles != MAX_STUCK_PROJECTILES.get()) MAX_STUCK_PROJECTILES.set(maxStuckProjectiles);
		if (aimHelperColor != AIM_HELPER_COLOR.get()) { AIM_HELPER_COLOR.set(aimHelperColor); aimHelperPackedColor = ColorSlider.rgbColor(aimHelperColor); }
		if (enableAimHelper != ENABLE_AIM_HELPER.get()) ENABLE_AIM_HELPER.set(enableAimHelper);
		if (bloodEffects != BLOOD_EFFECTS.get()) BLOOD_EFFECTS.set(bloodEffects);
		if (aimingPovCorrection != AIMING_POV_CORRECTION.get()) AIMING_POV_CORRECTION.set(aimingPovCorrection);
		if (showEpicFightAttributesInTooltip != SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get()) SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.set(showEpicFightAttributesInTooltip);
		if (activateComputeShader != ACTIVATE_COMPUTE_SHADER.get()) ACTIVATE_COMPUTE_SHADER.set(activateComputeShader);
		if (enableAnimatedFirstPersonModel != ENABLE_ANIMATED_FIRST_PERSON_MODEL.get()) ENABLE_ANIMATED_FIRST_PERSON_MODEL.set(enableAnimatedFirstPersonModel);
		if (mineBlockGuideOption != MINE_BLOCK_GUIDE_OPTION.get()) MINE_BLOCK_GUIDE_OPTION.set(mineBlockGuideOption);
		if (enableTargetEntityGuide != ENABLE_TARGET_ENTITY_GUIDE.get()) ENABLE_TARGET_ENTITY_GUIDE.set(enableTargetEntityGuide);
		if (enablePovAction != ENABLE_POV_ACTION.get()) ENABLE_POV_ACTION.set(enablePovAction);
		if (enableCosmetics != ENABLE_COSMETICS.get()) ENABLE_COSMETICS.set(enableCosmetics);
		if (enableOriginalModel != ENABLE_PLAYER_VANILLA_MODEL.get()) ENABLE_PLAYER_VANILLA_MODEL.set(enableOriginalModel);
		
		if (longPressCounter != LONG_PRESS_COUNTER.get()) LONG_PRESS_COUNTER.set(longPressCounter);
		if (authSwitchCamera != AUTO_SWITCH_CAMERA.get()) AUTO_SWITCH_CAMERA.set(authSwitchCamera);
		if (keyConflictResolveScope != KEY_CONFLICT_RESOLVE_SCOPE.get()) KEY_CONFLICT_RESOLVE_SCOPE.set(keyConflictResolveScope);
		if (preferenceWork != PREFERENCE_WORK.get()) PREFERENCE_WORK.set(preferenceWork);
		
		if (!combatPreferredItems.equals(BATTLE_MODE_SWITCHING_ITEMS.get().stream()
				.map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
				.collect(Collectors.toSet()))
		) {
			BATTLE_MODE_SWITCHING_ITEMS.set(combatPreferredItems.stream().map((item) -> ForgeRegistries.ITEMS.getKey(item).toString()).collect(Collectors.toList()));
		}
		if (!miningPreferredItems.equals(MINING_MODE_SWITCHING_ITEMS.get().stream()
				.map(itemName -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(itemName)))
				.collect(Collectors.toSet()))
		) {
			MINING_MODE_SWITCHING_ITEMS.set(miningPreferredItems.stream().map((item) -> ForgeRegistries.ITEMS.getKey(item).toString()).collect(Collectors.toList()));
		}
		
		if (showTargetIndicator != SHOW_TARGET_INDICATOR.get()) SHOW_TARGET_INDICATOR.set(showTargetIndicator);
		if (healthBarVisibility != HEALTH_BAR_VISIBILITY.get()) HEALTH_BAR_VISIBILITY.set(healthBarVisibility);
		if (staminaBarX != STAMINA_BAR_X.get()) STAMINA_BAR_X.set(staminaBarX);
		if (staminaBarY != STAMINA_BAR_Y.get()) STAMINA_BAR_Y.set(staminaBarY);
		if (staminaBarBaseX != STAMINA_BAR_BASE_X.get()) STAMINA_BAR_BASE_X.set(staminaBarBaseX);
		if (staminaBarBaseY != STAMINA_BAR_BASE_Y.get()) STAMINA_BAR_BASE_Y.set(staminaBarBaseY);
		if (weaponInnateX != WEAPON_INNATE_X.get()) WEAPON_INNATE_X.set(weaponInnateX);
		if (weaponInnateX != WEAPON_INNATE_Y.get()) WEAPON_INNATE_Y.set(weaponInnateY);
		if (weaponInnateBaseX != WEAPON_INNATE_BASE_X.get()) WEAPON_INNATE_BASE_X.set(weaponInnateBaseX);
		if (weaponInnateBaseY != WEAPON_INNATE_BASE_Y.get()) WEAPON_INNATE_BASE_Y.set(weaponInnateBaseY);
		if (passiveX != PASSIVE_X.get()) PASSIVE_X.set(passiveX);
		if (passiveY != PASSIVE_Y.get()) PASSIVE_Y.set(passiveY);
		if (passiveBaseX != PASSIVE_BASE_X.get()) PASSIVE_BASE_X.set(passiveBaseX);
		if (passiveBaseY != PASSIVE_BASE_Y.get()) PASSIVE_BASE_Y.set(passiveBaseY);
		if (passiveAlignDirection != PASSIVE_ALIGN_DIRECTION.get()) PASSIVE_ALIGN_DIRECTION.set(passiveAlignDirection);
		if (chargingBarX != CHARGING_BAR_X.get()) CHARGING_BAR_X.set(chargingBarX);
		if (chargingBarY != CHARGING_BAR_Y.get()) CHARGING_BAR_Y.set(chargingBarY);
		if (chargingBarBaseX != CHARGING_BAR_BASE_X.get()) CHARGING_BAR_BASE_X.set(chargingBarBaseX);
		if (chargingBarBaseY != CHARGING_BAR_BASE_Y.get()) CHARGING_BAR_BASE_Y.set(chargingBarBaseY);
	}
	
	public static Vec2i getStaminaPosition(int width, int height) {
		int posX = staminaBarBaseX.positionGetter.apply(width, staminaBarX);
		int posY = staminaBarBaseY.positionGetter.apply(height, staminaBarY);
		return new Vec2i(posX, posY);
	}
	
	public static Vec2i getWeaponInnatePosition(int width, int height) {
		int posX = weaponInnateBaseX.positionGetter.apply(width, weaponInnateX);
		int posY = weaponInnateBaseY.positionGetter.apply(height, weaponInnateY);
		return new Vec2i(posX, posY);
	}
	
	public static Vec2i getChargingBarPosition(int width, int height) {
		int posX = chargingBarBaseX.positionGetter.apply(width, chargingBarX);
		int posY = chargingBarBaseY.positionGetter.apply(height, chargingBarY);
		return new Vec2i(posX, posY);
	}
	
	/**
	 * Determines which entities should show the health bar
	 * 
	 * NONE: none of entities show the health bar
	 * HURT: entities whose health is lower than max health show the health bar
	 * TARGET: an entity that the player is targeting shows the health bar
	 * TARGET_AND_HURT: entities that meet both the HURT and TARGET conditions show the health bar
	 */
	@OnlyIn(Dist.CLIENT)
	public enum HealthBarVisibility implements CirculatableEnum<HealthBarVisibility>, StringRepresentable {
		NONE, HURT, TARGET, TARGET_AND_HURT;
		
		@Override
		public HealthBarVisibility nextEnum() {
			return HealthBarVisibility.values()[(this.ordinal() + 1) % 4];
		}

		@Override
		public String getSerializedName() {
			return ParseUtil.toLowerCase(this.name());
		}
	}
	
	/**
	 * Determines which indicators are activated for block mining guide
	 * 
	 * NONE: nothing
	 * CROSSHAIR : crosshair changes when player looks at the block with mining preferred item
	 * HIGHLIGHT : block flashes white when player looks at the block with mining preferred item
	 * CROSSHAIR_AND_HIGHLIGHT : both
	 */
	@OnlyIn(Dist.CLIENT)
	public enum BlockGuideOptions implements CirculatableEnum<BlockGuideOptions>, StringRepresentable {
		NONE(false, false), CROSSHAIR(true, false), HIGHLIGHT(false, true), CROSSHAIR_AND_HIGHLIGHT(true, true);
		
		boolean showCrosshair;
		boolean showBlockHighlight;
		
		BlockGuideOptions(boolean showCrosshair, boolean showBlockHighlight) {
			this.showCrosshair = showCrosshair;
			this.showBlockHighlight = showBlockHighlight;
		}
		
		public boolean switchCrosshair() {
			return this.showCrosshair;
		}
		
		public boolean showBlockHighlight() {
			return this.showBlockHighlight;
		}
		
		@Override
		public BlockGuideOptions nextEnum() {
			return BlockGuideOptions.values()[(this.ordinal() + 1) % 4];
		}
		
		@Override
		public String getSerializedName() {
			return ParseUtil.toLowerCase(this.name());
		}
	}
	
	/**
	 * The scope of vanilla actions that will be canceled when they conflict with Epic Fight keybinds (currently, it only supports mouse right button)
	 * 
	 * NONE: nothing
	 * BLOCK_INTERACTION : cancel block interactions (like furnace, crafting table)
	 * ITEM_INTERACTION : cancel item interactions (like plowing using a hoe)
	 * BLOCK_AND_ITEM_INTERACTION : both
	 */
	@OnlyIn(Dist.CLIENT)
	public enum KeyConflictResolveScope implements CirculatableEnum<KeyConflictResolveScope>, StringRepresentable {
		NONE(false, false), INTERACTION(true, false), ITEM_USE(false, true), INTERACTION_AND_ITEMUSE(true, true);
		
		boolean cancelInteraction;
		boolean cancelItemUse;
		
		KeyConflictResolveScope(boolean cancelBlockInteraction, boolean cancelItemInteraction) {
			this.cancelInteraction = cancelBlockInteraction;
			this.cancelItemUse = cancelItemInteraction;
		}
		
		public boolean cancelInteraction() {
			return this.cancelInteraction;
		}
		
		public boolean cancelItemUse() {
			return this.cancelItemUse;
		}
		
		@Override
		public KeyConflictResolveScope nextEnum() {
			return KeyConflictResolveScope.values()[(this.ordinal() + 1) % 4];
		}
		
		@Override
		public String getSerializedName() {
			return ParseUtil.toLowerCase(this.name());
		}
	}
	
	/**
	 * Determines how item preference works
	 * 
	 * ADAPTIVE: Decides the next action based on crosshair hit result and target
	 * SWITCH_MODE: Switches the player mode to each categorized preference, forcing the player to do only mine or attack.
	 */
	@OnlyIn(Dist.CLIENT)
	public enum PreferenceWork implements CirculatableEnum<PreferenceWork>, StringRepresentable {
		ADAPTIVE(true), SWITCH_MODE(false);
		
		boolean checkHitResult;
		
		PreferenceWork(boolean checkHitResult) {
			this.checkHitResult = checkHitResult;
		}
		
		public boolean checkHitResult() {
			return this.checkHitResult;
		}
		
		@Override
		public String getSerializedName() {
			return ParseUtil.toLowerCase(this.name());
		}
		
		@Override
		public PreferenceWork nextEnum() {
			return PreferenceWork.values()[(this.ordinal() + 1) % 2];
		}
	}
}