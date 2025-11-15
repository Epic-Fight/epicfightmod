package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
import dev.isxander.controlify.api.bind.InputBindingBuilder;
import dev.isxander.controlify.api.bind.InputBindingSupplier;
import dev.isxander.controlify.api.buttonguide.ButtonGuideApi;
import dev.isxander.controlify.api.buttonguide.ButtonGuidePredicate;
import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.api.entrypoint.InitContext;
import dev.isxander.controlify.api.entrypoint.PreInitContext;
import dev.isxander.controlify.api.event.ControlifyEvents;
import dev.isxander.controlify.api.guide.*;
import dev.isxander.controlify.bindings.BindContext;
import dev.isxander.controlify.bindings.ControlifyBindings;
import dev.isxander.controlify.bindings.RadialIcons;
import dev.isxander.controlify.bindings.input.Input;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.utils.render.Blit;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.input.InputMode;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputActions;
import yesman.epicfight.api.client.input.controller.ControllerBinding;
import yesman.epicfight.api.client.input.controller.EpicFightControllerModProvider;
import yesman.epicfight.api.client.input.controller.IEpicFightControllerMod;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;

import java.util.Objects;
import java.util.Optional;

// Important for maintainers: Be careful when using Epic Fight classes here,
// as the Epic Fight mod might not be loaded yet. For example, avoid referencing
// EpicFightItems.UCHIGATANA.get() in onControlifyPreInit.
@ApiStatus.Internal
public class ControlifyCompat implements ControlifyEntrypoint {
    @Override
    public void onControllersDiscovered(ControlifyApi controlify) {
    }

    @Override
    public void onControlifyInit(InitContext context) {
        // It's best to call this method in onControlifyInit,
        // ensuring that Epic Fight can use Controlify input bindings
        // only after they have been registered.
        registerModIntegration();
    }

    @Override
    public void onControlifyPreInit(PreInitContext context) {
        final ControlifyBindApi registrar = ControlifyBindApi.get();
        registerCustomRadialIcons();
        registrar.registerBindContext(COMBAT_MODE_CONTEXT);
        registerInputBindings(registrar);
        registerTargetLockOnSupport();
        registerGuides();
        registerScreenProcessors();
    }

    private static InputBindingSupplier attack;
    private static InputBindingSupplier mobility;
    private static InputBindingSupplier guard;
    private static InputBindingSupplier dodge;
    private static InputBindingSupplier lockOn;
    private static InputBindingSupplier switchMode;
    private static InputBindingSupplier weaponInnateSkill;
    private static InputBindingSupplier weaponInnateSkillTooltip;
    private static InputBindingSupplier openSkillEditorScreen;
    private static InputBindingSupplier openConfigScreen;
    private static InputBindingSupplier switchVanillaModeDebugging;

    private static final BindContext COMBAT_MODE_CONTEXT = new BindContext(
            EpicFightMod.rl("epicfight_combat"),
            mc -> {
                final boolean isInGame = mc.screen == null && mc.level != null && mc.player != null;
                return isInGame && ClientEngine.getInstance().isBattleMode();
            }
    );
    private static final BindContext IN_GAME_CONTEXT = BindContext.IN_GAME;
    private static final BindContext ANY_SCREEN_CONTEXT = BindContext.ANY_SCREEN;

    private record TranslationKeys(@NotNull String name, @NotNull String description) {
        private @NotNull Component getNameComponent() {
            return Component.translatable(name());
        }

        private @NotNull Component getDescriptionComponent() {
            return Component.translatable(description());
        }

