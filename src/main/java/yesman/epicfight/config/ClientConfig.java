package yesman.epicfight.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.utils.CirculatableEnum;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.api.utils.ColorUtils;
import yesman.epicfight.client.online.EpicFightServerConnectionHelper;
import yesman.epicfight.main.AuthenticationHelper.AuthenticationProvider;
import yesman.epicfight.main.EpicFightMod;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // UI
    public static final BooleanValue SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP = BUILDER.define("ingame.show_epicfight_attributes", () -> true);
    public static final DoubleValue TARGET_OUTLINE_COLOR = BUILDER.defineInRange("ingame.target_outline_color", 0.0D, 0.0D, 1.0D);
    public static final EnumValue<BlockGuideOptions> MINE_BLOCK_GUIDE_OPTION = BUILDER.defineEnum("ingame.mine_block_guide_option", BlockGuideOptions.CROSSHAIR);
    public static final BooleanValue ENABLE_TARGET_ENTITY_GUIDE = BUILDER.define("ingame.enable_target_entity_guide", () -> true);

    // Graphics
    public static final BooleanValue BLOOD_EFFECTS = BUILDER.define("ingame.blood_effects", () -> true);
    public static final BooleanValue ACTIVATE_COMPUTE_SHADER = BUILDER.define("ingame.use_compute_shader", () -> false);
    public static final BooleanValue ACTIVATE_PERSISTENT_BUFFER = BUILDER.define("ingame.use_persistent_buffer", () -> false);
    public static final BooleanValue GROUND_SLAMS = BUILDER.define("ingame.ground_slams", () -> true);

    // Model
    public static final IntValue MAX_STUCK_PROJECTILES = BUILDER.defineInRange("ingame.max_hit_projectiles", 30, 0, 30);
    public static final BooleanValue ENABLE_ANIMATED_FIRST_PERSON_MODEL = BUILDER.define("ingame.first_person_model", () -> true);
    public static final BooleanValue ENABLE_PLAYER_VANILLA_MODEL = BUILDER.define("ingame.enable_player_vanilla_model", () -> true);
    public static final BooleanValue ENABLE_COSMETICS = BUILDER.define("ingame.enable_cosmetics", () -> true);

    // Camera
    public static final BooleanValue ENABLE_FIRST_PERSON_CAMERA_MOVE = BUILDER.define("ingame.enable_pov_action", () -> true);
    public static final ConfigValue<TPSActivationType> TPS_TYPE = BUILDER.defineEnum("ingame.camera.camera_mode", TPSActivationType.ON_AIMING);
    public static final IntValue CAMERA_HORIZONTAL_LOCATION = BUILDER.defineInRange("ingame.camera.horizontal_location", -5, -10, 10);
    public static final IntValue CAMERA_VERTICAL_LOCATION = BUILDER.defineInRange("ingame.camera.vertical_location", 0, -2, 5);
    public static final IntValue CAMERA_ZOOM = BUILDER.defineInRange("ingame.camera.zoom", 3, 6, 10);
    public static final BooleanValue LOCK_ON_SNAPPING = BUILDER.define("ingame.camera.lock_on_quick_shift", () -> true);
    public static final IntValue ENTITY_FOCUSING_RANGE = BUILDER.defineInRange("ingame.camera.lock_on_range", 20, 5, 25);

    // Controls
    public static final IntValue HOLDING_THRESHOLD = BUILDER.defineInRange("ingame.long_press_count", 2, 1, 10);
    public static final BooleanValue AUTO_PERSPECTIVE_SWITCHING = BUILDER.define("ingame.camera_auto_switch", () -> false);
    public static final EnumValue<CanceledVanillaActions> CANCEL_VANILLA_ACTION = BUILDER.defineEnum("ingame.key_conflict_resolve_scope", CanceledVanillaActions.INTERACTION);
    public static final EnumValue<PlayerBehaviorStrategy> PLAYER_BEHAVIOR_STRATEGY = BUILDER.defineEnum("ingame.preference_work", PlayerBehaviorStrategy.ADAPTIVE);
    public static final EnumValue<CameraPerspectiveToggleMode> CAMERA_PERSPECTIVE_TOGGLE_MODE = BUILDER
            .comment("""
                    Defines how the camera toggles perspectives.
                    
                        1. Vanilla (Default)
                           Uses Minecraft's default behavior.
                           Cycles through all available perspectives.
                    
                        2. Skip Third-Person Front Perspective
                           Skips only the front view when toggling.
                           Other perspectives remain available.
                    """)
            .defineEnum("ingame.camera_perspective_toggle_mode", CameraPerspectiveToggleMode.VANILLA);

    public static final ConfigValue<List<? extends String>> COMBAT_CATEGORIZED_ITEMS = BUILDER.defineList("ingame.combat_preferred_items", Lists.newArrayList(), null, (element) -> {
        if (element instanceof String str) {
            return str.contains(":");
        }

        return false;
    });

    public static final ConfigValue<List<? extends String>> MINING_CATEGORIZED_ITEMS = BUILDER.defineListAllowEmpty("ingame.mining_preferred_items", Lists.newArrayList(), null, (element) -> {
        if (element instanceof String str) {
            return str.contains(":");
        }

        return false;
    });

    //Epic Skins Tokens
    public static final ModConfigSpec.ConfigValue<String> ACCESS_TOKEN = BUILDER.comment("Login information for epic fight patron server. Do not change these values manually").define("access_token", "");
    public static final ModConfigSpec.ConfigValue<String> REFRESH_TOKNE = BUILDER.define("refresh_token", "");
    public static final ModConfigSpec.EnumValue<AuthenticationProvider> PROVIDER = BUILDER.defineEnum("provider", AuthenticationProvider.NULL);

    // Config Spec
    public static final ModConfigSpec SPEC = BUILDER.build();

    // Config values

    // Graphic Config values
    public static boolean bloodEffects;
    public static boolean activateComputeShader;
    public static boolean activatePersistentBuffer;
    public static boolean groundSlams;

    // Model Config values
    public static int maxStuckProjectiles;
    public static boolean enableAnimatedFirstPersonModel;
    public static boolean enableOriginalModel;
    public static boolean enableCosmetics;

    // Camera Config values
    public static boolean enableFirstPersonCameraMove;

    /** Use {@link #getTpsActivationType()} to handle null */
    @Deprecated @ApiStatus.Internal
    public static TPSActivationType tpsType;
    public static int cameraHorizontalLocation;
    public static int cameraVerticalLocation;
    public static int cameraZoom;
    public static int entityFocusingRange;
    public static boolean lockOnSnapping;

    // Control Config values
    public static int holdingThreshold;
    public static boolean autoPerspectiveSwithing;
    public static CanceledVanillaActions canceledVanillaActions;
    public static PlayerBehaviorStrategy playerBehaviorStrategy;
    public static CameraPerspectiveToggleMode cameraPerspectiveToggleMode;
    public static Set<Item> combatCategorizedItems;
    public static Set<Item> miningCategorizedItems;

    // UI Config values
    public static boolean showEpicFightAttributesInTooltip;
    public static double targetOutlineColor;
    public static int packedTargetOutlineColor = 0xFFFFFFFF;
    public static BlockGuideOptions mineBlockGuideOption;
    public static boolean enableTargetEntityGuide;

    @SubscribeEvent
    static void epicfight$modConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getType() != ModConfig.Type.CLIENT) {
            return;
        }

        maxStuckProjectiles = MAX_STUCK_PROJECTILES.get();
        targetOutlineColor = TARGET_OUTLINE_COLOR.get();
        packedTargetOutlineColor = ColorUtils.hue(targetOutlineColor);
        bloodEffects = BLOOD_EFFECTS.get();
        showEpicFightAttributesInTooltip = SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get();
        activateComputeShader = ACTIVATE_COMPUTE_SHADER.get();
        activatePersistentBuffer = ACTIVATE_PERSISTENT_BUFFER.get();
        groundSlams = GROUND_SLAMS.get();
        enableAnimatedFirstPersonModel = ENABLE_ANIMATED_FIRST_PERSON_MODEL.get();
        mineBlockGuideOption = MINE_BLOCK_GUIDE_OPTION.get();
        enableTargetEntityGuide = ENABLE_TARGET_ENTITY_GUIDE.get();
        enableFirstPersonCameraMove = ENABLE_FIRST_PERSON_CAMERA_MOVE.get();
        enableCosmetics = ENABLE_COSMETICS.get();
        enableOriginalModel = ENABLE_PLAYER_VANILLA_MODEL.get();
        tpsType = TPS_TYPE.get();
        cameraHorizontalLocation = CAMERA_HORIZONTAL_LOCATION.get();
        cameraVerticalLocation = CAMERA_VERTICAL_LOCATION.get();
        cameraZoom = CAMERA_ZOOM.get();
        entityFocusingRange = ENTITY_FOCUSING_RANGE.get();

        holdingThreshold = HOLDING_THRESHOLD.get();
        autoPerspectiveSwithing = AUTO_PERSPECTIVE_SWITCHING.get();
        lockOnSnapping = LOCK_ON_SNAPPING.get();

        canceledVanillaActions = CANCEL_VANILLA_ACTION.get();
        playerBehaviorStrategy = PLAYER_BEHAVIOR_STRATEGY.get();
        cameraPerspectiveToggleMode = CAMERA_PERSPECTIVE_TOGGLE_MODE.get();

        combatCategorizedItems = COMBAT_CATEGORIZED_ITEMS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());
        miningCategorizedItems = MINING_CATEGORIZED_ITEMS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());

        if (EpicFightServerConnectionHelper.init(event.getConfig().getFullPath().getParent().toString())) {
            EpicFightMod.LOGGER.info("Epic Fight web server connection helper: supported");

            try {
                // Try loading epic skins code dynamically
                Class<?> cls = Class.forName("yesman.epicfight.epicskins.user.AuthenticationHelperImpl");
                Constructor<?> authImpl = cls.getDeclaredConstructor();
                authImpl.setAccessible(true);
                Object o = authImpl.newInstance();
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

    /// Gathers unsaved configuration changes and enqueues work for each case the user save or discard changes
    /// @param save      queued tasks when save changes
    /// @param discard   queued tasks when discard changes
    public static void checkUnsaved(List<Runnable> save, List<Runnable> discard) {
        if (maxStuckProjectiles != MAX_STUCK_PROJECTILES.get()) {
            save.add(() -> {
                MAX_STUCK_PROJECTILES.set(maxStuckProjectiles);
                MAX_STUCK_PROJECTILES.save();
            });
            discard.add(() -> maxStuckProjectiles = MAX_STUCK_PROJECTILES.get());
        }

        if (targetOutlineColor != TARGET_OUTLINE_COLOR.get()) {
            save.add(() -> {
                TARGET_OUTLINE_COLOR.set(targetOutlineColor);
                TARGET_OUTLINE_COLOR.save();
                packedTargetOutlineColor = ColorUtils.hue(targetOutlineColor);
            });
            discard.add(() -> {
                targetOutlineColor = TARGET_OUTLINE_COLOR.get();
                packedTargetOutlineColor = ColorUtils.hue(targetOutlineColor);
            });
        }

        if (bloodEffects != BLOOD_EFFECTS.get()) {
            save.add(() -> {
                BLOOD_EFFECTS.set(bloodEffects);
                BLOOD_EFFECTS.save();
            });
            discard.add(() -> bloodEffects = BLOOD_EFFECTS.get());
        }

        if (showEpicFightAttributesInTooltip != SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get()) {
            save.add(() -> {
                SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.set(showEpicFightAttributesInTooltip);
                SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.save();
            });
            discard.add(() -> showEpicFightAttributesInTooltip = SHOW_EPICFIGHT_ATTRIBUTES_IN_TOOLTIP.get());
        }

        if (activateComputeShader != ACTIVATE_COMPUTE_SHADER.get()) {
            save.add(() -> {
                ACTIVATE_COMPUTE_SHADER.set(activateComputeShader);
                ACTIVATE_COMPUTE_SHADER.save();
            });
            discard.add(() -> activateComputeShader = ACTIVATE_COMPUTE_SHADER.get());
        }

        if (activatePersistentBuffer != ACTIVATE_PERSISTENT_BUFFER.get()) {
            save.add(() -> {
                ACTIVATE_PERSISTENT_BUFFER.set(activatePersistentBuffer);
                ACTIVATE_PERSISTENT_BUFFER.save();
            });
            discard.add(() -> activatePersistentBuffer = ACTIVATE_PERSISTENT_BUFFER.get());
        }

        if (groundSlams != GROUND_SLAMS.get()) {
            save.add(() -> {
                GROUND_SLAMS.set(groundSlams);
                GROUND_SLAMS.save();
            });
            discard.add(() -> groundSlams = GROUND_SLAMS.get());
        }

        if (enableAnimatedFirstPersonModel != ENABLE_ANIMATED_FIRST_PERSON_MODEL.get()) {
            save.add(() -> {
                ENABLE_ANIMATED_FIRST_PERSON_MODEL.set(enableAnimatedFirstPersonModel);
                ENABLE_ANIMATED_FIRST_PERSON_MODEL.save();
            });
            discard.add(() -> enableAnimatedFirstPersonModel = ENABLE_ANIMATED_FIRST_PERSON_MODEL.get());
        }

        //
        if (mineBlockGuideOption != MINE_BLOCK_GUIDE_OPTION.get()) {
            save.add(() -> {
                MINE_BLOCK_GUIDE_OPTION.set(mineBlockGuideOption);
                MINE_BLOCK_GUIDE_OPTION.save();
            });
            discard.add(() -> mineBlockGuideOption = MINE_BLOCK_GUIDE_OPTION.get());
        }

        if (enableTargetEntityGuide != ENABLE_TARGET_ENTITY_GUIDE.get()) {
            save.add(() -> {
                ENABLE_TARGET_ENTITY_GUIDE.set(enableTargetEntityGuide);
                ENABLE_TARGET_ENTITY_GUIDE.save();
            });
            discard.add(() -> enableTargetEntityGuide = ENABLE_TARGET_ENTITY_GUIDE.get());
        }

        if (enableFirstPersonCameraMove != ENABLE_FIRST_PERSON_CAMERA_MOVE.get()) {
            save.add(() -> {
                ENABLE_FIRST_PERSON_CAMERA_MOVE.set(enableFirstPersonCameraMove);
                ENABLE_FIRST_PERSON_CAMERA_MOVE.save();
            });
            discard.add(() -> enableFirstPersonCameraMove = ENABLE_FIRST_PERSON_CAMERA_MOVE.get());
        }

        if (enableCosmetics != ENABLE_COSMETICS.get()) {
            save.add(() -> {
                ENABLE_COSMETICS.set(enableCosmetics);
                ENABLE_COSMETICS.save();
            });
            discard.add(() -> enableCosmetics = ENABLE_COSMETICS.get());
        }

        if (enableOriginalModel != ENABLE_PLAYER_VANILLA_MODEL.get()) {
            save.add(() -> {
                ENABLE_PLAYER_VANILLA_MODEL.set(enableOriginalModel);
                ENABLE_PLAYER_VANILLA_MODEL.save();
            });
            discard.add(() -> enableOriginalModel = ENABLE_PLAYER_VANILLA_MODEL.get());
        }

        if (tpsType != TPS_TYPE.get()) {
            save.add(() -> {
                TPS_TYPE.set(tpsType);
                TPS_TYPE.save();
            });
            discard.add(() -> tpsType = TPS_TYPE.get());
        }

        if (cameraHorizontalLocation != CAMERA_HORIZONTAL_LOCATION.get()) {
            save.add(() -> {
                CAMERA_HORIZONTAL_LOCATION.set(cameraHorizontalLocation);
                CAMERA_HORIZONTAL_LOCATION.save();
            });
            discard.add(() -> cameraHorizontalLocation = CAMERA_HORIZONTAL_LOCATION.get());
        }

        if (cameraVerticalLocation != CAMERA_VERTICAL_LOCATION.get()) {
            save.add(() -> {
                CAMERA_VERTICAL_LOCATION.set(cameraVerticalLocation);
                CAMERA_VERTICAL_LOCATION.save();
            });
            discard.add(() -> cameraVerticalLocation = CAMERA_VERTICAL_LOCATION.get());
        }

        if (cameraZoom != CAMERA_ZOOM.get()) {
            save.add(() -> {
                CAMERA_ZOOM.set(cameraZoom);
                CAMERA_ZOOM.save();
            });
            discard.add(() -> cameraZoom = CAMERA_ZOOM.get());
        }

        if (holdingThreshold != HOLDING_THRESHOLD.get()) {
            save.add(() -> {
                HOLDING_THRESHOLD.set(holdingThreshold);
                HOLDING_THRESHOLD.save();
            });
            discard.add(() -> holdingThreshold = HOLDING_THRESHOLD.get());
        }

        if (autoPerspectiveSwithing != AUTO_PERSPECTIVE_SWITCHING.get()) {
            save.add(() -> {
                AUTO_PERSPECTIVE_SWITCHING.set(autoPerspectiveSwithing);
                AUTO_PERSPECTIVE_SWITCHING.save();
            });
            discard.add(() -> autoPerspectiveSwithing = AUTO_PERSPECTIVE_SWITCHING.get());
        }

        if (lockOnSnapping != LOCK_ON_SNAPPING.get()) {
            save.add(() -> {
                LOCK_ON_SNAPPING.set(lockOnSnapping);
                LOCK_ON_SNAPPING.save();
            });
            discard.add(() -> lockOnSnapping = LOCK_ON_SNAPPING.get());
        }

        if (canceledVanillaActions != CANCEL_VANILLA_ACTION.get()) {
            save.add(() -> {
                CANCEL_VANILLA_ACTION.set(canceledVanillaActions);
                CANCEL_VANILLA_ACTION.save();
            });
            discard.add(() -> canceledVanillaActions = CANCEL_VANILLA_ACTION.get());
        }

        if (playerBehaviorStrategy != PLAYER_BEHAVIOR_STRATEGY.get()) {
            save.add(() -> {
                PLAYER_BEHAVIOR_STRATEGY.set(playerBehaviorStrategy);
                PLAYER_BEHAVIOR_STRATEGY.save();
            });
            discard.add(() -> playerBehaviorStrategy = PLAYER_BEHAVIOR_STRATEGY.get());
        }

        if (!combatCategorizedItems.equals(
            COMBAT_CATEGORIZED_ITEMS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet())
        )
        ) {
            save.add(() -> {
                COMBAT_CATEGORIZED_ITEMS.set(combatCategorizedItems.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList());
                COMBAT_CATEGORIZED_ITEMS.save();
            });
            discard.add(() -> {
                combatCategorizedItems.clear();
                combatCategorizedItems.addAll(COMBAT_CATEGORIZED_ITEMS.get().stream().map(itemStr -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemStr))).toList());
            });
        }

        if (
            !miningCategorizedItems.equals(
                MINING_CATEGORIZED_ITEMS.get().stream()
                    .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                    .collect(Collectors.toSet())
            )
        ) {
            save.add(() -> {
                MINING_CATEGORIZED_ITEMS.set(miningCategorizedItems.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList());
                MINING_CATEGORIZED_ITEMS.save();
            });
            discard.add(() -> {
                miningCategorizedItems.clear();
                miningCategorizedItems.addAll(MINING_CATEGORIZED_ITEMS.get().stream().map(itemStr -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemStr))).toList());
            });
        }
    }

    /// TODO: this is a cheap resolution for a crash by unknown reason: https://mclo.gs/nehnpG3
    /// We need to follow up the issue when the exact reason of the crash is confirmed, the log message
    /// will fully shown the caller
    public static TPSActivationType getTpsActivationType() {
        if (tpsType == null) {
            Exception noConfigValueException = new IllegalStateException("TPS Type is null");

            EpicFightMod.LOGGER.warn(
                "Epic Fight Config error: TPS Type is null",
                noConfigValueException
            );

            noConfigValueException.printStackTrace();

            return TPSActivationType.ON_AIMING;
        }

        return tpsType;
    }

    /// Determines which indicators are activated for block mining guide
    public enum BlockGuideOptions implements CirculatableEnum<BlockGuideOptions>, StringRepresentable {
        /// Changes nothign
        NONE(false, false),

        /// Crosshair changes when player looks at the block with mining preferred item
        CROSSHAIR(true, false),

        /// Block flashes white when player looks at the block with mining preferred item
        HIGHLIGHT(false, true),

        /// Both crosshair and block highlight will be appeared
        CROSSHAIR_AND_HIGHLIGHT(true, true);

        final boolean showCrosshair;
        final boolean showBlockHighlight;
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

    /// The scope of vanilla actions that will be canceled when they conflict with Epic Fight keybinds (currently, it only supports mouse right button)
    public enum CanceledVanillaActions implements CirculatableEnum<CanceledVanillaActions>, StringRepresentable {
        /// Won't cancel any vanilla behavior
        NONE(false, false),

        /// Cancel block interactions (e.g. opening UI screen for Furnace, Crafting Table)
        INTERACTION(true, false),

        /// Cancel item interactions (like plowing dirt using a hoe)
        ITEM_USE(false, true),

        /// Cancel both block and item interactions
        INTERACTION_AND_ITEM_USE(true, true);

        final boolean cancelInteraction;
        final boolean cancelItemUse;
        CanceledVanillaActions(boolean cancelBlockInteraction, boolean cancelItemInteraction) {
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
        public CanceledVanillaActions nextEnum() {
            return CanceledVanillaActions.values()[(this.ordinal() + 1) % 4];
        }

        @Override
        public String getSerializedName() {
            return ParseUtil.toLowerCase(this.name());
        }
    }

    /// Determines how item preference works
    public enum PlayerBehaviorStrategy implements CirculatableEnum<PlayerBehaviorStrategy>, StringRepresentable {
        /// Determines the next action based on crosshair hit result
        ADAPTIVE(true),

        /// Switches the player mode to each categorized preference, forcing the player to do only mine or attack.
        SWITCHING_MODE(false);

        final boolean checkHitResult;
        PlayerBehaviorStrategy(boolean checkHitResult) {
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
        public PlayerBehaviorStrategy nextEnum() {
            return PlayerBehaviorStrategy.values()[(this.ordinal() + 1) % 2];
        }
    }

    /// Defines how the camera perspective toggles when pressing toggle perspective (i.e., F5).
    public enum CameraPerspectiveToggleMode implements CirculatableEnum<CameraPerspectiveToggleMode>, StringRepresentable {
        /// Uses Minecraft's default behavior.
        ///
        /// Cycles through all available perspectives, including any added by third-party mods.
        /// This does not change the existing vanilla behavior.
        VANILLA,

        /// Skips the third-person front perspective only.
        ///
        /// Other perspectives remain available and are not ignored.
        SKIP_THIRD_PERSON_FRONT;

        @Override
        public CameraPerspectiveToggleMode nextEnum() {
            CameraPerspectiveToggleMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

        @Override
        public @NotNull String getSerializedName() {
            return ParseUtil.toLowerCase(this.name());
        }
    }

    /// Determines when camera should transite to TPS perspective in third-person
    public enum TPSActivationType implements CirculatableEnum<TPSActivationType>, StringRepresentable {
        /// always locates the camera in player's back like vanilla
        DISABLED(false, cameraApi -> false),

        /// activate tps perspective when player aims
        ON_AIMING(true, EpicFightCameraAPI::isZooming),

        /// always activate tps perspective
        ALWAYS(true, cameraApi -> true);

        final boolean hasTPSTransition;
        final Predicate<EpicFightCameraAPI> checker;
        TPSActivationType(boolean hasTPSTransition, Predicate<EpicFightCameraAPI> checker) {
            this.hasTPSTransition = hasTPSTransition;
            this.checker = checker;
        }

        public boolean shouldSwitch(EpicFightCameraAPI cameraApi) {
            return this.hasTPSTransition && this.checker.test(cameraApi);
        }

        public boolean hasTPSTransition() {
            return this.hasTPSTransition;
        }

        @Override
        public TPSActivationType nextEnum() {
            return TPSActivationType.values()[(this.ordinal() + 1) % 3];
        }

        @Override
        public String getSerializedName() {
            return ParseUtil.toLowerCase(this.name());
        }
    }
}
