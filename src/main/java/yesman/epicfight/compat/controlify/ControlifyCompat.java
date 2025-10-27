package yesman.epicfight.compat.controlify;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.bind.InputBinding;
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
import dev.isxander.controlify.utils.render.CGuiPose;
import dev.isxander.controlify.virtualmouse.VirtualMouseBehaviour;
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

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
        registerInGameGuides(context.guideRegistries().inGame());
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
                return isInGame && ClientEngine.getInstance().isEpicFightMode();
            }
    );
    private static final BindContext IN_GAME_CONTEXT = BindContext.IN_GAME;
    private static final BindContext ANY_SCREEN_CONTEXT = BindContext.ANY_SCREEN;

    private static class ComponentConstants {
        private static final Component KEY_COMBAT = Component.translatable("key.epicfight.combat");
        private static final Component KEY_GUI = Component.translatable("key.epicfight.gui");
        private static final Component KEY_SYSTEM = Component.translatable("key.epicfight.system");

        // Titles

        private static final Component WEAPON_INNATE_SKILL_TOOLTIP = Component.translatable("key.epicfight.show_tooltip");
        private static final Component KEY_SWITCH_MODE = Component.translatable("key.epicfight.switch_mode");
        private static final Component KEY_ATTACK = Component.translatable("key.epicfight.attack");
        private static final Component KEY_WEAPON_INNATE_SKILL = Component.translatable("key.epicfight.weapon_innate_skill");
        private static final Component KEY_SKILL_GUI = Component.translatable("key.epicfight.skill_gui");
        private static final Component KEY_DODGE = Component.translatable("key.epicfight.dodge");
        private static final Component KEY_GUARD = Component.translatable("key.epicfight.guard");
        private static final Component KEY_LOCK_ON = Component.translatable("key.epicfight.lock_on");
        private static final Component KEY_MOVER_SKILL = Component.translatable("key.epicfight.mover_skill");
        private static final Component KEY_CONFIG = Component.translatable("key.epicfight.config");
        private static final Component KEY_SWITCH_VANILLA_MODEL_DEBUG = Component.translatable("key.epicfight.switch_vanilla_model_debug");

        // Descriptions

        private static final Component WEAPON_INNATE_SKILL_TOOLTIP_DESCRIPTION = Component.translatable("key.epicfight.show_tooltip.description");
        private static final Component KEY_SWITCH_MODE_DESCRIPTION = Component.translatable("key.epicfight.switch_mode.description");
        private static final Component KEY_ATTACK_DESCRIPTION = Component.translatable("key.epicfight.attack.description");
        private static final Component KEY_WEAPON_INNATE_SKILL_DESCRIPTION = Component.translatable("key.epicfight.weapon_innate_skill.description");
        private static final Component KEY_SKILL_GUI_DESCRIPTION = Component.translatable("key.epicfight.skill_gui.description");
        private static final Component KEY_DODGE_DESCRIPTION = Component.translatable("key.epicfight.dodge.description");
        private static final Component KEY_GUARD_DESCRIPTION = Component.translatable("key.epicfight.guard.description");
        private static final Component KEY_LOCK_ON_DESCRIPTION = Component.translatable("key.epicfight.lock_on.description");
        private static final Component KEY_MOVER_SKILL_DESCRIPTION = Component.translatable("key.epicfight.mover_skill.description");
        private static final Component KEY_CONFIG_DESCRIPTION = Component.translatable("key.epicfight.config.description");
        private static final Component KEY_SWITCH_VANILLA_MODEL_DEBUG_DESCRIPTION = Component.translatable("key.epicfight.switch_vanilla_model_debug.description");
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
            RadialIcons.registerIcon(location, (graphics, x, y, tickDelta) -> {
                var pose = CGuiPose.ofPush(graphics);
                pose.translate(x, y);
                pose.scale(0.5f, 0.5f);
                Blit.tex(graphics, location, 0, 0, 0, 0, 32, 32, 32, 32);
                pose.pop();
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
        final Component combatCategory = ComponentConstants.KEY_COMBAT;
        final Component guiCategory = ComponentConstants.KEY_GUI;
        final Component systemCategory = ComponentConstants.KEY_SYSTEM;


        // Prevents Controlify from auto-registering this vanilla key mapping,
        // since Epic Fight already provides native support for it.
        final KeyMapping keyMappingToDisable = action.keyMapping();

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
                    builder -> builder.id(EpicFightMod.rl("attack"))
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
                            .name(ComponentConstants.KEY_ATTACK)
                            .description(ComponentConstants.KEY_ATTACK_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
            case MOBILITY -> mobility = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("mobility"))
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
                            .name(ComponentConstants.KEY_MOVER_SKILL)
                            .description(ComponentConstants.KEY_MOVER_SKILL_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
            case GUARD -> guard = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("guard"))
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
                            .name(ComponentConstants.KEY_GUARD)
                            .description(ComponentConstants.KEY_GUARD_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
            case DODGE -> dodge = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("dodge"))
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
                            .name(ComponentConstants.KEY_DODGE)
                            .description(ComponentConstants.KEY_DODGE_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
            case LOCK_ON -> lockOn = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("lock_on"))
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
                            .name(ComponentConstants.KEY_LOCK_ON)
                            .description(ComponentConstants.KEY_LOCK_ON_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
            case SWITCH_MODE -> switchMode = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("switch_mode"))
                            .category(systemCategory)
                            .allowedContexts(IN_GAME_CONTEXT)
                            .name(ComponentConstants.KEY_SWITCH_MODE)
                            .description(ComponentConstants.KEY_SWITCH_MODE_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
                            .radialCandidate(EpicFightRadialIcons.UCHIGATANA.getId())
            );
            case WEAPON_INNATE_SKILL -> weaponInnateSkill = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("weapon_innate_skill"))
                            .category(combatCategory)
                            .allowedContexts(COMBAT_MODE_CONTEXT)
                            .name(ComponentConstants.KEY_WEAPON_INNATE_SKILL)
                            .description(ComponentConstants.KEY_WEAPON_INNATE_SKILL_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
            case WEAPON_INNATE_SKILL_TOOLTIP -> weaponInnateSkillTooltip = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("weapon_innate_skill_tooltip"))
                            .category(guiCategory)
                            .allowedContexts(ANY_SCREEN_CONTEXT)
                            .name(ComponentConstants.WEAPON_INNATE_SKILL_TOOLTIP)
                            .description(ComponentConstants.WEAPON_INNATE_SKILL_TOOLTIP_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
            case OPEN_SKILL_SCREEN -> openSkillEditorScreen = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("open_skill_editor_screen"))
                            .category(guiCategory)
                            .allowedContexts(IN_GAME_CONTEXT)
                            .name(ComponentConstants.KEY_SKILL_GUI)
                            .description(ComponentConstants.KEY_SKILL_GUI_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
                            .radialCandidate(EpicFightRadialIcons.SKILL_BOOK.getId())
            );
            case OPEN_CONFIG_SCREEN -> openConfigScreen = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("open_config_screen"))
                            .category(guiCategory)
                            .allowedContexts(IN_GAME_CONTEXT)
                            .name(ComponentConstants.KEY_CONFIG)
                            .description(ComponentConstants.KEY_CONFIG_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
                            .radialCandidate(RadialIcons.getItem(Items.REDSTONE))
            );
            case SWITCH_VANILLA_MODEL_DEBUGGING -> switchVanillaModeDebugging = registrar.registerBinding(
                    builder -> builder.id(EpicFightMod.rl("switch_vanilla_mode_debugging"))
                            .category(systemCategory)
                            .allowedContexts(IN_GAME_CONTEXT)
                            .name(ComponentConstants.KEY_SWITCH_VANILLA_MODEL_DEBUG)
                            .description(ComponentConstants.KEY_SWITCH_VANILLA_MODEL_DEBUG_DESCRIPTION)
                            .addKeyCorrelation(keyMappingToDisable)
            );
        };
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

    private static void registerInGameGuides(GuideDomainRegistry<InGameCtx> registry) {
        registry.registerDynamicRule(
                Rule.builder().binding(dodge)
                        .where(ActionLocation.RIGHT)
                        .then(ComponentConstants.KEY_DODGE)
                        .build()
        );
        registry.registerDynamicRule(
                Rule.builder().binding(lockOn)
                        .where(ActionLocation.LEFT)
                        .when(InGameFacts.LOOKING_AT_ENTITY)
                        .then(ComponentConstants.KEY_LOCK_ON)
                        .build()
        );
    }

    private static @NotNull InputBinding getControlifyBinding(@NotNull EpicFightInputActions action) {
        final InputBindingSupplier bindingSupplier = switch (action) {
            case VANILLA_ATTACK_DESTROY -> ControlifyBindings.ATTACK;
            case USE -> ControlifyBindings.USE;
            case SWAP_OFF_HAND -> ControlifyBindings.SWAP_HANDS;
            case DROP -> ControlifyBindings.DROP_INGAME;
            case TOGGLE_PERSPECTIVE -> ControlifyBindings.CHANGE_PERSPECTIVE;
            case ATTACK -> attack;
            case JUMP -> ControlifyBindings.JUMP;
            case MOBILITY -> mobility;
            case GUARD -> guard;
            case DODGE -> dodge;
            case LOCK_ON -> lockOn;
            case SWITCH_MODE -> switchMode;
            case WEAPON_INNATE_SKILL -> weaponInnateSkill;
            case WEAPON_INNATE_SKILL_TOOLTIP -> weaponInnateSkillTooltip;
            case MOVE_FORWARD -> ControlifyBindings.WALK_FORWARD;
            case MOVE_BACKWARD -> ControlifyBindings.WALK_BACKWARD;
            case MOVE_LEFT -> ControlifyBindings.WALK_LEFT;
            case MOVE_RIGHT -> ControlifyBindings.WALK_RIGHT;
            case SPRINT -> ControlifyBindings.SPRINT;
            case SNEAK -> ControlifyBindings.SNEAK;
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
     *
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

        @Override
        public VirtualMouseBehaviour virtualMouseBehaviour() {
            // The skill edit screen does not natively support controllers.
            // To save development time, we work around this issue by enforcing the virtual mouse.
            return VirtualMouseBehaviour.ENABLED;
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
                screen.learnButton.onPress();
            }
            super.handleButtons(controller);
        }

        @Override
        protected void setInitialFocus() {
            // No-op
        }

        @Override
        protected void handleComponentNavigation(ControllerEntity controller) {
            // No-op
        }

        @Override
        public void onWidgetRebuild() {
            super.onWidgetRebuild();

            ButtonGuideApi.addGuideToButton(
                    this.screen.learnButton,
                    LEARN_SKILL,
                    ButtonGuidePredicate.always()
            );
        }
    }
}