        /**
         * Maps a non-vanilla {@link EpicFightInputActions} to its corresponding translation keys.
         *
         * @param action the non-vanilla action to get the translation key for
         * @return a {@link TranslationKeys} instance containing the translation keys for the name and description
         * @throws IllegalArgumentException if the action is a vanilla action, since getting the translation keys in this class
         *                                  is only relevant for Epic Fight custom input binds.
         *                                  Vanilla input binds are handled internally by Controlify.
         */
        private static @NotNull TranslationKeys fromAction(@NotNull EpicFightInputActions action) {
            return switch (action) {
                case ATTACK -> new TranslationKeys("key.epicfight.attack", "key.epicfight.attack.description");
                case DODGE -> new TranslationKeys("key.epicfight.dodge", "key.epicfight.dodge.description");
                case GUARD -> new TranslationKeys("key.epicfight.guard", "key.epicfight.guard.description");
                case LOCK_ON -> new TranslationKeys("key.epicfight.lock_on", "key.epicfight.lock_on.description");
                case SWITCH_MODE ->
                        new TranslationKeys("key.epicfight.switch_mode", "key.epicfight.switch_mode.description");
                case WEAPON_INNATE_SKILL ->
                        new TranslationKeys("key.epicfight.weapon_innate_skill", "key.epicfight.weapon_innate_skill.description");
                case WEAPON_INNATE_SKILL_TOOLTIP ->
                        new TranslationKeys("key.epicfight.show_tooltip", "key.epicfight.show_tooltip.description");
                case OPEN_SKILL_SCREEN ->
                        new TranslationKeys("key.epicfight.skill_gui", "key.epicfight.skill_gui.description");
                case OPEN_CONFIG_SCREEN ->
                        new TranslationKeys("key.epicfight.config", "key.epicfight.config.description");
                case SWITCH_VANILLA_MODEL_DEBUGGING ->
                        new TranslationKeys("key.epicfight.switch_vanilla_model_debug", "key.epicfight.switch_vanilla_model_debug.description");
                case MOBILITY ->
                        new TranslationKeys("key.epicfight.mover_skill", "key.epicfight.mover_skill.description");

                // Vanilla actions translations already handled by Controlify.
                case VANILLA_ATTACK_DESTROY, USE, SWAP_OFF_HAND, DROP, TOGGLE_PERSPECTIVE, JUMP,
                     MOVE_FORWARD, MOVE_BACKWARD, MOVE_LEFT, MOVE_RIGHT, SPRINT, SNEAK ->
                        throw new IllegalArgumentException(
                                "TranslationKeys#fromAction() must only be called for non-vanilla actions. " +
                                        "This action is vanilla and already registered by Controlify: " + action.name()
                        );
            };
        }

        private static @NotNull Component getNameOf(@NotNull EpicFightInputActions action) {
            return fromAction(action).getNameComponent();
        }
    }

    private static class EpicFightInputCategories {
        private static final Component COMBAT = Component.translatable("key.epicfight.combat");
        private static final Component GUI = Component.translatable("key.epicfight.gui");
        private static final Component SYSTEM = Component.translatable("key.epicfight.system");
    }

    private enum EpicFightRadialIcons {
        UCHIGATANA(EpicFightMod.rl("textures/item/uchigatana_gui.png")),
        SKILL_BOOK(EpicFightMod.rl("textures/item/skillbook.png"));

        private final @NotNull ResourceLocation id;

        EpicFightRadialIcons(@NotNull ResourceLocation id) {
            this.id = id;
        }

        public @NotNull ResourceLocation getId() {
            return id;
        }
    }

    private static void registerCustomRadialIcons() {
        for (EpicFightRadialIcons icon : EpicFightRadialIcons.values()) {
            final ResourceLocation location = icon.getId();

            // For consistency with the current Controlify radial icons,
            // this code is equivalent to:
            // https://github.com/isXander/Controlify/blob/f5c94c57d5e0d4954e413624a0d7ead937b6e8ab/src/main/java/dev/isxander/controlify/bindings/RadialIcons.java#L106-L112
            RadialIcons.registerIcon(location, (graphics, x, y, tickDelta) -> {
                graphics.pose().pushPose();
                graphics.pose().translate((float) x, (float) y, 0.0F);
                graphics.pose().scale(0.5F, 0.5F, 1.0F);
                Blit.blitTex(graphics, location, 0, 0, 0, 0, 32, 32, 32, 32);
                graphics.pose().popPose();
            });
        }
    }

    private static void registerInputBindings(ControlifyBindApi registrar) {
        for (EpicFightInputActions action : EpicFightInputActions.nonVanillaActions()) {
            registerInputBinding(registrar, action);
        }
    }

