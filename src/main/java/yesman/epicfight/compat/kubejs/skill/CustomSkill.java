package yesman.epicfight.compat.kubejs.skill;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.compat.kubejs.CallbackUtils;
import yesman.epicfight.registry.entries.EpicFightCreativeTabs;
import yesman.epicfight.skill.*;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CustomSkill extends Skill {
    public record DrawOnGuiContext(BattleModeGui getGui, SkillContainer getContainer, GuiGraphics getGuiGraphics, float getX, float getY) {}
    public record OnScreenContext(LocalPlayerPatch getLocalPlayerPatch, float getResolutionX, float getResolutionY) {}
    public record GetTooltipOnItem(ItemStack getItemStack, CapabilityItem getCap, PlayerPatch<?> getPlayerPatch) {}

    private final BiConsumer<SkillContainer, EntityEventListener> onInitiate;
    private final Consumer<SkillContainer> onRemoved;
    private final BiConsumer<SkillContainer, CompoundTag> executeOnServer;
    private final BiConsumer<SkillContainer, CompoundTag> executeOnClient;
    private final BiConsumer<SkillContainer, CompoundTag> cancelOnServer;
    private final BiConsumer<SkillContainer, CompoundTag> cancelOnClient;
    private final Consumer<DrawOnGuiContext> drawOnGui;
    private final Predicate<SkillContainer> shouldDraw;
    private final Predicate<SkillContainer> canExecute;
    private final Predicate<PlayerPatch<?>> executableState;
    private final BiConsumer<SkillContainer, Float> setConsumption;
    private final Consumer<SkillContainer> updateContainer;
    private final Function<PlayerPatch<?>, Float> cooldownRegenPerSecond;
    private final int maxStackSize;
    private final int maxDuration;
    private final Predicate<PlayerPatch<?>> shouldDeactivateAutomatically;
    private final Consumer<OnScreenContext> onScreen;
    private final Function<GetTooltipOnItem, List<Component>> getTooltipOnItem;
    private final Function<List<Object>, List<Object>> getTooltipArgsOfScreen;

    private final ResourceLocation tab;
    private ResourceLocation textureLocation = this.getSkillTexture();


    public CustomSkill(CustomSkillBuilder builder) {
        super(
            new SkillBuilder<SkillBuilder<?>> (null)
                .setCategory(builder.category)
                .setCreativeTab(BuiltInRegistries.CREATIVE_MODE_TAB.get(builder.tab))
                .setActivateType(builder.activateType)
                .setResource(builder.resource)
                .setRegistryName(builder.id)
        );

        this.onInitiate = builder.onInitiate;
        this.onRemoved = builder.onRemoved;
        this.executeOnServer = builder.executeOnServer;
        this.executeOnClient = builder.executeOnClient;
        this.cancelOnServer = builder.cancelOnServer;
        this.cancelOnClient = builder.cancelOnClient;
        this.drawOnGui = builder.drawOnGui;
        this.shouldDraw = builder.shouldDraw;
        this.canExecute = builder.canExecute;
        this.executableState = builder.executableState;
        this.setConsumption = builder.setConsumption;
        this.updateContainer = builder.updateContainer;
        this.cooldownRegenPerSecond = builder.cooldownRegenPerSecond;
        this.maxStackSize = builder.maxStackSize;
        this.maxDuration = builder.maxDuration;
        this.shouldDeactivateAutomatically = builder.shouldDeactivateAutomatically;
        this.onScreen = builder.onScreen;
        this.getTooltipOnItem = builder.getTooltipOnItem;
        this.getTooltipArgsOfScreen = builder.getTooltipArgsOfScreen;

        if (builder.textureLocation != null) {
            this.textureLocation = builder.textureLocation;
        }

        this.tab = builder.tab;
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        if (shouldDraw != null) return shouldDraw.test(container);
        return super.shouldDraw(container);
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        if (canExecute != null) return canExecute.test(container);
        return super.canExecute(container);
    }

    @Override
    public boolean isExecutableState(PlayerPatch<?> executor) {
        if (executableState != null) return executableState.test(executor);
        return super.isExecutableState(executor);
    }


    @Override
    public void executeOnServer(SkillContainer container, CompoundTag arguments) {
        if (executeOnServer != null) CallbackUtils.biSafeCallback(executeOnServer, container, arguments, "Error while executing executeOnServer for skill: " + getRegistryName());
        super.executeOnServer(container, arguments);
    }

    @Override
    public void executeOnClient(SkillContainer container, CompoundTag arguments) {
        if (executeOnClient != null) CallbackUtils.biSafeCallback(executeOnClient, container, arguments, "Error while executing executeOnClient for skill: " + getRegistryName());
        super.executeOnClient(container, arguments);
    }

    @Override
    public void cancelOnServer(SkillContainer container, CompoundTag arguments) {
        if (cancelOnServer != null) CallbackUtils.biSafeCallback(cancelOnServer, container, arguments, "Error while executing cancelOnServer for skill: " + getRegistryName());
        super.cancelOnServer(container, arguments);
    }

    @Override
    public void cancelOnClient(SkillContainer container, CompoundTag arguments) {
        if (cancelOnClient != null) CallbackUtils.biSafeCallback(cancelOnClient, container, arguments, "Error while executing cancelOnClient for skill: " + getRegistryName());
        super.cancelOnClient(container, arguments);
    }

    @Override
    public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
        if (drawOnGui != null) {
            DrawOnGuiContext context = new DrawOnGuiContext(gui, container, guiGraphics, x, y);
            CallbackUtils.safeCallback(drawOnGui, context, "Error while drawing HUD for skill: " + getRegistryName());
        }
        super.drawOnGui(gui, container, guiGraphics, x, y, partialTick);
    }

    @Override
    public void onRemoved(SkillContainer container) {
        if (onRemoved != null) CallbackUtils.safeCallback(onRemoved, container, "Error while executing onRemoved for skill: " + getRegistryName());
        super.onRemoved(container);
    }

    @Override
    public void onInitiate(SkillContainer container, EntityEventListener entityEventListener) {
        if (onInitiate != null) CallbackUtils.biSafeCallback(onInitiate, container, entityEventListener, "Error while executing onInitiate for skill: " + getRegistryName());
        super.onInitiate(container, entityEventListener);
    }

    @Override
    public void setConsumption(SkillContainer container, float value) {
        if (setConsumption != null) {
            CallbackUtils.biSafeCallback(setConsumption, container, value, "Error while executing consumption for skill: " + getRegistryName());
            return;
        }
        super.setConsumption(container, value);
    }

    @Override
    public void updateContainer(SkillContainer container) {
        if (updateContainer != null) CallbackUtils.safeCallback(updateContainer, container, "Error while executing updateContainer for skill: " + getRegistryName());
        super.updateContainer(container);
    }

    @Override
    public float getCooldownRegenPerSecond(PlayerPatch<?> executor) {
        if (cooldownRegenPerSecond != null) return cooldownRegenPerSecond.apply(executor);
        return super.getCooldownRegenPerSecond(executor);
    }

    @Override
    public int getMaxStack() {
        return maxStackSize;
    }

    @Override
    public int getMaxDuration() {
        return maxDuration;
    }

    @Override
    public boolean shouldDeactivateAutomatically(PlayerPatch<?> executor) {
        if (shouldDeactivateAutomatically != null) return shouldDeactivateAutomatically.test(executor);
        return super.shouldDeactivateAutomatically(executor);
    }

    @Override
    public void onScreen(LocalPlayerPatch localPlayerPatch, float resolutionX, float resolutionY) {
        if (onScreen != null) {
            OnScreenContext context = new OnScreenContext(localPlayerPatch, resolutionX, resolutionY);
            CallbackUtils.safeCallback(onScreen, context, "Error while executing onScreen for skill: " + getRegistryName());
        }
        super.onScreen(localPlayerPatch, resolutionX, resolutionY);
    }

    @Override
    public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerPatch) {
        if (getTooltipOnItem != null) {
            GetTooltipOnItem context = new GetTooltipOnItem(itemStack, cap, playerPatch);
            return getTooltipOnItem.apply(context);
        }
        return super.getTooltipOnItem(itemStack, cap, playerPatch);
    }

    @Override
    public List<Object> getTooltipArgsOfScreen(List<Object> args) {
        if (getTooltipArgsOfScreen != null) return getTooltipArgsOfScreen.apply(args);
        return super.getTooltipArgsOfScreen(args);
    }

    @Override
    public ResourceLocation getSkillTexture() {
        return textureLocation;
    }

    @Override
    public CreativeModeTab getCreativeTab() {
        if (tab != null) {
            return BuiltInRegistries.CREATIVE_MODE_TAB.get(tab);
        }
        return EpicFightCreativeTabs.ITEMS.get();
    }

    @Info("""
            Creates a custom skill. The builder requires one of each of the following to function:
            - category
            - activateType
            - resource
            - texture
            """)
    public static class CustomSkillBuilder extends BuilderBase<Skill> {
        public ResourceLocation tab;
        public SkillCategory category;
        public Skill.ActivateType activateType = Skill.ActivateType.ONE_SHOT;
        public Skill.Resource resource = Skill.Resource.NONE;

        private BiConsumer<SkillContainer, EntityEventListener> onInitiate;
        private Consumer<SkillContainer> onRemoved;
        private BiConsumer<SkillContainer, CompoundTag> executeOnServer;
        private BiConsumer<SkillContainer, CompoundTag> executeOnClient;
        private BiConsumer<SkillContainer, CompoundTag> cancelOnServer;
        private BiConsumer<SkillContainer, CompoundTag> cancelOnClient;
        private Consumer<DrawOnGuiContext> drawOnGui;
        private Predicate<SkillContainer> shouldDraw;
        private Predicate<SkillContainer> canExecute;
        private Predicate<PlayerPatch<?>> executableState;
        private BiConsumer<SkillContainer, Float> setConsumption;
        private Consumer<SkillContainer> updateContainer;
        private Function<PlayerPatch<?>, Float> cooldownRegenPerSecond;
        private int maxStackSize = 1;
        private int maxDuration = 0;
        private Predicate<PlayerPatch<?>> shouldDeactivateAutomatically;
        private Consumer<OnScreenContext> onScreen;
        private Function<GetTooltipOnItem, List<Component>> getTooltipOnItem;
        private Function<List<Object>, List<Object>> getTooltipArgsOfScreen;

        private ResourceLocation textureLocation;

        public CustomSkillBuilder(ResourceLocation id) {
            super(id);
        }

        @Info("""
                Sets the creative tab that the skill book for this skill will be in.
                Optional.
                The KubeJS tab is `'kubejs:kubejs'` and the Epic Fight tab is `epicfight:items`.
                """)
        public CustomSkillBuilder tab(ResourceLocation tab) {
            this.tab = tab;
            return this;
        }

        @Info("""
                Sets the category of the skill. Input a string of the category.
                Required.
                """)
        public CustomSkillBuilder category(SkillCategories category) {
            this.category = category;
            return this;
        }

        @Info("""
                Sets the activate type of the skill. Input a string of the type.
                """)
        public CustomSkillBuilder activateType(Skill.ActivateType activateType) {
            this.activateType = activateType;
            return this;
        }

        @Info("""
                Sets the resource type of the skill. Input a string of the type.
                """)
        public CustomSkillBuilder resource(Skill.Resource resource) {
            this.resource = resource;
            return this;
        }

        @Info("""
                Sets the texture of the skill. Input a string or resource location of the texture.
                Example: `minecraft:textures/block/stone.png`
                Required.
                """)
        public CustomSkillBuilder texture(ResourceLocation textureLocation) {
            this.textureLocation = textureLocation;
            return this;
        }

        @Info("""
                This is called when the skill is learned by the player.
                """)
        public CustomSkillBuilder onInitiate(BiConsumer<SkillContainer, EntityEventListener> consumer) {
            this.onInitiate = consumer;
            return this;
        }

        @Info("""
                This is called when the skill is removed from the player.
                """)
        public CustomSkillBuilder onRemoved(Consumer<SkillContainer> consumer) {
            this.onRemoved = consumer;
            return this;
        }

        @Info("""
                This is called when the skill is executed on the server. This is where you should put your skill logic.
                The second argument is the buffer that is sent from the client. It's used for data synchronization.
                """)
        public CustomSkillBuilder executeOnServer(BiConsumer<SkillContainer, CompoundTag> consumer) {
            this.executeOnServer = consumer;
            return this;
        }

        @Info("""
                This is called when the skill is executed on the client. Best to use this in sync with the server if it is a skill that moves the player.
                The second argument is the buffer that is sent from the server. It's used for data synchronization.
                """)
        public CustomSkillBuilder executeOnClient(BiConsumer<SkillContainer, CompoundTag> consumer) {
            this.executeOnClient = consumer;
            return this;
        }

        @Info("""
                Called when the skill is cancelled on the server.
                """)
        public CustomSkillBuilder cancelOnServer(BiConsumer<SkillContainer, CompoundTag> consumer) {
            this.cancelOnServer = consumer;
            return this;
        }

        @Info("""
                Called when the skill is cancelled on the client.
                """)
        public CustomSkillBuilder cancelOnClient(BiConsumer<SkillContainer, CompoundTag> consumer) {
            this.cancelOnClient = consumer;
            return this;
        }

        @Info("""
                Called when resource consumption is being calculated.
                """)
        public CustomSkillBuilder setConsumption(BiConsumer<SkillContainer, Float> consumer) {
            this.setConsumption = consumer;
            return this;
        }

        @Info("""
                Called each tick the skill is active.
                """)
        public CustomSkillBuilder updateContainer(Consumer<SkillContainer> consumer) {
            this.updateContainer = consumer;
            return this;
        }

        @Info("""
                Called when the cooldown regeneration is being calculated.
                """)
        public CustomSkillBuilder cooldownRegenPerSecond(Function<PlayerPatch<?>, Float> consumer) {
            this.cooldownRegenPerSecond = consumer;
            return this;
        }

        @Info("""
                Sets the max stack size of the skill.
                """)
        public CustomSkillBuilder maxStackSize(int maxStackSize) {
            this.maxStackSize = maxStackSize;
            return this;
        }

        @Info("""
                Sets the max duration of the skill.
                """)
        public CustomSkillBuilder maxDuration(int maxDuration) {
            this.maxDuration = maxDuration;
            return this;
        }

        @Info("""
                Predicate on whether the skill should deactivate automatically or not.
                """)
        public CustomSkillBuilder shouldDeactivateAutomatically(Predicate<PlayerPatch<?>> predicate) {
            this.shouldDeactivateAutomatically = predicate;
            return this;
        }

        @Info("""
                Consumer that is called to draw the skill on the GUI.
                """)
        public CustomSkillBuilder drawOnGui(Consumer<DrawOnGuiContext> consumer) {
            this.drawOnGui = consumer;
            return this;
        }

        @Info("""
                Predicate that is called to check if the skill should be drawn on the GUI.
                """)
        public CustomSkillBuilder shouldDraw(Predicate<SkillContainer> predicate) {
            this.shouldDraw = predicate;
            return this;
        }

        @Info("""
                Predicate that is called to check if the skill can be executed.
                """)
        public CustomSkillBuilder canExecute(Predicate<SkillContainer> predicate) {
            this.canExecute = predicate;
            return this;
        }

        @Info("""
                Predicate that is called to check if the skill is in executable state.
                """)
        public CustomSkillBuilder executableState(Predicate<PlayerPatch<?>> predicate) {
            this.executableState = predicate;
            return this;
        }

        @Info("""
                Consumer that is called when the skill is added from the skill HUD.
                """)
        public CustomSkillBuilder onScreen(Consumer<OnScreenContext> consumer) {
            this.onScreen = consumer;
            return this;
        }

        @Info("""
                Sets the tooltip of the skill on the item that has this skill as an innate.
                """)
        public CustomSkillBuilder getTooltipOnItem(Function<GetTooltipOnItem, List<Component>> function) {
            this.getTooltipOnItem = function;
            return this;
        }

        @Info("""
                Sets the parameters of the description of the skill on the skill book GUI.
                """)
        public CustomSkillBuilder getTooltipArgsOfScreen(Function<List<Object>, List<Object>> function) {
            this.getTooltipArgsOfScreen = function;
            return this;
        }

        @Override
        public Skill createObject() {
            return new CustomSkill(this);
        }
    }
}
