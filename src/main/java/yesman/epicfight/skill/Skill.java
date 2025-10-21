package yesman.epicfight.skill;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.api.neoevent.playerpatch.SkillCancelEvent;
import yesman.epicfight.api.neoevent.playerpatch.SkillCastEvent;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.network.server.SPSetSkillContainerValue;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public abstract class Skill {
	public static final Codec<Holder<Skill>> CODEC = EpicFightRegistries.SKILL.holderByNameCodec();
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Skill>> STREAM_CODEC = ByteBufCodecs.holderRegistry(EpicFightRegistries.Keys.SKILL);
	
	public static record ModifierEntry(Holder<Attribute> attribute, AttributeModifier modifier) {
	}
	
	public static final Codec<List<ModifierEntry>> ATTRIBUTE_ENTRY_CODEC = Codec.list(
		RecordCodecBuilder.create(instance -> 
			instance.group(
				Attribute.CODEC.fieldOf("attribute").forGetter(ModifierEntry::attribute),
				AttributeModifier.MAP_CODEC.forGetter(ModifierEntry::modifier)
			)
			.apply(instance, ModifierEntry::new)
		)
	);
	
	public static Holder<Skill> holderOrNull(Skill skill) {
		return skill == null ? null : skill.holder();
	}
	
	public static Skill skillOrNull(Holder<Skill> skill) {
		return skill == null ? null : skill.value();
	}
	
	@SuppressWarnings("unchecked")
	public static <B extends SkillBuilder<B>> B createBuilder(Function<B, ? extends Skill> constructor) {
		return (B)new SkillBuilder<> (constructor);
	}
	
	public static <B extends SkillBuilder<B>> B createIdentityBuilder(Function<B, ? extends Skill> constructor) {
		return new SkillBuilder<> (constructor).setCategory(SkillCategories.IDENTITY).setResource(Resource.NONE);
	}
	
	public static <B extends SkillBuilder<B>> B createMoverBuilder(Function<B, ? extends Skill> constructor) {
		return new SkillBuilder<> (constructor).setCategory(SkillCategories.MOVER).setResource(Resource.STAMINA);
	}
	
	public static final Skill EMPTY = new Skill() {};
	
	private final Map<Holder<Attribute>, AttributeModifier> attributes = new HashMap<> ();
	
	/**
	 * Skill events can be registered by both {@link SkillEvent} annotated method in skill class or by the skill builder
	 * 
	 * The container hierarchy looks as this:
	 * 
	 * caller mod id
	 *  ㄴ event class
	 * 	 ㄴ listener
	 * 
	 * See what each elements do: {@link SkillBuilder#addEventListenerBothSide}
	 **/
	private final Map<String, Map<Class<?>, SkillEventSubscriber>> clientEventListeners;
	private final Map<String, Map<Class<?>, SkillEventSubscriber>> serverEventListeners;
	
	protected final ResourceLocation registryName;
	protected final SkillCategory category;
	protected final CreativeModeTab creativeTab;
	protected final ActivateType activateType;
	protected final Resource resource;
	protected float consumption;
	protected int maxDuration;
	protected int maxStackSize;
	
	protected Holder<Skill> holder;
	
	public Skill(SkillBuilder<?> builder) {
		if (builder.registryName == null) {
			Exception e = new IllegalArgumentException("No registry name is given for " + this.getClass().getCanonicalName());
			e.printStackTrace();
		}
		
		this.registryName = builder.registryName;
		this.category = builder.category;
		this.creativeTab = builder.tab;
		this.activateType = builder.activateType;
		this.resource = builder.resource;
		this.clientEventListeners = ImmutableMap.copyOf(builder.clientEventListeners);
		this.serverEventListeners = ImmutableMap.copyOf(builder.serverEventListeners);
	}
	
	@ApiStatus.Internal
	@Deprecated
	private Skill() {
		this.registryName = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "empty");
		this.category = SkillCategories.EMPTY;
		this.creativeTab = null;
		this.activateType = ActivateType.ONE_SHOT;
		this.resource = Resource.NONE;
		this.clientEventListeners = ImmutableMap.of();
		this.serverEventListeners = ImmutableMap.of();
	}
	
	/**
	 * Load parameters from datapack presented as {@link CompoundTag}
	 * 
	 * Previous container data(e.g. HashMap, ArrayList) must be cleared
	 */
	public void loadDatapackParameters(CompoundTag parameters) {
		this.consumption = parameters.getFloat("consumption");
		this.maxDuration = parameters.getInt("max_duration");
		this.maxStackSize = parameters.contains("max_stacks") ? parameters.getInt("max_stacks") : 1;
		this.attributes.clear();
		
		if (parameters.contains("attribute_modifiers")) {
			ListTag modifierListTag = parameters.getList("attribute_modifiers", Tag.TAG_COMPOUND);
			ATTRIBUTE_ENTRY_CODEC.parse(NbtOps.INSTANCE, modifierListTag).result().ifPresent(modifiers -> modifiers.forEach(modifierEntry -> this.attributes.put(modifierEntry.attribute, modifierEntry.modifier)));
		}
	}
	
	/**
	 * Check the player state if he can execute the skill or not
	 */
	public boolean isExecutableState(PlayerPatch<?> executor) {
		return !executor.getOriginal().isSpectator() && !executor.isInAir() && executor.getEntityState().canUseSkill();
	}
	
	/**
	 * Check the resource & other restrictions to execute the skill
	 */
	public boolean canExecute(SkillContainer container) {
		return this.checkExecuteCondition(container);
	}
	
	/**
	 * This makes the skill icon white in Gui if it returns false
	 */
	public boolean checkExecuteCondition(SkillContainer container) {
		return true;
	}
	
	/**
	 * Notify the executor unmet conditions to cast the skill
	 */
	@OnlyIn(Dist.CLIENT)
	public void validationFeedback(SkillContainer container) {
		
	}
	
	/**
	 * Get a packet to send to the server
	 */
	@OnlyIn(Dist.CLIENT)
	public CustomPacketPayload getExecutionPacket(SkillContainer container, @Nullable CompoundTag args) {
		return new CPSkillRequest(container.getSlot(), CPSkillRequest.WorkType.CAST, args);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void gatherArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
	}
	
	public void executeOnServer(SkillContainer container, CompoundTag args) {
		SPSkillFeedback feedbackPacket = SPSkillFeedback.executed(container.getSlot());
		ServerPlayerPatch executor = container.getServerExecutor();
		
		if (executor.isHoldingAny()) {
			if (executor.getHoldingSkill() instanceof ChargeableSkill) {
				feedbackPacket.arguments().putInt("chargingTicks", executor.getAccumulatedChargeTicks());
			}
			
			if (executor.getHoldingSkill() == this) {
				executor.getHoldingSkill().onStopHolding(container, feedbackPacket);
			}
			
			executor.resetHolding();
		} else {
			container.activate();
		}
		
		EpicFightNetworkManager.sendToPlayer(feedbackPacket, executor.getOriginal());
	}
	
	public void cancelOnServer(SkillContainer container, CompoundTag args) {
		ServerPlayerPatch executor = container.getServerExecutor();
		SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, container);
		PlayerPatchEvent.postAndFireSkillListeners(skillCancelEvent);
		
		EpicFightNetworkManager.sendToPlayer(SPSkillFeedback.expired(container.getSlot()), executor.getOriginal());
	}
	
	public final float getDefaultConsumptionAmount(PlayerPatch<?> executor) {
        return switch (this.resource) {
            case STAMINA -> executor.getModifiedStaminaConsume(this.consumption);
            case WEAPON_CHARGE, COOLDOWN -> 1;
            default -> 0.0F;
        };
	}
	
	/**
	 * Instant feedback when the skill is executed successfully
	 * @param container
	 * @param args
	 */
	@OnlyIn(Dist.CLIENT)
	public void executeOnClient(SkillContainer container, CompoundTag args) {
	}
	
	/**
	 * Called when the duration ends.
	 * @param container
	 * @param args
	 */
	@OnlyIn(Dist.CLIENT)
	public void cancelOnClient(SkillContainer container, CompoundTag args) {
		LocalPlayerPatch executor = container.getClientExecutor();
		SkillCancelEvent skillCancelEvent = new SkillCancelEvent(executor, container);
		PlayerPatchEvent.postAndFireSkillListeners(skillCancelEvent);
	}
	
	public void onTracked(SkillContainer container, EpicFightNetworkManager.PayloadBundleBuilder payloadBuilder) {
	}
	
	public void onInitiate(SkillContainer container) {
		container.maxDuration = this.maxDuration;
		
		for (Map.Entry<Holder<Attribute>, AttributeModifier> stat : this.attributes.entrySet()) {
			AttributeInstance attr = container.getExecutor().getOriginal().getAttribute(stat.getKey());
			
			if (!attr.hasModifier(stat.getValue().id())) {
				attr.addTransientModifier(stat.getValue());
			}
		}
	}
	
	/**
	 * A method that initiates skill for local and remote players.
	 * It's ok to use onInitiate method above when you have to synchronize to the local player only. Use this method when you need your skill to be synchronized to all remote players
	 * @param container
	 */
	@OnlyIn(Dist.CLIENT)
	public void onInitiateClient(SkillContainer container) {
	}
	
	/**
	 * Remove all events from onInitiateClient
	 * @param container
	 */
	@OnlyIn(Dist.CLIENT)
	public void onRemoveClient(SkillContainer container) {
	}
	
	/**
	 * When skill removed from the container
	 * @param container
	 */
	public void onRemoved(SkillContainer container) {
		for (Map.Entry<Holder<Attribute>, AttributeModifier> stat : this.attributes.entrySet()) {
			AttributeInstance attr = container.getExecutor().getOriginal().getAttribute(stat.getKey());
			
			if (attr.hasModifier(stat.getValue().id())) {
				attr.removeModifier(stat.getValue());
			}
		}
	}
	
	/**
	 * When stacks reach to zero
	 * @param container
	 */
	public void onReset(SkillContainer container) {
	}
	
	public void setConsumption(SkillContainer container, float value) {
		container.resource = Mth.clamp(value, 0, container.getMaxResource());
		
		if (value >= container.getMaxResource()) {
			if (container.stack < this.maxStackSize) {
				container.stack++;	
				container.resource = 0;
				container.prevResource = 0;
			} else {
				container.resource = container.getMaxResource();
				container.prevResource = container.getMaxResource();
			}
		} else if (value == 0 && container.stack > 0) {
			--container.stack;
		}
	}
	
	public void updateContainer(SkillContainer container) {
		container.prevResource = container.resource;
		container.prevDuration = container.duration;
		
		if (this.resource == Resource.COOLDOWN) {
			if (container.stack < this.maxStackSize) {
				container.setResource(container.resource + this.getCooldownRegenPerSecond(container.getExecutor()) * EpicFightSharedConstants.A_TICK);
			}
		}
		
		if (container.isActivated()) {
			if (this.activateType == ActivateType.DURATION) {
				container.duration--;
			}
			
			boolean isEnd = false;
			
			if (this.activateType == ActivateType.TOGGLE) {
				if (container.stack <= 0 && !container.getExecutor().getOriginal().isCreative()) {
					isEnd = true;
				}
			} else if (this.activateType != ActivateType.HELD) {
				if (container.duration <= 0) {
					isEnd = true;
				}
			}
			
			if (isEnd) {
				container.runOnServer(serverplayerpatch -> {
					if (this.activateType != ActivateType.HELD) {
						this.cancelOnServer(container, null);
					}
				});
				
				container.deactivate();
			}
		}

		if (this.activateType == ActivateType.HELD && container.getExecutor().getHoldingSkill() == this) {
			HoldableSkill holdableSkill = (HoldableSkill)this;
			holdableSkill.holdTick(container);

			container.runOnServer(serverExecutor -> {
				container.getExecutor().resetActionTick();

				if (this instanceof ChargeableSkill chargingSkill && container.getExecutor().getSkillChargingTicks(1.0F) > chargingSkill.getAllowedMaxChargingTicks()) {
					SPSkillFeedback feedbackPacket = SPSkillFeedback.executed(container.getSlot());
					feedbackPacket.arguments().putInt("chargingTicks", serverExecutor.getAccumulatedChargeTicks());
					chargingSkill.onStopHolding(container, feedbackPacket);
					container.getExecutor().resetHolding();
					EpicFightNetworkManager.sendToPlayer(feedbackPacket, serverExecutor.getOriginal());
				}
			});
		}
	}
	
	public final SkillEventSubscriber getSkillEvent(String caller, Class<? extends net.neoforged.bus.api.Event> event, boolean logicalClient) {
		Map<String, Map<Class<?>, SkillEventSubscriber>> map = logicalClient ? this.clientEventListeners : this.serverEventListeners;
		
		if (map.containsKey(caller)) {
			Map<Class<?>, SkillEventSubscriber> byCaller = map.get(caller);
			
			if (byCaller.containsKey(event)) {
				return byCaller.get(event);
			}
		}
		
		return null;
	}
	
	public boolean isActivated(SkillContainer container) {
		return this.equals(container.getSkill()) ? container.isActivated() : false;
	}
	
	public boolean isDisabled(SkillContainer container) {
		return this.equals(container.getSkill()) ? container.isDisabled() : true;
	}
	
	/**
	 * Make sure this method is called in a server side.
	 */
	public void setConsumptionSynchronize(SkillContainer container, float amount) {
		if (this.equals(container.skill)) {
			setSkillConsumptionSynchronize(container, amount);
		}
	}
	
	public void setMaxDurationSynchronize(SkillContainer container, int amount) {
		if (this.equals(container.skill)) {
			setSkillMaxDurationSynchronize(container, amount);
		}
	}
	
	public void setDurationSynchronize(SkillContainer container, int amount) {
		if (this.equals(container.skill)) {
			setSkillDurationSynchronize(container, amount);
		}
	}
	
	public void setStackSynchronize(SkillContainer container, int amount) {
		if (this.equals(container.skill)) {
			setSkillStackSynchronize(container, amount);
		}
	}
	
	public void setMaxResourceSynchronize(SkillContainer container, float amount) {
		if (this.equals(container.skill)) {
			setSkillMaxResourceSynchronize(container, amount);
		}
	}
	
	public static void setSkillConsumptionSynchronize(SkillContainer skillContainer, float fVal) {
		skillContainer.setResource(fVal);
		EpicFightNetworkManager.sendToPlayer(SPSetSkillContainerValue.resource(skillContainer.getSlot(), fVal, skillContainer.getExecutor().getOriginal().getId()), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillDurationSynchronize(SkillContainer skillContainer, int iVal) {
		skillContainer.setDuration(iVal);
		EpicFightNetworkManager.sendToPlayer(SPSetSkillContainerValue.duration(skillContainer.getSlot(), iVal, skillContainer.getExecutor().getOriginal().getId()), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillMaxDurationSynchronize(SkillContainer skillContainer, int iVal) {
		skillContainer.setMaxDuration(iVal);
		EpicFightNetworkManager.sendToPlayer(SPSetSkillContainerValue.maxDuration(skillContainer.getSlot(), iVal, skillContainer.getExecutor().getOriginal().getId()), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillStackSynchronize(SkillContainer skillContainer, int iVal) {
		skillContainer.setStack(iVal);
		EpicFightNetworkManager.sendToPlayer(SPSetSkillContainerValue.stacks(skillContainer.getSlot(), iVal, skillContainer.getExecutor().getOriginal().getId()), skillContainer.getServerExecutor().getOriginal());
	}
	
	public static void setSkillMaxResourceSynchronize(SkillContainer skillContainer, float fVal) {
		skillContainer.setMaxResource(fVal);
		EpicFightNetworkManager.sendToPlayer(SPSetSkillContainerValue.maxResource(skillContainer.getSlot(), fVal, skillContainer.getExecutor().getOriginal().getId()), skillContainer.getServerExecutor().getOriginal());
	}
	
	public ResourceLocation getRegistryName() {
		return this.registryName;
	}
	
	public String getTranslationKey() {
		return String.format("skill.%s.%s", this.getRegistryName().getNamespace(), this.getRegistryName().getPath());
	}
	
	public float getCooldownRegenPerSecond(PlayerPatch<?> playerpatch) {
		return 1.0F;
	}
	
	public SkillCategory getCategory() {
		return this.category;
	}
	
	public CreativeModeTab getCreativeTab() {
		return this.creativeTab;
	}
	
	public int getMaxStack() {
		return this.maxStackSize;
	}
	
	public int getMaxDuration() {
		return this.maxDuration;
	}

	public float getConsumption() {
		return this.consumption;
	}
	
	public Set<Entry<Holder<Attribute>, AttributeModifier>> getModfierEntry() {
		return this.attributes.entrySet();
	}
	
	public boolean resourcePredicate(PlayerPatch<?> playerpatch, SkillCastEvent event) {
		return playerpatch.consumeForSkill(this, this.resource, event.getArguments());
	}
	
	public boolean shouldDeactivateAutomatically(PlayerPatch<?> executor) {
		return !executor.getOriginal().isCreative();
	}
	
	public ActivateType getActivateType() {
		return this.activateType;
	}
	
	public Resource getResourceType() {
		return this.resource;
	}
	
	public Skill getPriorSkill() {
		return null;
	}
	
	public Skill registerPropertiesToAnimation() {
		return this;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onScreen(LocalPlayerPatch playerpatch, float resolutionX, float resolutionY) {
	}
	
	/**
	 * @param itemStack.getCapability() == @param cap
	 * @return
	 */
	@OnlyIn(Dist.CLIENT)
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerpatch) {
		return new ArrayList<> ();
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		return list;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getSkillTexture() {
		return ResourceLocation.fromNamespaceAndPath(this.getRegistryName().getNamespace(), String.format("textures/gui/skills/%s/%s.png", this.category.toString().toLowerCase(Locale.ROOT), this.getRegistryName().getPath()));
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean shouldDraw(SkillContainer container) {
		return false;
	}
	
	@Override
	public String toString() {
		return this.getRegistryName().toString();
	}
	
	public Component getDisplayName() {
		return Component.translatable(String.format("%s.%s.%s", "skill", this.getRegistryName().getNamespace(), this.getRegistryName().getPath()));
	}
	
	public Set<WeaponCategory> getAvailableWeaponCategories() {
		return null;
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumeIconList) {
		return false;
	}
	
	public Holder<Skill> holder() {
		return this.holder;
	}
	
	@ApiStatus.Internal
	public void setHolder(Holder<Skill> holder) {
		this.holder = holder;
	}
	
	public enum ActivateType {
		ONE_SHOT, DURATION, DURATION_INFINITE, TOGGLE, HELD
	}
	
	public enum Resource {
		NONE(
			(skillContainer, playerpatch, amount) -> true,
			(skillContainer, playerpatch, amount) -> {}
		),
		
		WEAPON_CHARGE(
			(skillContainer, playerpatch, amount) -> skillContainer.getStack() >= amount,
			(skillContainer, playerpatch, amount) -> {
				Skill.setSkillStackSynchronize(skillContainer, skillContainer.getStack() - 1);
			}
		),
		
		COOLDOWN(
			(skillContainer, playerpatch, amount) -> skillContainer.getStack() >= amount,
			(skillContainer, playerpatch, amount) -> {
				Skill.setSkillStackSynchronize(skillContainer, skillContainer.getStack() - 1);
			}
		),
		
		STAMINA(
			(skillContainer, playerpatch, amount) -> playerpatch.hasStamina(amount),
			(skillContainer, playerpatch, amount) -> {
				playerpatch.resetActionTick();
				playerpatch.setStamina(playerpatch.getStamina() - amount);
			}
		),
		
		HEALTH(
			(skillContainer, playerpatch, amount) -> playerpatch.getOriginal().getHealth() > amount,
			(skillContainer, playerpatch, amount) -> {
				playerpatch.getOriginal().setHealth(playerpatch.getOriginal().getHealth() - amount);
			}
		);
		
		public final ResourcePredicate predicate;
		public final ResourceConsumer consumer;
		
		Resource(ResourcePredicate predicate, ResourceConsumer consumer) {
			this.predicate = predicate;
			this.consumer = consumer;
		}
		
		@FunctionalInterface
		public interface ResourcePredicate {
			boolean canExecute(SkillContainer skillContainer, PlayerPatch<?> playerpatch, float amount);
		}
		
		@FunctionalInterface
		public interface ResourceConsumer {
			void consume(SkillContainer skillContainer, ServerPlayerPatch playerpatch, float amount);
		}
	}
	
	public static record SkillEventSubscriber(int priority, BiConsumer<Event, SkillContainer> eventSubscriber, @Nullable Method reflectionMethod) implements Comparable<SkillEventSubscriber> {
		@Override
		public int compareTo(SkillEventSubscriber o) {
			return Integer.compare(this.priority(), o.priority()) ;
		}
	}
}