    /**
     * Registers a non-vanilla input binding with Controlify.
     * <p>
     * Must <strong>only</strong> be called for non-vanilla
     * {@link EpicFightInputActions}. Vanilla actions are already registered
     * and calling this with one will throw {@link IllegalArgumentException}.
     * <p>
     * <strong>Type-safety and exhaustive checking:</strong><br>
     * Returns an {@link InputBindingSupplier} via a <em>switch expression</em>
     * over all enum constants. The returned value is a dummy, used only
     * to satisfy the Java compiler and enforce exhaustive handling. It is
     * <strong>never used</strong> and has no effect on behavior.
     *
     * @param registrar the Controlify API used to register the binding
     * @param action    the non-vanilla input action to register
     * @return a dummy {@link InputBindingSupplier} for type-safety only
     * @throws IllegalArgumentException if called with a vanilla input action
     */
    @SuppressWarnings("UnusedReturnValue") // Read Javadocs of this method before removing.
    private static @NotNull InputBindingSupplier registerInputBinding(
            @NotNull ControlifyBindApi registrar,
            @NotNull EpicFightInputActions action
    ) {
        final Component combatCategory = EpicFightInputCategories.COMBAT;
        final Component guiCategory = EpicFightInputCategories.GUI;
        final Component systemCategory = EpicFightInputCategories.SYSTEM;

        // Using a switch expression to enforce compile-time exhaustive checking.
        // The returned value is a dummy and does nothing; its only purpose is to
        // satisfy the compiler and ensure all enum constants are handled.
        return switch (action) {
            case VANILLA_ATTACK_DESTROY, USE, SWAP_OFF_HAND, TOGGLE_PERSPECTIVE, DROP, MOVE_FORWARD, MOVE_BACKWARD,
                 MOVE_LEFT, MOVE_RIGHT, SPRINT, SNEAK, JUMP -> throw new IllegalArgumentException(
                    "ControlifyCompat#registerInputBinding() must only be called for non-vanilla actions. " +
                            "This action is vanilla and already registered by Controlify: " + action.name()
            );
            case ATTACK -> attack = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
            );
            case MOBILITY -> mobility = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
            );
            case GUARD -> guard = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
            );
            case DODGE -> dodge = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
            );
            case LOCK_ON -> lockOn = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
            );
            case SWITCH_MODE -> switchMode = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(systemCategory)
                            .allowedContexts(IN_GAME_CONTEXT)
                            .radialCandidate(EpicFightRadialIcons.UCHIGATANA.getId())
            );
            case WEAPON_INNATE_SKILL -> weaponInnateSkill = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
            );
            case WEAPON_INNATE_SKILL_TOOLTIP -> weaponInnateSkillTooltip = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(guiCategory)
                            .allowedContexts(ANY_SCREEN_CONTEXT)
            );
            case OPEN_SKILL_SCREEN -> openSkillEditorScreen = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(guiCategory)
                            .allowedContexts(IN_GAME_CONTEXT)
                            .radialCandidate(EpicFightRadialIcons.SKILL_BOOK.getId())
            );
            case OPEN_CONFIG_SCREEN -> openConfigScreen = registrar.registerBinding(
                    builder -> applyCommonBindingProperties(action, builder)
                            .category(guiCategory)
                            .allowedContexts(IN_GAME_CONTEXT)
                            .radialCandidate(RadialIcons.getItem(Items.REDSTONE))
            );
            case SWITCH_VANILLA_MODEL_DEBUGGING -> switchVanillaModeDebugging = registrar.registerBinding(
                    builder ->
                            applyCommonBindingProperties(action, builder)
                                    .category(systemCategory)
                                    .allowedContexts(IN_GAME_CONTEXT)
            );
        };
    }

    private static @NotNull InputBindingBuilder applyCommonBindingProperties(
            @NotNull EpicFightInputActions action,
            @NotNull InputBindingBuilder builder
    ) {
        final TranslationKeys translationKeys = TranslationKeys.fromAction(action);
        final KeyMapping keyMappingToIgnore = action.keyMapping();
        return builder
                .id(getBindingId(action))
                .name(translationKeys.getNameComponent())
                .description(translationKeys.getDescriptionComponent())
                // Prevents Controlify from auto-registering controller bindings for Epic Fight's
                // vanilla key mappings, since Epic Fight already provides explicit native support.
                .addKeyCorrelation(keyMappingToIgnore);
    }

    private static @NotNull ResourceLocation getBindingId(@NotNull EpicFightInputActions action) {
        final String path = switch (action) {
            // Project maintainers: if you change any ID (e.g., "attack"), update assets/controlify too.
            case ATTACK -> "attack";
            case MOBILITY -> "mobility";
            case GUARD -> "guard";
            case DODGE -> "dodge";
            case LOCK_ON -> "lock_on";
            case SWITCH_MODE -> "switch_mode";
            case WEAPON_INNATE_SKILL -> "weapon_innate_skill";
            case WEAPON_INNATE_SKILL_TOOLTIP -> "weapon_innate_skill_tooltip";
            case OPEN_SKILL_SCREEN -> "open_skill_editor_screen";
            case OPEN_CONFIG_SCREEN -> "open_config_screen";
            case SWITCH_VANILLA_MODEL_DEBUGGING -> "switch_vanilla_mode_debugging";
            case VANILLA_ATTACK_DESTROY, USE, SWAP_OFF_HAND, TOGGLE_PERSPECTIVE, DROP, MOVE_FORWARD, MOVE_BACKWARD,
                 MOVE_LEFT, MOVE_RIGHT, SPRINT, SNEAK, JUMP -> throw new IllegalArgumentException(
                    "ControlifyCompat#getInputBindingId() must only be called for non-vanilla actions. " +
                            "This action is vanilla and already registered by Controlify: " + action.name()
            );
        };
        return EpicFightMod.rl(path);
    }

    private static void registerModIntegration() {
        EpicFightControllerModProvider.set(EpicFightMod.MODID, new ControlifyIntegration());
    }

    private static void registerTargetLockOnSupport() {
        ControlifyEvents.LOOK_INPUT_MODIFIER.register(event -> {
            LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();

            if (localPlayerPatch != null && localPlayerPatch.isTargetLockedOn()) {
                // Fixes a minor issue when locking on an enemy.
                event.lookInput().zero();
            }
        });
    }

    private static void registerGuides() {
        // Button guides are unsupported in "Controlify: Forgified"
        // Since this is an official backport of an older version of Controlify.
        // However, when using Epic Fight NeoForge 1.21.1, it's fully supported
    }

    private static @NotNull InputBinding getControlifyBinding(@NotNull EpicFightInputActions action) {
        final InputBindingSupplier bindingSupplier = switch (action) {
            // Minecraft Vanilla actions
            case VANILLA_ATTACK_DESTROY -> ControlifyBindings.ATTACK;
            case MOVE_FORWARD -> ControlifyBindings.WALK_FORWARD;
            case MOVE_BACKWARD -> ControlifyBindings.WALK_BACKWARD;
            case MOVE_LEFT -> ControlifyBindings.WALK_LEFT;
            case MOVE_RIGHT -> ControlifyBindings.WALK_RIGHT;
            case SPRINT -> ControlifyBindings.SPRINT;
            case SNEAK -> ControlifyBindings.SNEAK;
            case USE -> ControlifyBindings.USE;
            case SWAP_OFF_HAND -> ControlifyBindings.SWAP_HANDS;
            case DROP -> ControlifyBindings.DROP_INGAME;
            case TOGGLE_PERSPECTIVE -> ControlifyBindings.CHANGE_PERSPECTIVE;
            case JUMP -> ControlifyBindings.JUMP;
            // Epic Fight custom actions
            case ATTACK -> attack;
            case MOBILITY -> mobility;
            case GUARD -> guard;
            case DODGE -> dodge;
            case LOCK_ON -> lockOn;
            case SWITCH_MODE -> switchMode;
            case WEAPON_INNATE_SKILL -> weaponInnateSkill;
            case WEAPON_INNATE_SKILL_TOOLTIP -> weaponInnateSkillTooltip;
            case OPEN_SKILL_SCREEN -> openSkillEditorScreen;
            case OPEN_CONFIG_SCREEN -> openConfigScreen;
            case SWITCH_VANILLA_MODEL_DEBUGGING -> switchVanillaModeDebugging;
        };
        final @Nullable InputBinding binding = bindingSupplier.onOrNull(requireControllerEntity());
        return Objects.requireNonNull(binding, "The binding for the action " + action.name() + " is not yet registered.");
    }

    private static @NotNull ControlifyApi getApi() {
        return ControlifyApi.get();
    }

    private static @NotNull ControllerEntity requireControllerEntity() {
        Optional<ControllerEntity> optionalControllerEntity = getApi().getCurrentController();

        if (optionalControllerEntity.isEmpty()) {
            final String message = String.format(
                    "The method IEpicFightControllerMod#getInputState must not be called when the input mode is not %s",
                    InputMode.CONTROLLER.name()
            );
            EpicFightMod.LOGGER.error(message);
            throw new IllegalStateException(message);
        }

        return optionalControllerEntity.get();
    }

    /**
     * Allows Epic Fight to communicate with Controlify APIs without depending on their classes directly.
     */
    private static class ControlifyIntegration implements IEpicFightControllerMod {
        @Override
        public String getModName() {
            return "Controlify";
        }

        @Override
        public @NotNull InputMode getInputMode() {
            return switch (getApi().currentInputMode()) {
                case KEYBOARD_MOUSE -> InputMode.KEYBOARD_MOUSE;
                case CONTROLLER -> InputMode.CONTROLLER;
                case MIXED -> InputMode.MIXED;
            };
        }

        @Override
        public @NotNull ControllerBinding getBinding(EpicFightInputActions action) {
            return new ControllerBindingImpl(getControlifyBinding(action));
        }

        @Override
        public @NotNull PlayerInputState getInputState() {
            ControllerEntity controller = requireControllerEntity();

            InputBinding forwardBind = ControlifyBindings.WALK_FORWARD.on(controller);
            InputBinding backwardBind = ControlifyBindings.WALK_BACKWARD.on(controller);
            InputBinding leftBind = ControlifyBindings.WALK_LEFT.on(controller);
            InputBinding rightBind = ControlifyBindings.WALK_RIGHT.on(controller);
            InputBinding jumpBind = ControlifyBindings.JUMP.on(controller);
            InputBinding sneakBind = ControlifyBindings.SNEAK.on(controller);

            float forwardImpulse = forwardBind.analogueNow() - backwardBind.analogueNow();
            float leftImpulse = leftBind.analogueNow() - rightBind.analogueNow();

            return new PlayerInputState(
                    leftImpulse, forwardImpulse,
                    forwardBind.digitalNow(), backwardBind.digitalNow(),
                    leftBind.digitalNow(), rightBind.digitalNow(),
                    jumpBind.digitalNow(), sneakBind.digitalNow()
            );
        }

        @Override
        public boolean isBoundToSameButton(@NotNull EpicFightInputActions action, @NotNull EpicFightInputActions action2) {
            final Input input1 = getControlifyBinding(action).boundInput();
            final Input input2 = getControlifyBinding(action2).boundInput();
            return input1.getRelevantInputs().equals(input2.getRelevantInputs());
        }
    }

    private record ControllerBindingImpl(@NotNull InputBinding inputBinding) implements ControllerBinding {

        @Override
        public ResourceLocation id() {
            return inputBinding.id();
        }

        @Override
        public @NotNull InputType getInputType() {
            if (inputBinding.boundInput().type() == dev.isxander.controlify.bindings.input.InputType.AXIS) {
                return InputType.ANALOGUE;
            }
            EpicFightMod.LOGGER.error("The method ControllerBinding#getInputType is misleading and should not be called as it will be removed in future updates.");
            return InputType.DIGITAL;
        }

        @Override
        public boolean isDigitalActiveNow() {
            return inputBinding.digitalNow();
        }

        @Override
        public boolean wasDigitalActivePreviously() {
            return inputBinding.digitalPrev();
        }

        @Override
        public boolean isDigitalJustPressed() {
            return inputBinding.justPressed();
        }

        @Override
        public boolean isDigitalJustReleased() {
            return inputBinding.justReleased();
        }

        @Override
        public float getAnalogueNow() {
            return inputBinding.analogueNow();
        }

        @Override
        public void emulatePress() {
            inputBinding.fakePress();
        }
    }

    private static void registerScreenProcessors() {
        ScreenProcessorProvider.registerProvider(
                SkillEditScreen.class,
                SkillEditScreenProcessor::new
        );
        ScreenProcessorProvider.registerProvider(
                SkillBookScreen.class,
                SkillBookScreenProcessor::new
        );
    }

    private static class SkillEditScreenProcessor extends ScreenProcessor<SkillEditScreen> {
        public SkillEditScreenProcessor(SkillEditScreen screen) {
            super(screen);
        }

        private static final InputBindingSupplier OPEN_SKILL_INFO = ControlifyBindings.GUI_ABSTRACT_ACTION_1;

        @Override
        protected void handleButtons(ControllerEntity controller) {
            super.handleButtons(controller);

            if (this.screen.getFocused() instanceof SkillEditScreen.EquipSkillButton equipSkillButton &&
                    OPEN_SKILL_INFO.on(controller).guiPressed().get()) {
                equipSkillButton.openSkillInfoScreen();
            }
        }
    }

    private static class SkillBookScreenProcessor extends ScreenProcessor<SkillBookScreen> {
        public SkillBookScreenProcessor(SkillBookScreen screen) {
            super(screen);
        }

        private static final InputBindingSupplier LEARN_SKILL = ControlifyBindings.GUI_PRESS;

        @Override
        protected void handleButtons(ControllerEntity controller) {
            if (LEARN_SKILL.on(controller).guiPressed().get()) {
                screen.getLearnButton().onPress();
                playClackSound();
            }
            super.handleButtons(controller);
        }

        // The Skill Book screen has a single actionable button (the "learn skill" button).
        // Controller navigation and focus are disabled, and only the primary controller
        // button (e.g., X on DualSense) is used to trigger the action.

        @Override
        protected void setInitialFocus() {
            // Intentionally empty. Do NOT call super.setInitialFocus().
        }

        @Override
        protected void handleComponentNavigation(ControllerEntity controller) {
            // Intentionally empty. Do NOT call super.handleComponentNavigation().
        }

        @Override
        public void onWidgetRebuild() {
            super.onWidgetRebuild();

            ButtonGuideApi.addGuideToButton(
                    this.screen.getLearnButton(),
                    LEARN_SKILL,
                    ButtonGuidePredicate.always()
            );
        }
    }
}